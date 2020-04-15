package com.github.mengjincn.ms365.e5.service;

import com.microsoft.graph.models.extensions.DriveItem;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.FileCopyUtils;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MsGraphServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(MsGraphServiceTest.class);
    @Autowired
    private MsGraphService msGraphService;
    private String file = "tmp/测试ApplyToken文件.txt";

    @BeforeEach
    void upload() {
        DriveItem driveItem = msGraphService.upload(file, "文件测试，已经被修改了...".getBytes());
        System.out.println("driveItem.id = " + driveItem.id);
        System.out.println("driveItem.name = " + driveItem.name);
    }

    @Test
    void download() {
        try (Reader inputStream = new InputStreamReader(msGraphService.download(file))) {
            logger.info("receive: {}", FileCopyUtils.copyToString(inputStream));
        } catch (IOException e) {
            logger.error("download file {} with error", file, e);
        }
    }


    @AfterEach
    void delete() {
        msGraphService.delete(file);
    }
}