package cyf.search.api.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import cyf.search.api.service.UpgradeService;
import cyf.search.base.model.template.AskSearchVO4Web;
import cyf.search.base.model.template.QueryResult;
import cyf.search.base.model.template.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * @author Cheng Yufei
 * @create 2019-08-06 10:38
 **/
@RestController
@RequestMapping("/test")
public class UpgradeController {

    @Resource
    private UpgradeService upgradeService;

    @GetMapping("/sendMq")
    public QueryResult<JSONObject> sendMq() {
        QueryResult<JSONObject> result = upgradeService.concern(Sets.newHashSet("150106010008993769"), Sets.newHashSet("深天地A|000023"), 0, 2);
        return result;
    }

    @GetMapping("/getById/{id}")
    public String getById(@PathVariable String id) {
        return upgradeService.getById(id);
    }

    @GetMapping("/getAsk")
    public JSONObject getAsk() {
        return upgradeService.getAsk();
    }

    @GetMapping("/update/{index}/{type}/{id}")
    public String update(@PathVariable String index, @PathVariable String type, @PathVariable String id) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("content", "更新ok");
        map.put("stocks", new String[]{"平安银行|000001"});
        User user = new User();
        user.setCity("上海");
        user.setCompany("it");
        map.put("user", JSONObject.toJSON(user));
        return upgradeService.updataDocByIndexAndTypeAndFields(index, type, id, map);
    }

    @GetMapping("/getByAgg")
    public QueryResult<JSONObject> getByAgg() {
       /* QueryResult<JSONObject> result = new QueryResult<>();
        result.setList(Collections.emptyList());
        return result;*/
      /*  ArrayList<String> list = Lists.newArrayList("A", "B","C");
        List<JSONObject> res = new ArrayList<>();
         list.stream().forEach(li -> {
            if (!StringUtils.equalsAny(li, "A", "B")) {
                return;
            }
            HashMap<String, String> map = new HashMap<>();
            map.put("A", "aa");
            map.put("B", "bb");
             res.add(JSONObject.parseObject(JSON.toJSONString(map)));

        });
        QueryResult<JSONObject> result = new QueryResult<>();
        result.setList(res);
        return result;*/
        return upgradeService.getByAgg(Sets.newHashSet("000002", "000001", "002064"), "id");
    }

    @GetMapping("/getAskWeb")
    public QueryResult<AskSearchVO4Web> getAskWeb() {
        String keyword = "上证指数(000001)" + "," + "回电话" + "," + "哈哈哈" + "," + "更新ok";
        return upgradeService.searchAsk4Web(keyword,0,10,true,true);

    }
    @GetMapping("/bulkAdd")
    public JSONObject bulkAdd() {
        return upgradeService.bulkAdd();

    }

    @GetMapping("/delete")
    public JSONObject delete() {
         //upgradeService.delete();
         upgradeService.bulkJson();
        return new JSONObject();
    }

    @GetMapping("/searchSBUserByInfo/{money}")
    public JSONObject searchSBUserByInfo(@PathVariable long money) {
        upgradeService.searchSBUserByInfo(money);
        return new JSONObject();
    }
}
