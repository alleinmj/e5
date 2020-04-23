package com.github.mengjincn.ms365.e5.service;

import com.github.javafaker.Faker;
import com.github.javafaker.service.FakeValuesService;
import com.github.javafaker.service.RandomService;
import com.github.mengjincn.ms365.e5.model.Book;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.file.Files.readAllLines;

public class FakeServiceTest {
    @Test
    public void fakeService() {
        FakeValuesService fakeValuesService = new FakeValuesService(
                new Locale("en-GB"), new RandomService());

        for (int i = 0; i < 100; i++) {
            String email = fakeValuesService.bothify("????##@gmail.com");
            System.out.println("email = " + email);
            String alphaNumericString = fakeValuesService.regexify("[a-z1-9]{10}");
            System.out.println("alphaNumericString = " + alphaNumericString);
        }

        for (int i = 0; i < 100; i++) {
            Faker faker = new Faker(Locale.CHINA);

            String streetName = faker.address().streetName();
            String number = faker.address().buildingNumber();
            String city = faker.address().city();
            String country = faker.address().country();
            String zipCode = faker.address().zipCode();

            System.out.println(String.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s",
                    number,
                    streetName,
                    city,
                    country,
                    zipCode,
                    faker.name().firstName(),
                    faker.name().lastName(),
                    faker.name().fullName()));
        }

    }

    @Test
    void file() {
        Faker faker = new Faker(Locale.CHINA);

        randomContent(faker);
    }

    public byte[] randomContent(Faker faker) {
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

    @Test
    public void books() throws Exception {
        List<String> allLines = Files.readAllLines(Paths.get("/Users/mengjin/Documents/workspaces/e5/src/main/resources/books"));
        final List<Book> books = new ArrayList<>(allLines.size());
        allLines.forEach(line -> {
            String[] tmp = line.split("\\|");
            books.add(new Book(tmp[0], tmp[1]));
        });

        System.out.println(books.size());
    }

    @Test
    public void string() {
        String s = "zhongguop:wenhua :    zuoye   测试：zhognwen=hello.pdf";
        System.out.println(s.replaceAll(":|：", "-"));


        String name = s.replaceAll(":|：", "-");
        String url = "http://localhost/hello.pdzx";
        name = url.endsWith(".pdf") ? name : name.substring(0, name.lastIndexOf(".")) + url.substring(url.lastIndexOf("."));

        name = name.replaceAll("=| ", "");

        System.out.println("name = " + name);
    }

    @Test
    public void list() {
        Deque<String> strings = new ConcurrentLinkedDeque<>();

        strings.add("one");
        strings.add("two");
        strings.add("three");


        int i=1;
        AtomicInteger integer = new AtomicInteger(0);
        for (String string = strings.poll(), last = strings.peekLast();
             string != null;
             string = strings.poll()) {

            if(integer.get() >=2){
                break;
            }

            if(last==string){
                integer.incrementAndGet();
                if(strings.peekLast()!=null){
                    last = strings.peekLast();
                }
            }


            if ("two".equals(string)) {
                System.out.println(i++);
                strings.add(string);
                continue;
            }
            System.out.println("string = " + string);
        }

        System.out.println("strings = " + strings);
    }

}
