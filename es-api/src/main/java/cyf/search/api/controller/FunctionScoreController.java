package cyf.search.api.controller;

import cyf.search.api.service.FunctionScoreService;
import cyf.search.base.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Cheng Yufei
 * @create 2018-03-03 上午9:35
 **/
@RestController
@RequestMapping("/function")
public class FunctionScoreController {

    @Autowired
    private FunctionScoreService functionScoreService;


    /**
     * field_value_factor: 对文档重新打分，使用文档中某个字段的值来改变_score
     *
     * @param searchField
     * @param searchWord
     * @param functionField
     * @return
     */
    @GetMapping("/fieldValue")
    public Response fieldValue(@RequestParam String searchField, @RequestParam String[] searchWord, @RequestParam String functionField) {

        functionScoreService.fieldValue(searchField, searchWord, functionField);
        return new Response();
    }

    /**
     * 对每份文档适用一个简单的提升,weight 值被原样使用
     *
     * @param searchField
     * @param searchWord
     * @return
     */
    @GetMapping("/weight")
    public Response weight(@RequestParam String searchField, @RequestParam String[] searchWord) {

        functionScoreService.weight(searchField, searchWord);
        return new Response();
    }

    /**
     * 利用 高斯函数影响搜索结果分值，origin 为中心offset范围内权重为1，距离offset值的scale值内权重逐渐降为decay
     * 查询出所有数据，用votes 浮动值（用户可接受的范围内筛选数据）考虑到_score 中；
     * <p>
     * 仅支持数字，日期和地理位置字段。
     *
     * @return
     */
    @GetMapping("/gauss")
    public Response gauss() {

        functionScoreService.gauss();
        return new Response();
    }

    /**
     * 自定义函数影响_score
     *
     * @return
     */
    @GetMapping("/scrip")
    public Response scrip() {

        functionScoreService.scrip();
        return new Response();
    }
}
