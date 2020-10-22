package cyf.search.api.service;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import cyf.search.base.enums.IndexType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
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

	/**
	 * 避免文档有相同的sort value，加入 _uid 【每条文档有唯一的值】
	 * @param searchAfterParam
	 * @return
	 */
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
				objectNode.putPOJO("sort", h.getSortValues());
				return objectNode;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}).collect(Collectors.toList());
		return collect;
	}

	/**
	 * 不用于实时请求，用于大量数据处理，eg：reindex
	 * @param scrollId
	 * @return
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public Map scroll(String scrollId) throws ExecutionException, InterruptedException {

		if (StringUtils.isNotBlank(scrollId)) {
			SearchScrollRequest scroll = new SearchScrollRequest(scrollId).scroll(TimeValue.timeValueMinutes(1));
			log.info("scroll,非首次请求 DSL:{}", scroll.toString());
			SearchResponse searchResponse = client.searchScroll(scroll).get();
			List<ObjectNode> list = handleResponse(searchResponse);
			return ImmutableMap.of("data", list, "scroll_id", searchResponse.getScrollId());
		}

		TermQueryBuilder termQuery = QueryBuilders.termQuery("province_id", 32);
		SearchRequestBuilder requestBuilder = client.prepareSearch(IndexType.COLLEGE.getIndex()).setTypes(IndexType.COLLEGE.getType())
				.setScroll("1m")
				.setQuery(termQuery)
				.setSize(5)
				.addSort("id", SortOrder.ASC);

		log.info("scroll,首次请求 DSL:{}", requestBuilder.toString());
		SearchResponse firstResponse = requestBuilder.get();
		List<ObjectNode> list = handleResponse(firstResponse);
		return ImmutableMap.of("data", list, "scroll_id", firstResponse.getScrollId());
	}

	private List<ObjectNode> handleResponse(SearchResponse searchResponse) {
		SearchHit[] hits = searchResponse.getHits().getHits();
		List<ObjectNode> collect = Stream.of(hits).map(h -> {
			try {
				ObjectNode objectNode = objectMapper.readValue(h.getSourceAsString(), ObjectNode.class);
				return objectNode;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}).collect(Collectors.toList());
		return collect;
	}
}
