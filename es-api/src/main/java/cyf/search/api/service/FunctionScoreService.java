package cyf.search.api.service;

import cyf.search.base.enums.IndexType;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.lucene.search.function.CombineFunction;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Cheng Yufei
 * @create 2018-03-03 上午9:36
 **/
@Service
@Slf4j
public class FunctionScoreService {

    @Autowired
    private TransportClient client;

    public void fieldValue(String searchField, String[] searchWord, String functionField) {

        //利用votes的平方影响文档得分，与搜索得分_score 以 sum方式结合
        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(QueryBuilders.termsQuery(searchField, searchWord),
                ScoreFunctionBuilders.fieldValueFactorFunction(functionField).modifier(FieldValueFactorFunction.Modifier.SQUARE)).boostMode(CombineFunction.SUM);

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(IndexType.kerr.getIndex()).setTypes(IndexType.kerr.getType()).setQuery(functionScoreQueryBuilder);

        log.debug("DSL:{}", searchRequestBuilder.request().source().toString());
    }

    public void weight(String searchField, String[] searchWord) {

        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(QueryBuilders.termsQuery(searchField, searchWord),
                //设置weight
                ScoreFunctionBuilders.weightFactorFunction(2)).boostMode(CombineFunction.MULTIPLY);

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(IndexType.kerr.getIndex()).setTypes(IndexType.kerr.getType()).setQuery(functionScoreQueryBuilder);

        log.debug("DSL:{}", searchRequestBuilder.request().source().toString());
    }

    public void gauss() {

        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(QueryBuilders.matchAllQuery(),

                // 设 gauss
                ScoreFunctionBuilders.gaussDecayFunction("votes", 10, 5, 1, 0.5)).boostMode(CombineFunction.SUM);

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(IndexType.kerr.getIndex()).setTypes(IndexType.kerr.getType()).setQuery(functionScoreQueryBuilder);

        log.debug("DSL:{}", searchRequestBuilder.request().source().toString());
    }

    public void scrip() {

        String scrip = "doc['price'].value / 1000 * doc['votes'].value";

        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(QueryBuilders.matchAllQuery(),
                ScoreFunctionBuilders.scriptFunction(scrip)).boostMode(CombineFunction.SUM);

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(IndexType.kerr.getIndex()).setTypes(IndexType.kerr.getType()).setQuery(functionScoreQueryBuilder);

        log.debug("DSL:{}", searchRequestBuilder.request().source().toString());
    }
}
