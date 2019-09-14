package cyf.search.api.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
//import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * @author Cheng Yufei
 * @create 2019-07-22 21:49
 **/
@Service
@Slf4j
public class HighLevelClient {
    //private RestHighLevelClient client;

    @PostConstruct
    public void initClient() {
        //client = new RestHighLevelClient(
        //        RestClient.builder(
        //                new HttpHost("localhost", 9200, "http"),
        //                new HttpHost("localhost", 9201, "http")));
    }

    public void test() {
       /* SearchRequest firstSearchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        firstSearchRequest.source(searchSourceBuilder).indices(new String[]{""});

        IndicesStatsRequest request = new IndicesStatsRequest();
        request.all();
        client.indices().
        SearchResponse response = client.search(firstSearchRequest, RequestOptions.DEFAULT);*/
        //client.multiSearch()
    }
}
