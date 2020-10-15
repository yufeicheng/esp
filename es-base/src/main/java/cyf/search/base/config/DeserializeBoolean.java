package cyf.search.base.config;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 *json串中为1，转实体类时转为true
 *
 * @author Cheng Yufei
 * @create 2020-10-15 11:04
 **/
public class DeserializeBoolean extends JsonDeserializer {

	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		String text = p.getText();
		if (text.equals("0")) {
			return 0;
		}
		if (text.contains("万")) {
			return Integer.valueOf(text.substring(0, text.lastIndexOf("万")))*10000;
		}
		return text.equals("1") ? true : false;
	}
}
