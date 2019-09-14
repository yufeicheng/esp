package cyf.search.api.config;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;

//import org.elasticsearch.common.transport.InetSocketTransportAddress;

/**
 * @author Cheng Yufei
 * @create 2018-02-09 10:36
 *
 * 配置文件中已经配置，无需单独在进行配置client
 **/
//@Configuration
@Slf4j
public class TransConfig {


//    @Bean
    public TransportClient getClient() {
        TransportClient transportClient = null;
        Settings settings = Settings.builder().put("cluster.name", "cyf-cluster").build();

      /*  try {
            transportClient = new PreBuiltTransportClient(settings).addTransportAddresses(new InetSocketTransportAddress(InetAddress.getByName("39.106.118.71"), 9300), new InetSocketTransportAddress(InetAddress.getByName("39.106.118.71"), 9301));
        } catch (UnknownHostException e) {
            log.error("-----------------client 创建失败-----------------");
            e.printStackTrace();
        }*/
        return transportClient;
    }
}
