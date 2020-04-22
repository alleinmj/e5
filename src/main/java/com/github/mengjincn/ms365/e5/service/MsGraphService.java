package com.github.mengjincn.ms365.e5.service;

import com.github.mengjincn.ms365.e5.config.MsGraphProperties;
import com.microsoft.graph.concurrency.ChunkedUploadProvider;
import com.microsoft.graph.concurrency.IProgressCallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.logger.DefaultLogger;
import com.microsoft.graph.logger.LoggerLevel;
import com.microsoft.graph.models.extensions.DriveItem;
import com.microsoft.graph.models.extensions.DriveItemUploadableProperties;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.models.extensions.UploadSession;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MsGraphService {
    private static final Logger logger = LoggerFactory.getLogger(MsGraphService.class);
    private IGraphServiceClient graphClient;
    private MsGraphProperties msgraphProperties;
    @Autowired
    private RestTemplate restTemplate;
    private String userId;
    private String driveId;

    public MsGraphService(MsGraphProperties msgraphProperties, ApplyTokenService applyTokenService) {
        this.msgraphProperties = msgraphProperties;
        Optional.ofNullable(msgraphProperties.getUserId()).ifPresent(this::setUserId);
        Optional.ofNullable(msgraphProperties.getDriveId()).ifPresent(this::setDriveId);
        DefaultLogger graphLogger = new DefaultLogger();

        graphLogger.setLoggingLevel(LoggerLevel.ERROR);

        // Build a Graph client
        graphClient = GraphServiceClient.builder()
                .authenticationProvider(request -> request.addHeader("Authorization", "Bearer " + applyTokenService.getToken()))
                .logger(graphLogger)
                .buildClient();
    }

    public IGraphServiceClient getGraphClient() {
        return graphClient;
    }


    public DriveItem upload(String filename, byte[] content) {
        return graphClient.users(userId).drive().root()
                .itemWithPath(encodeFilePath(filename)).content().buildRequest()
                .put(content);
    }

    private String encodeFilePath(String filename) {
        filename = Arrays.stream(filename.split("/")).map(v -> {
            try {
                return URLEncoder.encode(v, "UTF-8").replaceAll("\\+", "%20");
            } catch (UnsupportedEncodingException e) {
                return "";
            }
        }).filter(StringUtils::isNotBlank).collect(Collectors.joining("/"));
        return filename;
    }

    public InputStream download(String itemPath) {
        return graphClient.drives(driveId).root().itemWithPath(encodeFilePath(itemPath)).content().buildRequest().get();
    }

    public DriveItem get(String itemPath){
        String path = encodeFilePath(itemPath);
        return graphClient.drives(driveId).root().itemWithPath(path).buildRequest().get();
    }



    public void delete(String itemPath) {
        graphClient.drives(driveId).root().itemWithPath(encodeFilePath(itemPath)).buildRequest().delete();
    }

    public void largeFileUpload(String itemPath, InputStream uploadFile, long fileSize) throws IOException, InterruptedException {
        itemPath = encodeFilePath(itemPath);
        IProgressCallback<DriveItem> callback = new IProgressCallback<DriveItem>() {
            @Override
            public void progress(final long current, final long max) {
                double rate = current * 100.0 / max;
                System.out.println("rate = " + rate);
            }

            @Override
            public void success(final DriveItem result) {
                //Handle the successful response
                String finishedItemId = result.id;
                System.out.println("item.name = " + result.name);
                System.out.println("item.id = " + finishedItemId);
            }

            @Override
            public void failure(final ClientException ex) {
                //Handle the failed upload
                System.out.println("Upload session failed");
            }
        };

        UploadSession uploadSession = graphClient
                .users(userId)
                .drive()
                .root()
                .itemWithPath(itemPath)
                .createUploadSession(new DriveItemUploadableProperties())
                .buildRequest()
                .post();

        ChunkedUploadProvider<DriveItem> chunkedUploadProvider = new ChunkedUploadProvider<DriveItem>(
                uploadSession,
                graphClient,
                uploadFile,
                fileSize,
                DriveItem.class);

        chunkedUploadProvider.upload(callback);
    }


    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setDriveId(String driveId) {
        this.driveId = driveId;
    }
}