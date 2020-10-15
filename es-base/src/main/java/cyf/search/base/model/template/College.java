package cyf.search.base.model.template;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import cyf.search.base.config.DeserializeBoolean;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Date;

/**
 * @JsonProperty: 序列化和反序列化都使用指定的名称
 *
 * @JsonAlias: 指定的名称只用于反序列化（字符串转实体类时候），实体类转字符串时仍然使用字段名
 *
 * @author Cheng Yufei
 * @create 2020-10-15 10:13
 **/
@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class College {

	@JsonAlias("school_id")
	Integer id;
	String name;
	@JsonAlias("type")
	Integer type_code;
	String type_name;
	@JsonAlias("school_type")
	Integer school_type_code;
	String school_type_name;
	@JsonAlias("school_nature")
	Integer school_nature_code;
	String school_nature_name;
	@JsonAlias("level")
	Integer level_code;
	String level_name;
	@JsonAlias("dual_class")
	Integer dual_class_code;
	String dual_class_name;
	String belong;
	@JsonDeserialize(using = DeserializeBoolean.class)
	boolean f985;
	@JsonDeserialize(using = DeserializeBoolean.class)
	boolean f211;
	Integer num_subject;
	Integer num_master;
	Integer num_doctor;
	Integer num_academician;
	@JsonDeserialize(using = DeserializeBoolean.class)
	Integer num_library;
	Integer num_lab;
	Integer province_id;
	String province_name;
	Integer city_id;
	String city_name;
	Integer county_id;
	String town_name;
	Integer create_date;
	float area;
	@JsonAlias("short")
	String shortV;
	String email;
	String address;
	Integer postcode;
	String site;
	String school_site;
	String phone;
	String content;
}