package cyf.search.base.model.template;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * @author Cheng Yufei
 * @create 2019-08-08 13:39
 **/
@Getter
@Setter
@NoArgsConstructor
public class AskSearchVO4Web {
    private String sourceId;
    private Integer askType;
    private Integer id;
    private Integer relateId;
    private Integer rootId;
    private String userId;
    private String userName;
    private String headImage;
    private Date ctime;
    private String content;
    private Integer priType;
    private Integer type;
    private Integer status;
    private Integer isOpen;
    private Long openTime;
    private Integer isanony;
    private Integer isdel;
    private Integer isaudit;
    private String source;
    private String dirUserId;
    private String dirUserName;
    private Integer replyNum;
    private Integer relation;
    //private AnswerVo answerVo;
    private String ip;
    private String answerContent;
    private String answerVoiceMp3;
    private String answerTime;
}
