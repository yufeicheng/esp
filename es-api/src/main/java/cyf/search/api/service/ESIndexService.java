package cyf.search.api.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cyf.search.base.enums.IndexType;
import cyf.search.base.model.template.College;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.ScrollableHitSource;
import org.elasticsearch.rest.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

@Service
@Slf4j
public class ESIndexService {


	@Autowired
	private TransportClient client;
	@Autowired
	private Gson gson;
	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private ObjectMapper objectMapper;

	private static final String collegeUrl = "https://static-data.eol.cn/www/school/%s/info.json";

	public String sendTypeToIndex(String jsonData, String indexName, String typeName, String primaryKey) {

		BulkRequestBuilder requestBuilder = client.prepareBulk().add(client.prepareIndex(indexName, typeName, primaryKey).setSource(jsonData, XContentType.JSON));
		//多条数据可add多次，最后执行get请求

		BulkResponse responses = requestBuilder.get();
		if (responses.hasFailures()) {
			log.error("根据id添加数据失败，msg:{}", responses.buildFailureMessage());
			return "sendTypeToIndex: fail";
		}
		log.info("添加数据成功，index:{}，type:{}，id:{}", indexName, typeName, primaryKey);
		return "success";
	}


	public String deleteDocument(String indexName, String typeName, String sourceId) {
		log.info("根据id删除数据，index:{}，type:{}，id:{}", indexName, typeName, sourceId);
		DeleteResponse deleteResponse = client.prepareDelete(indexName, typeName, sourceId).get();
		if (deleteResponse.status().getStatus() != RestStatus.OK.getStatus()) {
			return "删除失败";
		}
		return "success";
	}


	public String sendTypeToIndex(String jsonData, String indexName, String typeName) {
		BulkResponse responses = client.prepareBulk().add(client.prepareIndex(indexName, typeName).setSource(jsonData, XContentType.JSON)).get();
		if (responses.hasFailures()) {
			return "sendTypeToIndex: fail";
		}
		return "success";
	}


	public String deleteByQuery(String[] ids) {

		BulkByScrollResponse response = DeleteByQueryAction.INSTANCE.newRequestBuilder(client)
				.filter(QueryBuilders.termsQuery("_id", ids))
				.source(IndexType.kerr.getIndex())
				.get();

		List<BulkItemResponse.Failure> bulkFailures = response.getBulkFailures();
		List<ScrollableHitSource.SearchFailure> searchFailures = response.getSearchFailures();
		if (!bulkFailures.isEmpty() || !searchFailures.isEmpty()) {
			log.error("删除数据失败，bulkFailures:{},searchFailures:{}", bulkFailures.toString(), searchFailures.toString());
			return "删除数据失败";
		}
		return "success";
	}

	public String sendTypeToIndexForPoetry() throws IOException {
		Resource resource = new PathMatchingResourcePatternResolver().getResource("classpath:kibana_dsl/300.json");
		File file = resource.getFile();
		String s = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

		JsonArray array = JsonParser.parseString(s).getAsJsonArray();
		BulkRequestBuilder bulk = client.prepareBulk();
		array.forEach(a -> {
			JsonObject object = a.getAsJsonObject();
			IndexRequestBuilder builder = client.prepareIndex(IndexType.POETRY.getIndex(), IndexType.POETRY.getType()).setId(object.get("id").getAsString())
					.setPipeline("index_at").setSource(object.toString(), XContentType.JSON);
			bulk.add(builder);
		});

		BulkResponse responses = bulk.get();
		if (responses.hasFailures()) {
			log.error("根据id添加数据失败，msg:{}", responses.buildFailureMessage());
			return "sendTypeToIndex: fail";
		}
		log.info("添加数据成功");
		return "success";
	}

