package cyf.search.base.model.template;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Cheng Yufei
 * @create 2018-02-27 17:35
 **/
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HistogramResult {

//    private double key;
    private Object key;

    private long docCount;

    /**
     * (date)histogram 聚合每个区间的 sum
      */
    private double sum;

    public HistogramResult(Object key, long docCount) {
        this.key = key;
        this.docCount = docCount;
    }
}
