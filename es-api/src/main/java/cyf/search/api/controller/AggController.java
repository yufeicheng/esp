package cyf.search.api.controller;

import cyf.search.api.service.AggService;
import cyf.search.base.model.Response;
import cyf.search.base.model.template.HistogramResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Cheng Yufei
 * @create 2018-02-02 16:35
 **/
@RestController
@RequestMapping("/agg")
public class AggController {

    @Autowired
    private AggService aggService;

    /**
     *  聚合直方图: x轴：price 间隔10000，[0-9999],[10000-19999] ...
     *         y轴：各个价格区间的文档数量
     *
     *  子聚合每个区间的价格之和
     * @return
     */
    @GetMapping("/histogram")
    public Response histogram() {
        List<HistogramResult> histogramResultList = aggService.histogram();
        return new Response(histogramResultList);
    }

    /**
     * 全年每个月份的文档数及 votes sum
     * @return
     */
    @GetMapping("/dateHistogram")
    public Response dateHistogram() {
        List<HistogramResult> kerrVos = aggService.dateHistogram();
        return new Response(kerrVos);
    }

    /**
     * 全年四个季度各个city的votes及四个季度votes总和的走势
     * @return
     */
    @GetMapping("/dateHistogramQuarter")
    public Response dateHistogramQuarter() {
        List<HistogramResult> kerrVos = aggService.dateHistogramQuarter();
        return new Response(kerrVos);
    }

    /**
     *  全局桶： 搜索city为美国数据及聚合美国votes平均数及全部文档数据的votes平均数
     *
     *  1.默认情况下 聚合和查询 是相同作用域
     *  2.setQuery().addAggregation() 俩个的作用域相同 ，但使用 global 来在搜索的上下文中聚合所有的文档而不仅仅是搜索出数据的范围
     *
     * @return
     */
    @GetMapping("/globalAgg")
    public Response globalAgg(@RequestParam String searchWord, @RequestParam String field) {

        aggService.globalAgg(field,searchWord);
        return new Response();
    }

    /**
     * 过滤桶： 搜索出文档，然后过滤聚合符合条件的文档及其votes平均值
     * @param searchWord
     * @param field
     * @return
     */
    @GetMapping("/filterAgg")
    public Response filterAgg(@RequestParam String searchWord, @RequestParam String field) {

        aggService.filterAgg(field,searchWord);
        return new Response();
    }

    /**
     * 后置过滤器：
     *
     *  搜索出所有跑车；terms聚合跑车所有颜色 ；最后post_filter筛选指定颜色的跑车,post_filter在查询之后被执行，聚合结果不受影响，结果数据只显示固定颜色，但所有颜色选项仍全部展示。
     *  post_filter应该只和聚合一起使用，并且仅当你使用了不同的过滤条件时。
     *
     * @param searchWord
     * @param color
     * @return
     */
    @GetMapping("/postFilterAgg")
    public Response postFilterAgg(@RequestParam String searchWord, @RequestParam String color) {

        aggService.postFilter(searchWord,color);
        return new Response();
    }

    /**
     * 度量排序：按照聚合的平均值排序
     * @return
     */
    @GetMapping("/sort")
    public Response sort() {
        aggService.sort();
        return new Response();
    }
}
