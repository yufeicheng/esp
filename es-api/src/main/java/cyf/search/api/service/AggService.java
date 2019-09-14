package cyf.search.api.service;

import com.google.common.collect.Lists;
import cyf.search.base.enums.IndexType;
import cyf.search.base.model.template.HistogramResult;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.FilterAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.global.GlobalAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.*;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.avg.AvgAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.sum.InternalSum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Cheng Yufei
 * @create 2018-02-09 10:33
 **/
@Service
@Slf4j
public class AggService {

    @Autowired
    private TransportClient client;


    public List<HistogramResult> histogram() {

        List<HistogramResult> result = Lists.newArrayList();

        HistogramAggregationBuilder histogram = AggregationBuilders.histogram("pricehis").field("price").interval(10000).subAggregation(AggregationBuilders.sum("revenue").field("price"));

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(IndexType.kerr.getIndex()).setTypes(IndexType.kerr.getType()).addAggregation(histogram).setSize(0);

        SearchResponse searchResponse = searchRequestBuilder.get();
        log.debug("DSL:{}", searchRequestBuilder.request().source().toString());

        List<Aggregation> list = searchResponse.getAggregations().asList();

        //获取价格区间、区间内的文档数、区间内文档的price字段的sum
        for (Aggregation aggregation : list) {
            List<InternalHistogram.Bucket> buckets = ((InternalHistogram) aggregation).getBuckets();
            for (InternalHistogram.Bucket bucket : buckets) {
                double xAxis = (double) bucket.getKey();
                long yAxis = bucket.getDocCount();
                InternalSum internalSum = (InternalSum) bucket.getAggregations().asList().get(0);
                double sumValue = internalSum.getValue();
                HistogramResult histogramResult = new HistogramResult(xAxis, yAxis, sumValue);
                result.add(histogramResult);
            }
        }
        return result;
    }


    public List<HistogramResult> dateHistogram() {

        List<HistogramResult> result = Lists.newArrayList();
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram("dateHis").field("publishtime").dateHistogramInterval(DateHistogramInterval.MONTH).format("yyyy-MM-dd").extendedBounds(new ExtendedBounds("2018-01-01", "2018-12-31"));

        dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.sum("votesSum").field("votes"));

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(IndexType.kerr.getIndex()).setTypes(IndexType.kerr.getType()).addAggregation(dateHistogramAggregationBuilder).setSize(0);

        log.debug("DSL:{}", searchRequestBuilder.request().source().toString());

        SearchResponse searchResponse = searchRequestBuilder.get();

