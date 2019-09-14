package cyf.search.base.model.template;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Cheng Yufei
 * @create 2019-08-06 17:33
 **/
@Getter
@Setter
@NoArgsConstructor
public class StockBucket {
    private String stockCode;
    private String stockName;
    private String key;
    private Long docCount;
}
