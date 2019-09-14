package cyf.search.api.service;

import cyf.search.base.model.template.KerrVo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Service;

/**
 * @author Cheng Yufei
 * @create 2017-12-13 15:47
 **/
@Service
public abstract class BaseService implements ElasticsearchRepository<KerrVo,Integer> {



}
