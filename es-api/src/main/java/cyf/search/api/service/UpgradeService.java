package cyf.search.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cyf.search.base.model.template.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.tophits.TopHits;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//import org.elasticsearch.search.aggregations.BucketOrder;

/**
 * @author Cheng Yufei
 * @create 2019-08-06 10:36
 **/
@Service
@Slf4j
public class UpgradeService {
    @Resource
    private TransportClient client;

    private static final FastDateFormat sdf = FastDateFormat.getInstance("yyyyMMdd");

    private org.apache.commons.lang3.time.FastDateFormat format = org.apache.commons.lang3.time.FastDateFormat.getInstance("yyyyMMddHHmmss");


    public QueryResult<JSONObject> concern(Set<String> userIds, Set<String> stocks, int from, int size) {
        log.info("searchUsersConcernsBase...");
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        int userIdsSize = 0, stocksSize = 0;

        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.WEEK_OF_MONTH, -2);
        Date beginDate = calendar.getTime();
        QueryBuilder pubDateRangeBuilder = QueryBuilders.rangeQuery("pubDate")
                .from(sdf.format(beginDate)).to(sdf.format(now));

        if (!CollectionUtils.isEmpty(userIds)) {
            userIdsSize = userIds.size();
            boolQueryBuilder.should(QueryBuilders.termsQuery("userId", userIds));
        }
        if (!CollectionUtils.isEmpty(stocks)) {
            stocksSize = stocks.size();
            boolQueryBuilder.should(QueryBuilders.termsQuery("stocks", stocks));
        }
        boolQueryBuilder.filter(pubDateRangeBuilder);

        SearchRequestBuilder searchRequestBuilder = client
                .prepareSearch("ugcindex")
                .setTypes("concern")
                .setSearchType(SearchType.DEFAULT).setFetchSource(null, "content")
                //.setQuery(boolQueryBuilder)
                .setQuery(QueryBuilders.termsQuery("userId", userIds))
                .setFrom(from).setSize(size).setExplain(false).addSort("ctime", SortOrder.DESC);
        String jsonData = searchRequestBuilder.toString();
        log.info("DSL:{}", jsonData);

        SearchResponse searchResponse = searchRequestBuilder.get();

        SearchHit[] hits = searchResponse.getHits().getHits();

        log.info("searchUsersConcernsBase->searchResult== took:" + searchResponse.getTook() + ",timed_out:" + searchResponse.isTimedOut()
                + ",_shards:" + searchResponse.getSuccessfulShards());

        QueryResult<JSONObject> qr = new QueryResult<JSONObject>();
        qr.setRecordnum(searchResponse.getHits().getTotalHits());

