package cyf.search.api.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cyf.search.base.enums.IndexType;
import cyf.search.base.model.PageInDto;
import cyf.search.base.model.template.Name;
import cyf.search.dao.model.Kerr;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author Cheng Yufei
 * @create 2017-12-12 18:37
 **/
@Service
@Slf4j
public class MatchService {

    @Autowired
    private TransportClient client;

    public List<Kerr> matchQuery(PageInDto pageInDto,String field,String searchWord) throws ExecutionException, InterruptedException, IOException {

        List<Kerr> result = Lists.newArrayList();
        QueryBuilder queryBuilder = QueryBuilders.matchQuery(field, searchWord).operator(Operator.OR).minimumShouldMatch("50%");

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(IndexType.kerr.getIndex()).setTypes(IndexType.kerr.getType()).setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(queryBuilder).setFrom(pageInDto.getOffset()).setSize(pageInDto.getLimit()).highlighter(new HighlightBuilder().field("features").preTags("<span style=\"color:#FF5A5A;\">").postTags("</span>"));

        log.debug("DSL:{}", searchRequestBuilder.toString());
        SearchResponse searchResponse = searchRequestBuilder.get();
        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {
            Kerr kerr = JSONObject.parseObject(hit.getSourceAsString(), Kerr.class);
            result.add(kerr);
//            Kerr kerr = Jackson2ObjectMapperBuilder.json().build().readValue(hit.getSourceAsString(), Kerr.class);
        }
        return result;
    }

    public List<Kerr> matchPhraseQuery(PageInDto pageInDto,String field, String searchWord,int slop) {
        List<Kerr> result = Lists.newArrayList();

        //slop参数（需要移动一个词条多少次来让查询和文档匹配）
        MatchPhraseQueryBuilder matchPhraseQueryBuilder = QueryBuilders.matchPhraseQuery(field, searchWord).slop(slop);

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(IndexType.kerr.getIndex()).setTypes(IndexType.kerr.getType()).setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(matchPhraseQueryBuilder).setFrom(pageInDto.getOffset()).setSize(pageInDto.getLimit());
        log.debug("DSL:{}", searchRequestBuilder.toString());
        SearchResponse searchResponse = searchRequestBuilder.get();
        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {
            Kerr kerr = JSONObject.parseObject(hit.getSourceAsString(), Kerr.class);
            result.add(kerr);
        }
        return result;
    }

    public List<Kerr> multiMatchQuery(PageInDto pageInDto, String searchWord, String[] fields,int mulType,int operaType) {
        List<Kerr> result = Lists.newArrayList();
//          搜索title、features(手机 黄色 宝马):
//        BEST_FIELDS :（字段为中心 最佳匹配） 在一个字段中匹配多个搜索词项的分值 > 在多个字段中分开匹配的分值；
//                                          没有tie_breaker 参数时，文档得分使用最佳字段中的权重，忽略其他字段的匹配得到的分值；有tie_breaker 时其他字段的分值也会影响结果；
//                                           文档1：{ "title": "宝马5系","features": "手机 黄色 宝马",} == 文档2：{ "title": "苹果","features": "手机 黄色 宝马",};

//        MOST_FIELDS:  （字段为中心）尽可能匹配多的字段，查找与任何字段匹配的文档，其他字段也会影响最终得分，组合每个字段的权重；
//                        { "title": "手机","features": "黄色",} > {"title": "手机","features": "手机 笔记本",} > { "title": "苹果","features": "手机 黄色 宝马",}
//                         {"title": "手机 黄色", "features": "手机",} > {"title": "手机","features": "黄色",}
//        CROSS_FIELDS: （词条为中心）搜索的内容不在一个字段内需要跨字段 省市县 或者 first name 、last name


//StringUtils.deleteWhitespace(searchWord)
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(searchWord, fields)
                .operator(operaType == 1? Operator.AND: Operator.OR)
                .type(mulType == 1? MultiMatchQueryBuilder.Type.BEST_FIELDS:mulType == 2? MultiMatchQueryBuilder.Type.MOST_FIELDS: MultiMatchQueryBuilder.Type.CROSS_FIELDS);

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(IndexType.kerr.getIndex()).setTypes(IndexType.kerr.getType()).setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setQuery(multiMatchQueryBuilder);

        log.debug("DSL:{}", searchRequestBuilder.toString());
        SearchResponse searchResponse = searchRequestBuilder.get();
        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {
            Kerr kerr = JSONObject.parseObject(hit.getSourceAsString(), Kerr.class);
            Name name = JSONObject.parseObject(JSONObject.toJSONString(hit.getSourceAsMap().get("name")), Name.class);
            kerr.setFirstName(name.getFirst());
            kerr.setLastName(name.getLast());
            kerr.setScore(hit.getScore());
            result.add(kerr);
        }
        return result;

    }

    public void update(String id, String field, String element) {
        Map map = Maps.newHashMap();
        //  "name": {  "properties": {"first":xx,"last":xxx}} 形式的mapping： 存数据时set 成对象；更新是转换为json格式的Object，直接传对象会报错

        if (field.equals("name")) {
            String[] split = StringUtils.split(element, ",");
            Name name = new Name();
            name.setFirst(split[0]);
            name.setLast(split[1]);
            map.put(field, JSONObject.toJSON(name));

        } else {
            map.put(field, element);
        }

        UpdateRequestBuilder updateRequestBuilder = client.prepareUpdate(IndexType.kerr.getIndex(), IndexType.kerr.getType(), id).setDoc(map);
        log.debug("update - source:{}", updateRequestBuilder.request().doc());
        UpdateResponse updateResponse = updateRequestBuilder.get();
        RestStatus status = updateResponse.status();
        if (status.getStatus() != RestStatus.OK.getStatus()) {
            ReplicationResponse.ShardInfo shardInfo = updateResponse.getShardInfo();
            ReplicationResponse.ShardInfo.Failure[] failures = shardInfo.getFailures();
            log.error("更新出错：{}", failures);
            return;
        }
        System.out.println();
        log.debug("更新完成：{}",updateResponse.getShardInfo().getSuccessful());
    }
}
