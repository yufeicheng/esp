package cyf.search.dao.cache;

import cyf.search.dao.config.RedisConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.Cache;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.util.Base64Utils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 如果启用缓存在 *mapper.xml中增加<cache type="com.daishumovie.dao.cache.MyBatisRedisCache"></cache>标签
 *
 * @since 1.0
 */
@Slf4j
public class MyBatisRedisCache implements Cache {

    /**
     * The ReadWriteLock.
     */
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private String id;

    //所有id集合 用于后台清理
    public static Set<String> set = new HashSet<>();

    public MyBatisRedisCache(String id) {

        log.info("MyBatisRedisCache id =========" + id);
        if (id == null) {
            throw new IllegalArgumentException("Cache instances require an ID");
        }
        this.id = id;
        set.add(id);
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void putObject(Object key, Object value) {
        String k = formatKey(key.toString());
        log.debug("redis-cache-putObject.key={},value={}", k, value);
        HashOperations<String, String, String> hashOperations = RedisConfiguration.redisTemplate.opsForHash();

        String v = Base64Utils.encodeToString(SerializationUtils.serialize((Serializable) value));

        hashOperations.put(id, k, v);
    }

    @Override
    public Object getObject(Object key) {
        String k = formatKey(key.toString());
        log.debug("redis-cache-getObject.key={}", k);
        HashOperations<String, String, String> hashOperations = RedisConfiguration.redisTemplate.opsForHash();
        String value = hashOperations.get(id, k);

        if (value == null) {
            return null;
        }
        return SerializationUtils.deserialize(Base64Utils.decodeFromString(value));
    }

    @Override
    public Object removeObject(Object key) {
        String k = formatKey(key.toString());
        HashOperations<String, String, String> hashOperations = RedisConfiguration.redisTemplate.opsForHash();
        Long sum = hashOperations.delete(id, k);
        log.debug("redis-cache-removeObject,key={},sum={}", k, sum);
        return sum;
    }

    @Override
    public void clear() {
        log.debug("redis-cache-clear,id={}", id);
//		log.debug("getSize={},id={}",getSize(),id);
        RedisConfiguration.redisTemplate.delete(id);
//        log.debug("getSize={},id={}",getSize(),id);
    }


    @Override
    public int getSize() {

        HashOperations<String, String, String> hashOperations = RedisConfiguration.redisTemplate.opsForHash();
        Long size = hashOperations.size(id);
        return Math.toIntExact(size);
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }


    private String formatKey(Object key) {
        return key.toString().replace("\t", "").replace("\n", "").replace(" ", "");
    }

}
