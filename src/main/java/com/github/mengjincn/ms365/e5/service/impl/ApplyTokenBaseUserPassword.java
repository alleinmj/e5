package com.github.mengjincn.ms365.e5.service.impl;

import com.github.mengjincn.ms365.e5.config.MsGraphProperties;
import com.github.mengjincn.ms365.e5.service.ApplyTokenService;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.core.DefaultClientConfig;
import com.microsoft.graph.core.IClientConfig;
import com.microsoft.graph.http.IHttpRequest;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class ApplyTokenBaseUserPassword extends ApplyTokenService {
    private String clientId;
    private String username;
    private String password;
    // Don't use password grant in your apps. Only use for legacy solutions and automated testing.
    private String grantType = "password";
    private String tokenEndpoint;
    private String resourceId = "https%3A%2F%2Fgraph.microsoft.com%2F";
    private String accessToken = null;

    protected IGraphServiceClient graphClient = null;

    public ApplyTokenBaseUserPassword(MsGraphProperties msgraphProperties)
    {
        tokenEndpoint = String.format("https://login.microsoftonline.com/%s/oauth2/v2.0/token", msgraphProperties.getTenant());
        clientId = msgraphProperties.getClientId();
        username = "";
        password = "";
        init();
       // GetAuthenticatedClient();
    }

    private void GetAuthenticatedClient()
    {
        if (graphClient == null) {
            try {
                accessToken = getAccessToken().replace("\"", "");
                IAuthenticationProvider mAuthenticationProvider = new IAuthenticationProvider() {
                    @Override
                    public void authenticateRequest(final IHttpRequest request) {
                        request.addHeader("Authorization",
                                "Bearer " + accessToken);
                    }
                };
                IClientConfig mClientConfig = DefaultClientConfig.createWithAuthenticationProvider(mAuthenticationProvider);

                graphClient = GraphServiceClient.fromConfig(mClientConfig);
            }
            catch (Exception e)
            {
                throw new Error("Could not create a graph client: " + e.getLocalizedMessage());
            }
        }
    }


    public String getAccessToken()
    {

        try {
            URL url = new URL(tokenEndpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            String line;
            StringBuilder jsonString = new StringBuilder();

            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);
            conn.connect();
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            String payload = String.format("grant_type=%1$s&scope=%2$s&client_id=%3$s&username=%4$s&password=%5$s",
                    grantType,
                    URLEncoder.encode("https://graph.microsoft.com/.default", "UTF-8"),
                    clientId,
                    username,
                    password);
            writer.write(payload);
            writer.close();
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while((line = br.readLine()) != null){
                    jsonString.append(line);
                }
                br.close();
            } catch (Exception e) {
                throw new Error("Error reading authorization response: " + e.getLocalizedMessage());
            }
            conn.disconnect();

            JsonObject res = new GsonBuilder().create().fromJson(jsonString.toString(), JsonObject.class);
            return res.get("access_token").toString().replaceAll("\"", "");

        } catch (Exception e) {
            throw new Error("Error retrieving access token: " + e.getLocalizedMessage());
        }
    }
}
