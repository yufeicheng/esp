import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import cyf.search.base.model.template.College;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Cheng Yufei
 * @create 2020-10-15 10:37
 **/
public class TestC {
	@Test
	public void jackson() throws IOException {
		String s = "{\"school_id\":\"31\",\"data_code\":\"112000100\",\"name\":\"北京大学\",\"type\":\"5000\",\"school_type\":\"6000\",\"school_nature\":\"36000\",\"level\":\"2001\",\"code_enroll\":\"1000100\",\"belong\":\"教育部\",\"f985\":\"1\",\"f211\":\"1\",\"department\":\"1\",\"admissions\":\"1\",\"central\":\"2\",\"dual_class\":\"38001\",\"is_seal\":\"2\",\"num_subject\":\"54\",\"num_master\":\"282\",\"num_doctor\":\"258\",\"num_academician\":\"58\",\"num_library\":\"703万\",\"num_lab\":\"47\",\"province_id\":\"11\",\"city_id\":\"1101\",\"county_id\":\"110108\",\"is_ads\":\"1\",\"is_recruitment\":\"1\",\"create_date\":\"1898\",\"area\":7000,\"old_name\":\"\",\"status\":\"1\",\"add_id\":\"0\",\"add_time\":\"2018-12-08 17:23:21\",\"update_id\":\"825\",\"update_time\":\"2020-07-23 14:10:07\",\"ad_level\":\"1\",\"short\":\"pku,北大\",\"e_pc\":\"1\",\"e_app\":\"1\",\"ruanke_rank\":\"2\",\"single\":\"\",\"colleges_level\":\"\",\"doublehigh\":\"0\",\"wsl_rank\":\"2\",\"qs_rank\":\"2\",\"xyh_rank\":\"1\",\"is_sell\":\"2\",\"eol_rank\":\"2\",\"school_batch\":\"7\",\"logo\":\"/app/html/upload/logo/31.jpg\",\"level_name\":\"普通本科\",\"type_name\":\"综合类\",\"school_type_name\":\"普通本科\",\"school_nature_name\":\"公办\",\"dual_class_name\":\"双一流\",\"province_name\":\"北京\",\"city_name\":\"北京市\",\"town_name\":\"海淀区\",\"email\":\"bdzsb@pku.edu.cn\",\"school_email\":\"\",\"address\":\"北京市海淀区颐和园路5号\",\"postcode\":\"100871\",\"site\":\"http://www.gotopku.cn\",\"school_site\":\"https://www.pku.edu.cn//\",\"phone\":\"010-62751407,010-62554332\",\"school_phone\":\"\",\"content\":\"北京大学创办于1898年，初名京师大学堂，是中国第一所国立综合性大学，也是当时中国最高教育行政机关。辛亥革命后，于1912年改为现名。作为新文化运动的中心和“五四”运动的策源地，作为中国最早传播马克思主义和民主科学思想的发祥地，作为中国共产党最早的活动基地，北京大学为民族的振兴和解放、国家的建设和发展、社会的文明和进步做出了不可替代的贡献，在\"}";

		// 反序列化Bean
		ObjectMapper objectMapper = new ObjectMapper();
		//禁用不匹配字段报错
		//objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		//objectMapper.registerModule(new JavaTimeModule());
		//objectMapper.registerModule(new Jdk8Module());
		College college = objectMapper.readValue(s, College.class);

		System.out.println("++++++");

		//序列化转集合或Map
		String s2 = "{\"name\":\"yitian\",\"interests\":[\"pc games\",\"music\"],\"age\":25}";
		JsonNode jsonNode = objectMapper.readTree(s2);
		jsonNode.get("interests").forEach(c -> {
			System.out.println(c.asText());
		});
		Map<String, Object> map = objectMapper.readValue(s2, new TypeReference<Map<String, Object>>() {
		});
		List<String> valuesAsText = (List<String>) map.get("interests");
		System.out.println(valuesAsText);


		College college1 = new College();
		//兼容java8的日期类型，返回2020-10-20T16:30:32.754
		objectMapper.registerModule(new JavaTimeModule());
		//禁用日期转为字符串，返回2020-10-20T07:53:45.138+0000
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		college1.setTestTimeLocal(LocalDateTime.now());
		college1.setTestTimeDate(new Date());
		System.out.println(objectMapper.writeValueAsString(college1));


		System.out.println("++++++");
		//Date类型的属性，在反序列化时可以直接传timestamp
		String str = "{\"testTimeDate\": \"1603180178971\"}";
		College college2 = objectMapper.readValue(str, College.class);
		System.out.println(college2);

	}

}

