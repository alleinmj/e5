package com.github.mengjincn.ms365.e5.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("msgraph")
public class MsGraphProperties {
    private String tenant;
    private String clientId;
    private String clientSecret;
    private String userId;
    private String driveId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDriveId() {
        return driveId;
    }

    public void setDriveId(String driveId) {
        this.driveId = driveId;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
