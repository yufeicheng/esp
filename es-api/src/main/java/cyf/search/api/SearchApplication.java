package cyf.search.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.support.DefaultBeanNameGenerator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * boot入口
 */
@SpringBootApplication(scanBasePackages = {"cyf.search.api", "cyf.search.dao"})
@MapperScan(basePackages = "cyf.search.dao.mapper")
public class SearchApplication {

	public static void main(String[] args) {
		//System.setProperty("es.set.netty.runtime.available.processors", "false");
		new SpringApplicationBuilder(SearchApplication.class)
				//类名重复bean的处理
				.beanNameGenerator(new DefaultBeanNameGenerator())
				.run(args);
	}

	@Bean(name = "gson")
	public Gson initGson() {
		return new Gson();
	}

	@Bean(name="objectMapper")
	public ObjectMapper initObjectMapper (){
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		return objectMapper;
	}

	@Bean(name = "restTemplate")
	public RestTemplate initRestTemplate() {
		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(6500)
				.setConnectTimeout(6500)
				.setConnectionRequestTimeout(1000)
				.build();

		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(400);
		connectionManager.setDefaultMaxPerRoute(150);

		CloseableHttpClient client = HttpClients.custom().setConnectionManager(connectionManager).setDefaultRequestConfig(requestConfig).evictExpiredConnections().build();

		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(client));
		return restTemplate;
	}
}
