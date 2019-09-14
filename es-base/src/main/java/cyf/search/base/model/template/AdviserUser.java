package cyf.search.base.model.template;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Cheng Yufei
 * @create 2019-08-08 17:53
 **/
@Setter
@Getter
@NoArgsConstructor
public class AdviserUser {
    protected int type;
    protected String position;
    protected String company;
    protected String passportName;
    protected String userName;
    private String province;
    private String city;
    private int growupVal = 1;
    private int signV = 1;
    private String typeDesc;
    private String intro;
    private int level;
    private String certificationNum;

    private String headImage;
    private String userId;


}
