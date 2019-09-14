package cyf.search.base.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Cheng Yufei
 * @create 2018-02-02 16:35
 **/
public class PageInDto implements Serializable{

    private @Getter @Setter int pageIndex;
    private @Getter int pageSize;

    public PageInDto(int pageIndex) {
        this.pageIndex = pageIndex;
        this.pageSize = 10;
    }

    public PageInDto(int pageIndex, int pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }

    public int getOffset() {
        return (pageIndex - 1) * pageSize;
    }

    public int getLimit() {
        return pageSize;
    }
}
