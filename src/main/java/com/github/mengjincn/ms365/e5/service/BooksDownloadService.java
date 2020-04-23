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
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

@Service
public class BooksDownloadService {
    private static final Logger logger = LoggerFactory.getLogger(BooksDownloadService.class);
    private Deque<Book> books;
    private Set<String> error = new HashSet<>();
    private MsGraphService msGraphService;
    private static final int MAX_TIMES = 10;

    public BooksDownloadService(@Value("${book.path}") String bookPath, MsGraphService msGraphService) {
        this.msGraphService = msGraphService;
        try {
            books = new ConcurrentLinkedDeque<>();
            List<String> allLines = Files.readAllLines(Paths.get(bookPath));
            allLines.forEach(line -> {
                String[] tmp = line.split("\\|");
                String name = tmp[0].replaceAll(":|：", "-");
                final String url = tmp[1];
                name = url.endsWith(".pdf") ? name : name.substring(0, name.lastIndexOf(".")) + url.substring(url.lastIndexOf("."));
                int index = url.lastIndexOf('_');
                name = index > 0 ? name.substring(0, name.lastIndexOf(".")) + url.substring(index) : name;
                name = name.replaceAll("=| ", "");
                books.add(new Book(name, url));
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
                    TimeUnit.MINUTES.sleep(45);
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void uploadBookInBestEffort() {
        int times = 0;
        for (Book last = books.peekLast(), book = books.poll();
             book != null && last != null;
             book = books.poll()) {
            if (times >= MAX_TIMES) {
                break;
            }
            if (!uploadSingleBook(book)) {
                books.addLast(book);
            } else {
                try {
                    TimeUnit.MINUTES.sleep(45);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (last == book) {
                times++;
                last = books.peekLast();
            }
        }
        logger.info("*************************** All books has been upload to OneDrive ***************************");
    }

    /**
     * 上传单个书籍
     *
     * @param book
     * @return 上传成功返回true；否则返回false；
     */
    private boolean uploadSingleBook(Book book) {
        InputStream inputStream = null;
        try {
            String filePath = "books/" + book.getName();
            try {
                // 文件如果已存在则返回成功，不再上传
                msGraphService.get(filePath);
                return true;
            } catch (GraphServiceException e) {
                Resource resource = new UrlResource(book.getUrl());
                inputStream = resource.getInputStream();
                msGraphService.largeFileUpload(filePath, inputStream, resource.contentLength());
                return true;
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
