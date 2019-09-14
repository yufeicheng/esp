package cyf.search.base.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @since 1.0
 */
@Setter
@Getter
@ToString
public class Header {

    private String did;

    private String sessionId;

    private Integer uid;

    private String version;
    private String screen;
    private String networkType;
    private String language;
    private String device;
    private String os;
    private String osVersion;

    private OsEnum osEnum;


    public OsEnum getOsEnum() {
        return OsEnum.get(this.os);
    }


    public enum OsEnum{
        IOS(1), Android(2)
        ;

        @Getter
        private Integer os;

        OsEnum(Integer os){
            this.os = os;
        }

        public static OsEnum get(String name){
            OsEnum[] values = values();
            for (OsEnum value : values) {
                if(value.name().equalsIgnoreCase(name)){
                    return value;
                }
            }
            return null;
        }

    }



}
