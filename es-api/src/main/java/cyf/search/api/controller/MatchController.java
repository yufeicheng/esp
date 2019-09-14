package cyf.search.api.controller;

import cyf.search.api.service.MatchService;
import cyf.search.base.model.PageInDto;
import cyf.search.base.model.Response;
import cyf.search.dao.model.Kerr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Cheng Yufei
 * @create 2018-02-02 16:35
 **/
@RestController
@RequestMapping("/match")
public class MatchController {

    @Autowired
    private MatchService matchService;

    /**
     * matchQuery
     *
     * @param pageIndex
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IOException
     */
    @GetMapping("/matchQuery/{pageIndex}")
    public Response trans(@PathVariable int pageIndex, String field, String searchWord) throws ExecutionException, InterruptedException, IOException {

        PageInDto pageInDto = new PageInDto(pageIndex, 1);
        List<Kerr> search = matchService.matchQuery(pageInDto, field, searchWord);
        return new Response(search);
    }

    /**
     * 解析查询字符串生成词条列表，查询包含了所有搜索词条的文档，并且词条的位置要邻接 - 匹配比较严格
     *
     * @param pageIndex
     * @param searchWord
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IOException
     */
    @GetMapping("/matchPhraseQuery/{pageIndex}/{slop}")
    public Response matchPhraseQuery(@PathVariable int pageIndex, @PathVariable int slop, String field, String searchWord) throws ExecutionException, InterruptedException, IOException {

        PageInDto pageInDto = new PageInDto(pageIndex, 2);
        List<Kerr> kerrs = matchService.matchPhraseQuery(pageInDto, field, searchWord, slop);
        return new Response(kerrs);
    }

    /**
     * @param pageIndex
     * @param searchWord
     * @param mulType : 1 best 2 most 3 cross
     * @param operaType 1 and
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws IOException
     */
    @GetMapping("/multiMatchQuery/{mulType}/{operaType}/{pageIndex}")
    public Response multiMatchQuery(@PathVariable int pageIndex, @PathVariable int mulType, @PathVariable int operaType, String searchWord, String[] fields) throws ExecutionException, InterruptedException, IOException {

        PageInDto pageInDto = new PageInDto(pageIndex, 2);
        List<Kerr> kerrs = matchService.multiMatchQuery(pageInDto, searchWord, fields,mulType,operaType);
        return new Response(kerrs);
    }

}