        List<Aggregation> list = searchResponse.getAggregations().asList();
        for (Aggregation aggregation : list) {
            List<InternalDateHistogram.Bucket> buckets = ((InternalDateHistogram) aggregation).getBuckets();
            for (InternalDateHistogram.Bucket bucket : buckets) {
                //获取间隔日期（2018-01-01 - 2018-12-01）
                Object xAxis = bucket.getKeyAsString();
                //每个月的文档数
                long yAxis = bucket.getDocCount();
                InternalSum internalSum = (InternalSum) bucket.getAggregations().asList().get(0);
                double sumValue = internalSum.getValue();
                HistogramResult histogramResult = new HistogramResult(xAxis, yAxis, sumValue);
                result.add(histogramResult);
            }
        }
        return result;
    }


    public List<HistogramResult> dateHistogramQuarter() {

        List<HistogramResult> results = Lists.newArrayList();
        // 聚合出四个季度
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram("his").field("publishtime").dateHistogramInterval(DateHistogramInterval.QUARTER).format("yyyy-MM-dd").extendedBounds(new ExtendedBounds("2018-01-01", "2018-12-31"));

        //每个季度中聚合每个地区及地区的votes sum： 1.注意subAggregation的嵌套关系   2.city的type为text不支持，需用city.raw(类型为keyword)
        dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.terms("cityterms").field("city.raw").subAggregation(AggregationBuilders.sum("cityvotessum").field("votes")));

        //每个季度votes总和的趋势
        dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.sum("quartervotessum").field("votes"));

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(IndexType.kerr.getIndex()).setTypes(IndexType.kerr.getType()).addAggregation(dateHistogramAggregationBuilder).setSize(0);

        log.debug("DSL:{}", searchRequestBuilder.request().source().toString());

        SearchResponse searchResponse = searchRequestBuilder.get();

        List<Aggregation> list = searchResponse.getAggregations().asList();
       /* for (Aggregation aggregation : list) {
            List<InternalDateHistogram.Bucket> buckets = ((InternalDateHistogram) aggregation).getBuckets();
            for (InternalDateHistogram.Bucket bucket : buckets) {
                //获取间隔日期（2018-01-01 - 2018-12-01）
                Object xAxis = bucket.getKeyAsString();
                //每个月的文档数
                long yAxis = bucket.getDocCount();
                InternalSum internalSum = (InternalSum) bucket.getAggregations().asList().get(0);
                double sumValue = internalSum.getValue();
                HistogramResult histogramResult = new HistogramResult(xAxis, yAxis,sumValue);
                results.add(histogramResult);
            }
        }*/
        return results;
    }


    public void globalAgg(String field, String searchWord) {

        //搜索数据（"city":"美国"）
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(field, searchWord);

        //全局桶，聚合全部文档的votes avg
        GlobalAggregationBuilder global = AggregationBuilders.global("global");
        global.subAggregation(AggregationBuilders.avg("globalAvg").field("votes"));

        //聚合搜索数据的votes avg
        AvgAggregationBuilder avgAggregationBuilder = AggregationBuilders.avg("cityavg").field("votes");

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(IndexType.kerr.getIndex()).setTypes(IndexType.kerr.getType()).setQuery(matchQueryBuilder)
                .addAggregation(global)
                .addAggregation(avgAggregationBuilder)
                .setSize(0);

        log.debug("DSL:{}", searchRequestBuilder.request().source().toString());
        System.out.println();

    }

    public void filterAgg(String field, String searchWord) {
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(field, searchWord);

        FilterAggregationBuilder filter = AggregationBuilders.filter("votesFilter", QueryBuilders.rangeQuery("votes").from(10))
                .subAggregation(AggregationBuilders.avg("votesavg").field("votes"));

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(IndexType.kerr.getIndex()).setTypes(IndexType.kerr.getType()).setQuery(matchQueryBuilder)
                .addAggregation(filter);

        log.debug("DSL:{}", searchRequestBuilder.request().source().toString());

    }

    public void postFilter(String searchWrd,String color) {

        //搜出所有跑车
        MatchPhraseQueryBuilder matchPhraseQueryBuilder = QueryBuilders.matchPhraseQuery("features", searchWrd);

        //聚合跑车所有颜色
        TermsAggregationBuilder allColor = AggregationBuilders.terms("allColor").field("color");

        //后置过滤器，在查询完后被执行，不影响聚合结果
        TermQueryBuilder assignColor = QueryBuilders.termQuery("color", color);

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(IndexType.kerr.getIndex()).setTypes(IndexType.kerr.getType()).setQuery(matchPhraseQueryBuilder)
                .addAggregation(allColor)
                .setPostFilter(assignColor)
                .setSize(10);

        log.debug("DSL:{}", searchRequestBuilder.request().source().toString());

    }

    public void sort() {

        TermsAggregationBuilder allColor = AggregationBuilders.terms("allColor").field("color");
        allColor.subAggregation(AggregationBuilders.avg("avgVotes").field("votes"));

        //度量排序：按照聚合的平均值排序
        allColor.order(Terms.Order.aggregation("avgVotes", false));
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(IndexType.kerr.getIndex()).setTypes(IndexType.kerr.getType()).addAggregation(allColor).setSize(0);
        log.debug("DSL:{}", searchRequestBuilder.request().source().toString());
    }
}
