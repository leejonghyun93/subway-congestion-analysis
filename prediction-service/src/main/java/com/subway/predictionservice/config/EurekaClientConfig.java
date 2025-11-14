package com.subway.predictionservice.config;

import org.springframework.cloud.netflix.eureka.http.DefaultEurekaClientHttpRequestFactorySupplier;
import org.springframework.cloud.netflix.eureka.http.RestTemplateDiscoveryClientOptionalArgs;
import org.springframework.cloud.netflix.eureka.http.RestTemplateTransportClientFactories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EurekaClientConfig {

    @Bean
    public RestTemplateDiscoveryClientOptionalArgs restTemplateDiscoveryClientOptionalArgs() {
        return new RestTemplateDiscoveryClientOptionalArgs(
                new DefaultEurekaClientHttpRequestFactorySupplier()
        );
    }

    @Bean
    public RestTemplateTransportClientFactories restTemplateTransportClientFactories(
            RestTemplateDiscoveryClientOptionalArgs args) {
        return new RestTemplateTransportClientFactories(args);
    }
}