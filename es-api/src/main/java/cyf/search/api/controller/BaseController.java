package cyf.search.api.controller;

import com.google.common.collect.Lists;
import cyf.search.api.service.ESIndexService;
import cyf.search.api.service.MatchService;
import cyf.search.base.enums.IndexType;
import cyf.search.base.model.Response;
import cyf.search.base.model.template.KerrVo;
import cyf.search.base.model.template.Name;
import cyf.search.dao.mapper.EmployeesMapper;
import cyf.search.dao.mapper.KerrMapper;
import cyf.search.dao.model.Employees;
import cyf.search.dao.model.Kerr;
import cyf.search.dao.model.KerrExample;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.FastDateFormat;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.index.query.BoostingQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.List;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

/**
 * @author Cheng Yufei
 * @create 2017-12-12 15:03
 **/
@RestController
@RequestMapping("/base")
@Slf4j
public class BaseController {

   /* @Resource
    private BaseService baseService;*/

    @Resource
    private KerrMapper kerrMapper;
    @Resource
    private EmployeesMapper employeesMapper;
    @Resource
    private ElasticsearchTemplate elasticsearchTemplate;
    @Resource
    private MatchService matchService;
    @Autowired
    private ESIndexService indexService;

    @RequestMapping(value = "/saveAllKerr2")
    public Response saveAll() throws IOException {

        KerrExample example = new KerrExample();
        List<Kerr> kerr2List = kerrMapper.selectByExample(example);
        List<IndexQuery> indexQueries = Lists.newArrayList();
        List<KerrVo> kerr2VoList = Lists.newArrayList();
        kerr2List.forEach(k -> {
            KerrVo vo = new KerrVo();
            BeanUtils.copyProperties(k, vo);
            IndexQuery indexQuery = new IndexQuery();
            indexQuery.setObject(vo);
            indexQueries.add(indexQuery);
            kerr2VoList.add(vo);
        });

        elasticsearchTemplate.bulkIndex(indexQueries);
        //baseService.saveAll(kerr2VoList);
        return new Response();

    }

