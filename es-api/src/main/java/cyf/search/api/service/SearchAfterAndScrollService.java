package cyf.search.api.service;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cyf.search.base.enums.IndexType;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Cheng Yufei
 * @create 2020-10-21 16:28
 **/
@Service
@Slf4j
public class SearchAfterAndScrollService {

	@Autowired
	private TransportClient client;
	@Autowired
	private ObjectMapper objectMapper;

	public List<ObjectNode> searchAfter(String[] searchAfterParam) {
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		boolQueryBuilder.filter(QueryBuilders.termQuery("province_id", 32)).mustNot(QueryBuilders.termQuery("create_date", 0));

		SearchRequestBuilder requestBuilder = client.prepareSearch(IndexType.COLLEGE.getIndex()).setTypes(IndexType.COLLEGE.getType())
				.setQuery(boolQueryBuilder).addSort("create_date", SortOrder.ASC).addSort("_uid", SortOrder.ASC);

		if (searchAfterParam.length != 0) {
			requestBuilder.searchAfter(searchAfterParam);
		}

		log.info("searchAfter,DSL:{}", requestBuilder.toString());
		SearchResponse searchResponse = requestBuilder.get();
		SearchHit[] hits = searchResponse.getHits().getHits();
		List<ObjectNode> collect = Stream.of(hits).map(h -> {
			try {
				ObjectNode objectNode = objectMapper.readValue(h.getSourceAsString(), ObjectNode.class);
				objectNode.putPOJO("sort",h.getSortValues());
				return objectNode;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}).collect(Collectors.toList());
		return collect;
	}
}
