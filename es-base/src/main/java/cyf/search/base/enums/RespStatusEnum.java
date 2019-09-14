package cyf.search.base.enums;

import lombok.Getter;

/**
 * @since 1.0
 */
@Getter
public enum RespStatusEnum {

    //1000一下的状态码为通用状态码
    OK(0,"success"),
    ERROR(1,"系统错误"),
    PARAM_FAIL(2,"参数错误"),
    PARAM_FAIL_NOT_EMOJI(3,"参数不支持emoji表情"),

    //1000以上状态码按业务划分
    COLUMN_NOT_EXIST(1000, "栏目不存在"),
    TAG_NOT_EXIST(1001, "标签不存在"),

    EPISODE_NOT_EXIST(2000,"短片不存在"),
    EPISODE_OFFLINE(2001,"短片已下线"),

    AUTH_FAIL(3000,"请在微信中打开"),
    WECHAT_CODE_FAIL(3001,"微信code错误"),
    WECHAT_AUTH_FAIL(3002,"需要先发起网页微信授权"),

    PRAISE_REPEAT(4000,"你已赞过啦"),
    PRAISE_FILE(4001,"无效操作"),

//    COMMENT_REPEAT(5000,"标签已存在"),//TODO
    COMMENT_REPEAT(5000,"重复提交"),//TODO
    COMMENT_SENSITIVE_WORD(5001,"评论内容含有敏感词"),
    COMMENT_NOT_EXIST(5002,"评论不存在"),
    COMMENT_NOT_AUTH(5003,"无权限删除此条评论"),

    USER_NOT_LOGIN(6000,"未登录"),
	USER_SESSION_NOT_EXIST(6001, "会话不存在"),
    USER_LOGIN_FAIL(6002,"登陆失败"),
    USER_TYPE_NOT_ALLOW_UPDATE_INFO(6003, "用户已认证，不能修改信息"),
    USER_ALREADY_AUTHING(6004, "用户已申请认证，请耐心等待审核结果。"),
    USER_ALREADY_AUTHED(6005, "用户已通过认证，请不要重复提交申请。"),
    USER_NOT_UPLODE(6006, "非V用户，不能上传"),

    MOBILE_ERROR(6007,"手机号码格式错误"),
    SEND_SMS_LIMIT(6008,"发送短信次数太多了"),
    MOBILE_EXISTED(6009,"该手机号已注册"),
    SMS_ERROR(6010,"短信验证码错误"),


    VIDEO_NOT_EXIST(7000,"整片不存在"),
    ;



    ;

    private int status;
    private String desc;

    RespStatusEnum(int status, String desc){
        this.status = status;
        this.desc = desc;
    }
    
	public static RespStatusEnum get(Integer status) {
		if (status == null) {
			return null;
		}
		for (RespStatusEnum respStatusEnum : RespStatusEnum.values()) {
			if (respStatusEnum.getStatus() == status) {
				return respStatusEnum;
			}
		}
		return null;
	}
}
