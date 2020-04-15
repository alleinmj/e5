package com.github.mengjincn.ms365.e5.config;

import com.github.javafaker.Faker;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Locale;

@Configuration
@EnableConfigurationProperties({MsGraphProperties.class})
public class MsGraphConfiguration {
    @Bean
    public Faker faker(){
        return new Faker(Locale.CHINA);
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder().setConnectTimeout(Duration.ofSeconds(30)).setReadTimeout(Duration.ofMinutes(10)).build();
    }
}
