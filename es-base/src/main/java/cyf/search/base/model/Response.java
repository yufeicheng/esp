package cyf.search.base.model;

import cyf.search.base.enums.RespStatusEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * 通用返回对象
 * 
 * @author Richard on 2017/1/5
 * @since 1.0
 */
@Getter
@Setter
@ToString
public class Response<T> implements Serializable {

	private static final long serialVersionUID = -6553522891216707934L;

	private int status;
	private String desc;
	private T result;

	/**
	 * 扩展http状态码 全局异常包装返回
	 * @param httpStatus HttpStatus
	 */
	public Response(HttpStatus httpStatus) {
		this.status = httpStatus.value();
		this.desc = httpStatus.getReasonPhrase();
	}

	/**
	 * 失败错误返回，只允许返回枚举中存在的类型，枚举中不存在先扩充枚举类
	 * @param respStatusEnum 枚举类型
	 */
	public Response(RespStatusEnum respStatusEnum) {
		this.status = respStatusEnum.getStatus();
		this.desc = respStatusEnum.getDesc();
	}

	/**
	 * 成功的返回
	 * @param t
	 */
	public Response(T t) {
		this.status = RespStatusEnum.OK.getStatus();
		this.desc = RespStatusEnum.OK.getDesc();
		this.result = t;
	}

	/**
	 * 成功的返回
	 */
	public Response() {
		this.status = RespStatusEnum.OK.getStatus();
		this.desc = RespStatusEnum.OK.getDesc();
	}

	public void setRespStatus(RespStatusEnum respStatusEnum) {
		this.status = respStatusEnum.getStatus();
		this.desc = respStatusEnum.getDesc();
	}
}
