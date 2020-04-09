package com.github.mengjincn.ms365.e5.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplyTokenServiceTest {

    @Autowired
    private ApplyTokenService applyTokenService;

    @Test
    void getToken() {
        System.out.println("token = " + applyTokenService.getToken());
    }
}