package com.github.mengjincn.ms365.e5.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mengjincn.ms365.e5.config.MsGraphProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;

@Service
public class ApplyTokenService extends TimerTask {
    private static final Logger logger = LoggerFactory.getLogger(ApplyTokenService.class);
    private final String TOKEN_URL;
    private RestTemplate restTemplate;
    private final String CLIENT_ID;
    private final String CLIENT_SECRET;
    private ObjectMapper objectMapper;
    private volatile String token;
    private Timer timer = new Timer(true);
    private final long period = Duration.ofMinutes(59L).toMillis();

    private HttpEntity<MultiValueMap<String, String>> requestEntity;

    public ApplyTokenService(RestTemplate restTemplate,
                             MsGraphProperties msgraphProperties,
                             ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        TOKEN_URL = String.format("https://login.microsoftonline.com/%s/oauth2/v2.0/token", msgraphProperties.getTenant());
        CLIENT_ID = msgraphProperties.getClientId();
        CLIENT_SECRET = msgraphProperties.getClientSecret();
        this.objectMapper = objectMapper;
        init();
    }

    private void init() {
        initRequestHeader();
        run();
        timer.schedule(this, period, period);
    }

    private void initRequestHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", CLIENT_ID);
        params.add("scope", "https://graph.microsoft.com/.default");
        params.add("client_secret", CLIENT_SECRET);
        params.add("grant_type", "client_credentials");
        requestEntity = new HttpEntity<>(params, headers);
    }


    private String applyToken() throws JsonProcessingException {
        ResponseEntity<String> response = restTemplate.exchange(TOKEN_URL, HttpMethod.POST, requestEntity, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            String rawJSON = response.getBody();
            try {
                return objectMapper.readTree(rawJSON).get("access_token").asText();
            } catch (JsonProcessingException e) {
                throw e;
            }
        }
        throw new RuntimeException("申请token出错");
    }

    public String getToken() {
        return token;
    }

    @Override
    public void run() {
        for (; ; ) {
            try {
                token = applyToken();
                logger.info("成功申请token.");
                return;
            } catch (Exception e) {
                logger.error("申请token出错.", e);
            }
        }
    }
}
