package com.github.mengjincn.ms365.e5.service;

import com.github.mengjincn.ms365.e5.model.Book;
import com.microsoft.graph.http.GraphServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class BooksDownloadService {
    private static final Logger logger = LoggerFactory.getLogger(BooksDownloadService.class);
    private List<Book> books;
    private List<String> error = new ArrayList<>();
    private MsGraphService msGraphService;

    public BooksDownloadService(@Value("${book.path}") String bookPath, MsGraphService msGraphService) {
        this.msGraphService = msGraphService;
        try {
            books = new ArrayList<>();
            List<String> allLines = Files.readAllLines(Paths.get(bookPath));
            allLines.forEach(line -> {
                String[] tmp = line.split("\\|");
                books.add(new Book(tmp[0], tmp[1]));
            });
        } catch (Exception e) {
            logger.error("read books name and url has error: ", e);
        }
    }


    public void processBooks() {
        books.forEach(book -> {
            InputStream inputStream = null;
            try {
                String filePath = "books/" + book.getName();
                try {
                    msGraphService.get(filePath);
                } catch (GraphServiceException e) {
                    Resource resource = new UrlResource(book.getUrl());
                    inputStream = resource.getInputStream();
                    msGraphService.largeFileUpload(filePath, inputStream, resource.contentLength());
                }
            } catch (Exception e) {
                logger.error("download {} has error", book.getUrl());
                error.add(book.getName() + "|" + book.getUrl());
                try {
                    Files.write(Paths.get("/tmp/error.txt"), error);
                } catch (IOException ex) {
                    logger.error("write error list: ", ex);
                }
            } finally {
                try {
                    if (inputStream != null) inputStream.close();
                    TimeUnit.MINUTES.sleep(45);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
    }
}
