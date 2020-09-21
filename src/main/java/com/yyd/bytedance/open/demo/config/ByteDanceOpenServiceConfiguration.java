package com.yyd.bytedance.open.demo.config;

import com.github.yydzxz.common.redis.IByteDanceRedisOps;
import com.github.yydzxz.common.redis.RedissonByteDanceRedisOps;
import com.github.yydzxz.common.service.IByteDanceHttpRequestService;
import com.github.yydzxz.common.service.impl.RestTemplateByteDanceHttpRequestServiceImpl;
import com.github.yydzxz.open.api.IByteDanceOpenService;
import com.github.yydzxz.open.api.impl.ByteDanceOpenInRedisConfigStorage;
import com.github.yydzxz.open.api.impl.ByteDanceOpenServiceImpl;
import com.github.yydzxz.open.api.v1.impl.ByteDanceOpenV1ComponentServiceImpl;
import com.github.yydzxz.open.api.v2.impl.ByteDanceOpenV2ComponentServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * ByteDanceOpenServiceConfiguaration
 *
 * @author Clevo
 * @date 2020/7/12
 */
@Slf4j
@Configuration
public class ByteDanceOpenServiceConfiguration {

    @Autowired
    private ByteDanceOpenProperties byteDanceOpenProperties;

    /**
     * <b>使用resttemplate + jackson</b>
     * new RestTemplateByteDanceHttpRequestServiceImpl(restTemplate, new JacksonSerializer());
     * <b>使用OkHttpClient + gson</b>
     * new OkHttpClientByteDanceHttpRequestServiceImpl(new GsonSerializer());
     * <b>使用resttemplate和默认的json工具（根据pom中引用的jar包自动选择）</b>
     * new RestTemplateByteDanceHttpRequestServiceImpl(restTemplate);
     * <b>使用 okhttpclient和默认的json工具（根据pom中引用的jar包自动选择</b>
     * new OkHttpClientByteDanceHttpRequestServiceImpl()
     * @return
     */
    @Bean
    public IByteDanceHttpRequestService getByteDanceHttpRequestService(RestTemplate restTemplate){
        return new RestTemplateByteDanceHttpRequestServiceImpl(restTemplate);
    }

    @Bean
    public IByteDanceRedisOps getByteDanceRedisOps(RedissonClient redissonClient){
        return new RedissonByteDanceRedisOps(redissonClient);
    }

    @Bean
    public ByteDanceOpenInRedisConfigStorage getByteDanceOpenInRedisConfigStorage(IByteDanceRedisOps byteDanceRedisOps){
        ByteDanceOpenInRedisConfigStorage byteDanceOpenInRedisConfigStorage = new ByteDanceOpenInRedisConfigStorage(byteDanceRedisOps, "yourprefix");
        if("yourprefix".equals(byteDanceOpenInRedisConfigStorage.getKeyPrefix())){
            log.warn("请设置自己的redis前缀");
        }
        byteDanceOpenInRedisConfigStorage.setComponentAppId(byteDanceOpenProperties.getComponentAppId());
        byteDanceOpenInRedisConfigStorage.setComponentAppSecret(byteDanceOpenProperties.getComponentSecret());
        byteDanceOpenInRedisConfigStorage.setComponentToken(byteDanceOpenProperties.getComponentToken());
        byteDanceOpenInRedisConfigStorage.setComponentAesKey(byteDanceOpenProperties.getComponentAesKey());
        return byteDanceOpenInRedisConfigStorage;
    }

    /**
     * v1 不支持预设置分账比例 new ByteDanceOpenV1ComponentServiceImpl(byteDanceOpenService)
     * v2 支持预设置分账比例（建议使用） new ByteDanceOpenV2ComponentServiceImpl(byteDanceOpenService)
     * @param byteDanceHttpRequestService
     * @param byteDanceRedisOps
     * @param byteDanceOpenInRedisConfigStorage
     * @return
     */
    @Bean
    public IByteDanceOpenService getIByteDanceOpenService(IByteDanceHttpRequestService byteDanceHttpRequestService,
        IByteDanceRedisOps byteDanceRedisOps, ByteDanceOpenInRedisConfigStorage byteDanceOpenInRedisConfigStorage){
        IByteDanceOpenService byteDanceOpenService = new ByteDanceOpenServiceImpl();
        byteDanceOpenService.setByteDanceHttpRequestService(byteDanceHttpRequestService);
        byteDanceOpenService.setByteDanceRedisOps(byteDanceRedisOps);

        byteDanceOpenService.setByteDanceOpenV1ComponentService(new ByteDanceOpenV1ComponentServiceImpl(byteDanceOpenService));
        byteDanceOpenService.setByteDanceOpenV2ComponentService(new ByteDanceOpenV2ComponentServiceImpl(byteDanceOpenService));

        byteDanceOpenService.setByteDanceOpenConfigStorage(byteDanceOpenInRedisConfigStorage);
        return byteDanceOpenService;
    }
}
