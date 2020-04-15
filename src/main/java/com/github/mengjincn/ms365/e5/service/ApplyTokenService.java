package com.github.mengjincn.ms365.e5.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;


public abstract class ApplyTokenService extends TimerTask {
    private static final Logger logger = LoggerFactory.getLogger(ApplyTokenService.class);
    private volatile String token;
    private Timer timer = new Timer(true);
    private final long period = Duration.ofMinutes(59L).toMillis();

    public ApplyTokenService() {
    }

    protected void init() {
        run();
        timer.schedule(this, period, period);
    }

    public String getToken() {
        return token;
    }

    @Override
    public void run() {
        for (; ; ) {
            try {
                token = getAccessToken();
                logger.info("成功申请token.");
                return;
            } catch (Exception e) {
                logger.error("申请token出错.", e);
            }
        }
    }

    protected abstract String getAccessToken() ;
}
