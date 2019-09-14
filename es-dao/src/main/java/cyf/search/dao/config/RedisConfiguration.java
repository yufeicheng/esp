package cyf.search.dao.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @since 1.0
 */

@Configuration
@Slf4j
@AutoConfigureBefore({PrimaryDataSourceConfiguration.class})
public class RedisConfiguration {

    public static RedisTemplate<String,String> redisTemplate;

    @Bean
    public RedisTemplate<String,String> getRedisTemplate(StringRedisTemplate template) {
        RedisConfiguration.redisTemplate = template;
        log.info("---------------- redisTemplate  注入cache ----------------");
        return template;
    }
}