	/**
	 * 保存上下文提示
	 * PUT context_suggest/A/1
	 * {
	 *   "id":1,
	 *   "contents":{
	 *     "input":["长安一片月，万户捣衣声。秋风吹不尽，总是玉关情。何日平胡虏，良人罢远征？"],
	 *     "contexts":{
	 *       "con_type":["poetry"]
	 *     }
	 *   }
	 * }
	 * @param id
	 * @param input
	 * @param category
	 * @return
	 * @throws IOException
	 */
	public String sendTypeToIndexForContextSuggest(Integer id, String input, String category) throws IOException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("id", id);

		JSONObject contents = new JSONObject();
		JSONArray inputs = new JSONArray();
		inputs.add(input);
		contents.put("input", inputs);

		JSONObject categorys = new JSONObject();
		categorys.put("con_type", category);
		contents.put("contexts", categorys);

		jsonObject.put("contents", contents);

		IndexRequestBuilder builder = client.prepareIndex(IndexType.CONTEXT_SUGGEST.getIndex(), IndexType.CONTEXT_SUGGEST.getType())
				.setId(String.valueOf(id))
				.setSource(jsonObject.toString(), XContentType.JSON);

		IndexResponse indexResponse = builder.get();
		RestStatus status = indexResponse.status();
		System.out.println();
		return "success";
	}

	public String sendTypeToIndexForCollege() throws IOException {
		BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
		Stream.iterate(4000, i -> ++i).limit(500).forEach(j -> {
			String object = restTemplate.getForObject(String.format(collegeUrl, j), String.class);
			if (StringUtils.isBlank(object) || object.equals("\"\"")) {
				return;
			}
			try {
				College college = objectMapper.readValue(object, College.class);
				IndexRequest indexRequest = new IndexRequest(IndexType.COLLEGE.getIndex(), IndexType.COLLEGE.getType(), String.valueOf(college.getId()))
						.source(objectMapper.writeValueAsString(college), XContentType.JSON);
				bulkRequestBuilder.add(indexRequest);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		int numberOfActions = bulkRequestBuilder.numberOfActions();
		if (numberOfActions ==0) {
			log.info("无数据添加");
			return "success";
		}
		BulkResponse responses = bulkRequestBuilder.get();
		if (responses.hasFailures()) {
			log.error("根据id添加数据失败，msg:{}", responses.buildFailureMessage());
			return "sendTypeToIndex: fail";
		}
		log.info("添加数据成功，{}条", numberOfActions);
		return "success";
	}

	/**
	 * 下载图片到本地
	 *
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static String downloadImg(String url) throws IOException {
		FileOutputStream fileOutputStream = null;
		InputStream inputStream = null;
		BufferedInputStream bufferedInputStream = null;

		String fileName = StringUtils.substring(url, StringUtils.lastIndexOf(url, "/") + 1);
		if (fileName.length() > 36) {
			fileName = StringUtils.substring(fileName, 0, StringUtils.indexOf(fileName, "?"));
		}
		LocalDate now = LocalDate.now();
		/*String dirPath = new StringBuilder().append("/data/images/").append(now.getYear()).append("/").append(now.getMonthValue()).append("/").append(now.getDayOfMonth()).toString();*/
		String dirPath = new StringBuilder().append("D:/data/images/").append(now.getYear()).append("/").append(now.getMonthValue()).append("/").append(now.getDayOfMonth()).toString();
		File dir = new File(dirPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		String path = dir + "/" + fileName;
		File file = new File(path);

		try {
			fileOutputStream = new FileOutputStream(file);

			URL httpUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false);
			conn.setRequestProperty("User-Agent", "Mozilla/4.0 compatible; MSIE 5.0;Windows NT; DigExt)");
			conn.connect();

			inputStream = conn.getInputStream();
			bufferedInputStream = new BufferedInputStream(inputStream);
			byte b[] = new byte[1024];
			int len = 0;
			while ((len = bufferedInputStream.read(b)) != -1) {
				fileOutputStream.write(b, 0, len);
			}

		} catch (IOException e) {
			e.printStackTrace();
			path = null;

		} finally {
			bufferedInputStream.close();
			inputStream.close();
			fileOutputStream.close();
		}
		return path;
	}
}