    /**
     * 数据存储
     * @return
     * @throws IOException
     * @throws ParseException
     */
    @RequestMapping(value = "/saveAllKerr")
    public Response saveAllKerr() throws IOException, ParseException {

        List<Kerr> kerrs = kerrMapper.selectByExample(new KerrExample());
        List<KerrVo> kerrVoList = Lists.newArrayList();
        kerrs.forEach(k -> {
            KerrVo kerrVo = new KerrVo();
            BeanUtils.copyProperties(k, kerrVo);
            Employees employees = employeesMapper.selectByPrimaryKey(k.getEmployeesId());
            //kerrVo.setEmployees(employees);

            Name name = new Name();
            name.setFirst(k.getFirstName());
            name.setLast(k.getLastName());
            kerrVo.setName(name);

            //mapping中设置date类型，但在传输实体中的时间需设置成String 类型，从数据库读出（cst时间）格式化为mapping中的所需格式,
            // 若传输实体设为date类型则在es中为毫秒数
            kerrVo.setPublishtime(FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").format(k.getPublishtime()));
            kerrVoList.add(kerrVo);

        });
        //baseService.saveAll(kerrVoList);
        return new Response();
    }

    /**
     * 更新单个数据 (use)
     *
     * @param id
     * @return
     */
    @PostMapping("/update/{id}")
    public Response update(@PathVariable String id, @RequestParam String field, @RequestParam String element, HttpServletRequest request) throws UnsupportedEncodingException {
        matchService.update(id, field, element);
        return new Response();
    }

    @RequestMapping(value = "/getById/{id}")
    public Response getById(@PathVariable Integer id) throws IOException {
       /* Optional<KerrVo> optional = baseService.findById(id);
        KerrVo kerr2Vo = optional.get();
        log.debug("查询结果：{}", Optional.ofNullable(JSON.toJSONString(kerr2Vo)).orElse("未找到结果"));
        return new Response(kerr2Vo);*/
        return new Response();
    }

    @RequestMapping(value = "/update")
    public Response getById() throws IOException {

        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.index("miranda");
        updateRequest.type("kerr");
        updateRequest.id("1");
        updateRequest.doc(jsonBuilder().startObject().field("_id", 1).field("title", "学习目标:elasticsearch").endObject());


        UpdateQuery updateQuery = new UpdateQuery();
        updateQuery.setIndexName(IndexType.kerr.getIndex());
        updateQuery.setType(IndexType.kerr.getType());
        updateQuery.setUpdateRequest(updateRequest);

        elasticsearchTemplate.update(updateQuery);

        return new Response();
    }

    /**
     * 理论获取
     *
     * @param keyword
     * @param weigth
     * @param pageable
     * @return
     */
    @RequestMapping(value = "/search")
    public Response search(@RequestParam String keyword, @RequestParam Integer weigth, @PageableDefault(sort = "weight", direction = Sort.Direction.DESC) Pageable pageable) {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchQuery("field", keyword).operator(Operator.AND)
                .minimumShouldMatch("70%")).withSort(SortBuilders.fieldSort(""))
                .withHighlightFields(new HighlightBuilder.Field[]{new HighlightBuilder.Field("")}).withPageable(pageable);
        SearchQuery searchQuery = nativeSearchQueryBuilder.build();


// 搜索苹果，将食物类的文档降低分值利用 boostingquery 放入（negativeQuery 降低 negativeBoost）
        BoostingQueryBuilder boostingQueryBuilder = new BoostingQueryBuilder(QueryBuilders.matchQuery("", keyword), QueryBuilders.matchQuery("", ""));


        // function_score 组合
        FunctionScoreQueryBuilder.FilterFunctionBuilder[] filterFunctionBuilders = new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{new FunctionScoreQueryBuilder.FilterFunctionBuilder(ScoreFunctionBuilders.fieldValueFactorFunction("vote").modifier(FieldValueFactorFunction.Modifier.LOG1P).factor(2.0f)),

                new FunctionScoreQueryBuilder.FilterFunctionBuilder(ScoreFunctionBuilders.randomFunction(""))};


        NativeSearchQueryBuilder natives = new NativeSearchQueryBuilder().withQuery(QueryBuilders.boolQuery().should(QueryBuilders.rangeQuery("weight").lt(weigth))).withQuery(QueryBuilders.boolQuery().filter(QueryBuilders.termQuery("", ""))).withQuery(QueryBuilders.disMaxQuery().tieBreaker(0.3f)).withQuery(QueryBuilders.functionScoreQuery(filterFunctionBuilders)).withQuery(QueryBuilders.boostingQuery(boostingQueryBuilder.positiveQuery(), boostingQueryBuilder.negativeQuery()).negativeBoost(0.5f)).withFilter(QueryBuilders.rangeQuery("price").gt("2000"));


        elasticsearchTemplate.queryForList(searchQuery, KerrVo.class);
        return new Response();
    }

    @GetMapping("/practice")
    public Response practice() {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(QueryBuilders.matchQuery("features", "跑车 手机").operator(Operator.OR))
                .withSourceFilter(new FetchSourceFilter(new String[]{"title","features","id","price"},new String[]{}))
                .withHighlightFields(new HighlightBuilder.Field[]{new HighlightBuilder.Field("features")})
                .build();

        String queryDsl = searchQuery.getQuery().toString();
        HighlightBuilder.Field[] fields = searchQuery.getHighlightFields();
        log.debug("DSL:{}", fields);
        List<KerrVo> kerrVos = elasticsearchTemplate.queryForList(searchQuery, KerrVo.class);

        return new Response(kerrVos);

    }

    @GetMapping("/savePoetry")
    public String savePoetry(@RequestParam Integer id) throws IOException {
        indexService.sendTypeToIndexForPoetry();
        return "success";
    }
}
