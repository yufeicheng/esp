package cyf.search.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import cyf.search.base.enums.IndexType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.ScrollableHitSource;
import org.elasticsearch.index.reindex.UpdateByQueryAction;
import org.elasticsearch.index.reindex.UpdateByQueryRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ESUpdateService {


    @Resource
    private TransportClient client;

    /**
     * updata某个索引中的某个类型的某些field的方法
     *
     * @param indexName                       索引名
     * @param typeName                        类型名
     * @param id                              文件id
     * @param fieldsAndData:字段数据key是fieldName value是该字段的新数据
     * @return 修改结果json
     * <p>
     * 传参示例： [user为复合结构]
     * HashMap<String, Object> map = new HashMap<>();
     * map.put("content","更新ok" );
     * map.put("stocks", new String[]{"平安银行|000001"});
     * User user = new User();
     * user.setCity("上海");
     * user.setCompany("jrj");
     * map.put("user", JSONObject.toJSON(user));
     */

    public String updataDocByIndexAndTypeAndFields(String indexName, String typeName, String id, Map<String, Object> fieldsAndData) {

        if (StringUtils.isAnyEmpty(indexName, typeName, id) || MapUtils.isEmpty(fieldsAndData)) {
            return "{" + "\"retCode\":" + -1 + ",\"msg\":\"参数错误\"}";
        }
        UpdateRequestBuilder updateRequestBuilder = client.prepareUpdate(indexName, typeName, id).setDoc(fieldsAndData);
        UpdateResponse updateResponse = updateRequestBuilder.get();
        RestStatus status = updateResponse.status();
        if (status.getStatus() != RestStatus.OK.getStatus()) {
            ReplicationResponse.ShardInfo shardInfo = updateResponse.getShardInfo();
            ReplicationResponse.ShardInfo.Failure[] failures = shardInfo.getFailures();
            log.error("更新出错：{}", failures);
            return "error";
        }
        log.info("更新完成：{}", JSON.toJSONString(updateResponse));
        return "success";
    }



    public String updateByQuery(String[] ids, boolean auditFlag) {

        Script script = new Script(ScriptType.INLINE, "painless", "ctx._source.auditFlag=params.auditFlag;ctx._source.updatedAt=params.updatedAt", ImmutableMap.of("auditFlag", auditFlag,
                "updatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));

        UpdateByQueryRequestBuilder requestBuilder = UpdateByQueryAction.INSTANCE.newRequestBuilder(client).filter(QueryBuilders.termsQuery("_id", ids))
                .script(script)
                .source(IndexType.kerr.getIndex());

        BulkByScrollResponse response = requestBuilder.get();

        List<BulkItemResponse.Failure> bulkFailures = response.getBulkFailures();
        List<ScrollableHitSource.SearchFailure> searchFailures = response.getSearchFailures();
        if (!bulkFailures.isEmpty() || !searchFailures.isEmpty()) {
            log.error("更新数据失败，bulkFailures:{},searchFailures:{}", bulkFailures.toString(), searchFailures.toString());
            return "更新数据失败";
        }
        return "success";
    }

}
