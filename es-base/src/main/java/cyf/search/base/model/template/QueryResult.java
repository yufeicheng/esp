package cyf.search.base.model.template;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author Cheng Yufei
 * @create 2019-08-06 13:41
 **/
@Getter
@Setter
@NoArgsConstructor
public class QueryResult<T> {
    private List<T> list;
    private long recordnum;
    private long advisernum;
    private long mystocksnum;
    private int concernsCode;
    private String type;

}
