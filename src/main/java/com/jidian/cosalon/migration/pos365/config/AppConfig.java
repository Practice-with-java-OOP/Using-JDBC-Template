package com.jidian.cosalon.migration.pos365.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

//    @Bean
//    public RestTemplate getRestTemplate() {
//        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
//        interceptors.add(new HttpHeaderInterceptor("Accept", MediaType.APPLICATION_JSON_VALUE));
//        interceptors.add(new HttpHeaderInterceptor("ss-id", "OblCExwGf0CNwNVMHFr0"/*Utils.SESSION_ID*/));
//        interceptors.add(new RequestResponseLoggingInterceptor());
//        RestTemplate restTemplate = new RestTemplate();
//        restTemplate.setInterceptors(interceptors);
////        restTemplate.setInterceptors(Collections.singletonList(new RequestResponseLoggingInterceptor()));
//        return restTemplate;
//    }
}