        List<JSONObject> list = new ArrayList<JSONObject>();
        Stream.of(hits).forEach(h -> {
            Map<String, Object> sourceAsMap = h.getSourceAsMap();
            List stocksList = (List) sourceAsMap.get("stocks");
            if (stocksList.isEmpty()) {
                list.add(JSONObject.parseObject(JSON.toJSONString(sourceAsMap)));
                return;
            }
            if (!CollectionUtils.isEmpty(stocks)) {
                sourceAsMap.put("stocks", stocksList.stream().filter(s -> stocks.contains(s)).collect(Collectors.toList()));
                sourceAsMap.put("mainContent", HtmlUtils.htmlUnescape((String) sourceAsMap.get("mainContent")));
            }
            //list.add(JSONObject.parseObject(JSON.toJSONString(sourceAsMap)));
            list.add(JSONObject.parseObject(h.getSourceAsString()));
        });
        qr.setList(list);
        return qr;
    }

    public String getById(String id) {
        SearchRequestBuilder requestBuilder = client.prepareSearch("ugcindex").setTypes("concern").setQuery(QueryBuilders.termQuery("id", id));
        SearchResponse response = requestBuilder.get();
        SearchHit[] hits = response.getHits().getHits();
        if (hits.length == 0) {
            return null;
        }
        return hits[0].getSourceAsString();
    }

    public JSONObject getAsk() {

        SearchRequestBuilder searchRequestBuilder = client
                .prepareSearch("ugcindex")
                .setTypes("ask")
                .setSearchType(SearchType.DEFAULT).setQuery(QueryBuilders.matchAllQuery()).addAggregation(AggregationBuilders
                        .terms("by_stocks")
                        .field("stocks").size(20).shardSize(1000));
        SearchResponse response = searchRequestBuilder.get();
        log.debug("DSL:{}", searchRequestBuilder.toString());

        //取bucks时若不知转哪个类型或者类型转换错误，则使用Terms接收
        List<? extends Terms.Bucket> buckets = ((Terms) response.getAggregations().asMap().get("by_stocks")).getBuckets();
        if (buckets.isEmpty()) {
            return null;
        }
        List<StockBucket> list = new ArrayList<StockBucket>(buckets.size());
        buckets.stream().forEach(b -> {
            String key = (String) b.getKey();
            String[] split = key.split("\\|");
            if (split.length == 2) {
                StockBucket stockBucket = new StockBucket();
                stockBucket.setStockName(split[0]);
                stockBucket.setStockCode(split[1]);
                stockBucket.setDocCount(b.getDocCount());
                list.add(stockBucket);
            }
        });
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", list);
        return jsonObject;
    }


    public QueryResult<JSONObject> getByAgg(Set<String> stockCodes, String aggSortKey) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.DAY_OF_YEAR, -3);
        Date beginDate = calendar.getTime();

        /*DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate now = LocalDate.of(2017, 9, 14);

        QueryBuilder pubDateRangeBuilder = QueryBuilders.rangeQuery("pubDate")
                .from(formatter.format(now.minusDays(3))).to(formatter.format(now));*/
        QueryBuilder pubDateRangeBuilder = QueryBuilders.rangeQuery("pubDate")
                .from(sdf.format(beginDate)).to(sdf.format(now));

        if (!CollectionUtils.isEmpty(stockCodes)) {
            boolQueryBuilder.should(QueryBuilders.termsQuery("stocks", stockCodes));
        }

        boolQueryBuilder.filter(pubDateRangeBuilder);

        SearchRequestBuilder searchRequestBuilder = client
                .prepareSearch("ugcindex2")
                .setTypes("stockcon")
                .setQuery(boolQueryBuilder)
                .setSize(0).setExplain(false)
                .addAggregation(AggregationBuilders
                        .terms("by_stocks")
                        .field("stockCode")
                        .order(Terms.Order.aggregation("maxValue", false)).size(10)
                        //.order(Order.aggregation("maxValue", false)).size(10)
                        .subAggregation(AggregationBuilders.topHits("top_hits_source").sort(aggSortKey, SortOrder.DESC).sort("ctime", SortOrder.DESC)
                                .fetchSource(true).size(1))
                        .subAggregation(AggregationBuilders.max("maxValue").field(aggSortKey))
                );
        log.debug("DSL:{}", searchRequestBuilder.toString());
        SearchResponse response = searchRequestBuilder.get();
        QueryResult<JSONObject> qr = new QueryResult<JSONObject>();
        List<? extends Terms.Bucket> buckets = ((Terms) (response.getAggregations().getAsMap().get("by_stocks"))).getBuckets();
        if (buckets.isEmpty()) {
            qr.setRecordnum(0);
            qr.setList(Collections.emptyList());
            return qr;
        }
        qr.setRecordnum(buckets.size());
        List<JSONObject> list = buckets.stream().map(bucket -> {
            SearchHit[] hitsSources = ((TopHits) bucket.getAggregations().getAsMap().get("top_hits_source")).getHits().getHits();
            Map<String, Object> sourceAsMap = hitsSources[0].getSourceAsMap();
            sourceAsMap.remove("stocks");
            sourceAsMap.remove("user");
            return JSONObject.parseObject(JSON.toJSONString(sourceAsMap));
        }).collect(Collectors.toList());
        qr.setList(list);
        return qr;
    }

    public String updataDocByIndexAndTypeAndFields(String indexName, String typeName, String id, Map<String, Object> fieldsAndData) {

        if (StringUtils.isAnyEmpty(indexName, typeName, id) || CollectionUtils.isEmpty(fieldsAndData)) {
            return "{" + "\"retCode\":" + -1 + ",\"msg\":\"参数错误\"}";
        }
       /*  Set<String> fieldsNameSet = fieldsAndData.keySet();
       UpdateRequestBuilder updateRequestBuilder = null;
        try {
            XContentBuilder builder = jsonBuilder().startObject();
            fieldsNameSet.stream().forEach(field -> {
                try {
                    //builder.field(field, JSON.toJSONString(fieldsAndData.get(field)));
                    builder.field(field, JSON.toJSONString(fieldsAndData.get(field)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            builder.endObject();
            updateRequestBuilder = client.prepareUpdate(indexName, typeName, id).setDoc(builder);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        UpdateRequestBuilder updateRequestBuilder = client.prepareUpdate(indexName, typeName, id).setDoc(fieldsAndData);
        UpdateResponse updateResponse = updateRequestBuilder.get();
        RestStatus status = updateResponse.status();
        if (status.getStatus() != RestStatus.OK.getStatus()) {
            ReplicationResponse.ShardInfo shardInfo = updateResponse.getShardInfo();
            ReplicationResponse.ShardInfo.Failure[] failures = shardInfo.getFailures();
            log.error("更新出错：{}", failures);
            return "";
        }
        log.debug("更新完成：{}", updateResponse.getShardInfo().getSuccessful());

        //返回的数据格式
//		{
//			   "_index": "ugcindex",
//			   "_type": "asknew",
//			   "_id": "2799",
//			   "_version": 8
//		}
        return "context";
    }

    public QueryResult<AskSearchVO4Web> searchAsk4Web(String keyword, int from, int size, boolean simple, boolean highlight) {
        log.info("keyword : " + keyword);
        String[] keywordsArray = keyword.split(",");

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.filter(QueryBuilders.rangeQuery("pubTime").gt("19700101000000").lt("99991231235959"))
                .filter(QueryBuilders.rangeQuery("answeredTimes").gt(0))
                .filter(QueryBuilders.rangeQuery("isOpen").from(1).to(1))
                .filter(QueryBuilders.rangeQuery("type").from(1).to(1));
                /*.filter(QueryBuilders.termQuery("isOpen", 1))
                .filter(QueryBuilders.termQuery("type", 1))*/
        //.filter(QueryBuilders.termsQuery("content", keywordsArray));

        SearchRequestBuilder searchRequestBuilder = client
                .prepareSearch("ugcindex")
                .setTypes("ask")
                .setSearchType(SearchType.DEFAULT)
                //.setQuery(boolQuery)
                .setQuery(QueryBuilders.matchAllQuery())
                .setPostFilter(boolQuery)
                .setFrom(from).setSize(size).addSort("pubTime", SortOrder.DESC);
        log.debug("DSL:{}", searchRequestBuilder.toString());

        SearchResponse response = searchRequestBuilder.get();
        SearchHit[] hits = response.getHits().getHits();
        QueryResult<AskSearchVO4Web> result = new QueryResult<>();
        if (hits.length == 0) {
            result.setRecordnum(0);
            result.setList(Collections.emptyList());
            return result;
        }

        result.setRecordnum(response.getHits().getTotalHits());
        List<AskSearchVO4Web> askSearchVO4Webs = Stream.of(hits).map(hit -> {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            AskSearchVO4Web ask = new AskSearchVO4Web();
            ask.setUserId(((String) sourceAsMap.get("userId")));
            ask.setUserName((String) sourceAsMap.get("userName"));
            ask.setAskType((Integer) sourceAsMap.get("type"));
            ask.setId((Integer) sourceAsMap.get("id"));
            String content = (String) sourceAsMap.get("content");
            ask.setContent("");
            if (content != null) {
                String contentUnescape = HtmlUtils.htmlUnescape(HtmlUtils.htmlUnescape(content));
                content = content.equals(contentUnescape) ? contentUnescape : cyf.search.base.utils.HtmlUtils .extractText(contentUnescape);
                ask.setContent(content);
            }
            try {
                ask.setCtime(format.parse(((String) sourceAsMap.get("pubTime"))));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (highlight) {
                ask.setContent(highlightKeyword(ask.getContent(), keywordsArray));
            }
            return ask;
        }).collect(Collectors.toList());

        HashSet<String> idSet = new HashSet<>();
        for (int i = 0; i < askSearchVO4Webs.size(); i++) {
            if (simple) {
                if (i == 0) {
                    idSet.add(String.valueOf(askSearchVO4Webs.get(i).getId()));
                }
            } else {
                idSet.add(String.valueOf(askSearchVO4Webs.get(i).getId()));
            }
        }
        result.setList(askSearchVO4Webs);
        return result;
    }

    private String highlightKeyword(String text, String[] keywords) {
        if (keywords == null || keywords.length == 0) {
            return text;
        }
        for (String keyword : keywords) {
            text = text.replaceAll(keyword, "<span class=red>" + keyword + "</span>");
        }
        return text;
    }

    public JSONObject bulkAdd() {
        AdviserUser adviserUser = new AdviserUser();
        adviserUser.setUserId("6");
        adviserUser.setType(6);
        adviserUser.setPosition("position6");
        adviserUser.setCompany("company6");
        adviserUser.setPassportName("passportname6");
        adviserUser.setUserName("username6");
        adviserUser.setProvince("province6");
        adviserUser.setCity("city6");
        adviserUser.setGrowupVal(6);
        adviserUser.setSignV(6);
        adviserUser.setTypeDesc("typedesc6");
        adviserUser.setIntro("intro6");
        adviserUser.setLevel(6);
        adviserUser.setCertificationNum("copy.getCertificationNum()6");

        AdviserUser adviserUser2 = new AdviserUser();
        adviserUser2.setUserId("7");
        adviserUser2.setType(7);
        adviserUser2.setPosition("position7");
        adviserUser2.setCompany("company7");
        adviserUser2.setPassportName("passportname7");
        adviserUser2.setUserName("username7");
        adviserUser2.setProvince("province7");
        adviserUser2.setCity("city7");
        adviserUser2.setGrowupVal(7);
        adviserUser2.setSignV(7);
        adviserUser2.setTypeDesc("typedesc7");
        adviserUser2.setIntro("intro7");
        adviserUser2.setLevel(7);
        adviserUser2.setCertificationNum("copy.getCertificationNum()7");


        ArrayList<AdviserUser> users = Lists.newArrayList(adviserUser, adviserUser2);

        HashMap<String, Object> map = new HashMap<>();
        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
        users.stream().forEach(user -> {
            map.put("userId", user.getUserId());
            map.put("userName", user.getUserName());
            map.put("type", user.getType());
            map.put("company", user.getCompany());
            map.put("province", user.getProvince());
            map.put("city", user.getCity());
            map.put("headImage", user.getHeadImage());
            map.put("intro", user.getIntro());
            map.put("status", 5);
            bulkRequestBuilder.add(client.prepareIndex("ugcindex3", "adviser",user.getUserId()).setId(user.getUserId()).setSource(map, XContentType.JSON));
        });
        log.debug("数量：{}", bulkRequestBuilder.numberOfActions());
        BulkResponse responses = bulkRequestBuilder.get();
        return JSONObject.parseObject(JSON.toJSONString(responses));
    }

    public void delete() {
        client.prepareDelete("ugcindex3", "adviser", String.valueOf(6)).get();
    }


    public JSONObject searchSBUserByInfo( Long money) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.rangeQuery("subId").gte(0).lt(Integer.MAX_VALUE));

        AggregationBuilder aggs = AggregationBuilders.range("money_gte").field("subId").addRange(money, Integer.MAX_VALUE);

        SearchRequestBuilder searchRequestBuilder = client
                .prepareSearch("ugcindex2")
                .setTypes("stockcon")
                .setSearchType(SearchType.DEFAULT).setQuery(boolQueryBuilder).addAggregation(aggs)
                .setFrom(0).setSize(10).setExplain(false).addSort("subId", SortOrder.DESC);

        SearchResponse response = searchRequestBuilder.get();
        SearchHit[] hits = response.getHits().getHits();

        JSONObject obj = new JSONObject();
        if (hits.length == 0) {
            obj.put("top10", Collections.emptyList());
            obj.put("total", 0);
            return obj;
        }

        long recordnum = response.getHits().getTotalHits();
        //前十数据
        List<JSONObject> list = Stream.of(hits).map(h -> {
            Map<String, Object> sourceAsMap = h.getSourceAsMap();
            String phoneStr = (String) sourceAsMap.get("phone");
            if (StringUtils.isNotEmpty(phoneStr)) {
                String phoneTemp = phoneStr.substring(0, 3) + "**" + phoneStr.substring(9);
                sourceAsMap.put("phone", phoneTemp);
            }
            return JSONObject.parseObject(JSON.toJSONString(sourceAsMap));
        }).collect(Collectors.toList());

        //聚合数据
        List<? extends Range.Bucket> buckets = ((Range) response.getAggregations().asMap().get("money_gte")).getBuckets();
        return new JSONObject();
    }

    public void bulkJson() {
        User user = new User();
        user.setName("doe");
        client.prepareBulk().add(client.prepareIndex("candice","user").setSource(JSONObject.parseObject(JSON.toJSONString(user)))).get();
    }
}