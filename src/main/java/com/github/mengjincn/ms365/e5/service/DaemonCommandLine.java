package com.github.mengjincn.ms365.e5.service;

import com.github.javafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Component
public class DaemonCommandLine implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DaemonCommandLine.class);
    @Autowired
    private Faker faker;
    @Autowired
    private MsGraphService msGraphService;
    @Autowired
    private BooksDownloadService booksDownloadService;

    @Override
    public void run(String... args) {
        List<String> fileNames = new CopyOnWriteArrayList<>();
        new Thread(()->{
            try {
                TimeUnit.MINUTES.sleep(ThreadLocalRandom.current().nextInt(40,70));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String file = fileNames.get(ThreadLocalRandom.current().nextInt(0, fileNames.size()));
            msGraphService.delete(file);
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                booksDownloadService.processBooks();
            }
        }).start();
        while (true) {
            try {
                String file = "tmp/" + faker.file().fileName();
                msGraphService.upload(file, randomContent(faker));
                logger.info("create file: {}", file);
                fileNames.add(file);
                TimeUnit.MINUTES.sleep(ThreadLocalRandom.current().nextInt(30, 60));
                try (InputStream inputStream = msGraphService.download(file);
                     ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                    int readSize = 0;
                    byte[] buffer = new byte[4096];
                    while ((readSize = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, readSize);
                    }
                    logger.info("receive: {}", outputStream.toString("UTF-8"));
                } catch (IOException e) {
                    logger.error("download file {} with error", file, e);
                    msGraphService.delete(file);
                }
            } catch (Exception e) {
                logger.error("run msgraph with error: ", e);
            }
        }
    }

    byte[] randomContent(Faker faker) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            stringBuilder.append(faker.shakespeare().asYouLikeItQuote());
            stringBuilder.append(System.lineSeparator());
            stringBuilder.append(faker.shakespeare().hamletQuote());
            stringBuilder.append(System.lineSeparator());
            stringBuilder.append(faker.shakespeare().kingRichardIIIQuote());
            stringBuilder.append(System.lineSeparator());
            stringBuilder.append(faker.shakespeare().romeoAndJulietQuote());

            for (int j = 0; j < 100; j++) {
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append(faker.gameOfThrones().quote());
            }
        }

        return stringBuilder.toString().getBytes(StandardCharsets.UTF_8);
    }
}
