package com.github.mengjincn.ms365.e5.service;

import com.github.javafaker.Faker;
import com.github.javafaker.service.FakeValuesService;
import com.github.javafaker.service.RandomService;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    public byte[] randomContent(Faker faker){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i=0; i<10; i++) {
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
    public void books() throws Exception{
        List<String> allLines = Files.readAllLines(Paths.get("/Users/mengjin/Documents/workspaces/e5/src/main/resources/books"));
        final List<Book> books = new ArrayList<>(allLines.size());
        allLines.forEach(line->{
            String[] tmp = line.split("\\|");
            books.add(new Book(tmp[0], tmp[1]));
        });

        System.out.println(books.size());
    }

    @Test
    public void string(){
        String s = "zhongguop:wenhua : zuoye测试：zhognwen.pdf";
        System.out.println(s.replaceAll(":|：","-"));


        String name = s.replaceAll(":|：","-");
        String url  = "http://localhost/hello.pdzx";
        name = url.endsWith(".pdf") ? name : name.substring(0, name.lastIndexOf("."))+url.substring(url.lastIndexOf("."));
        System.out.println("name = " + name);
    }


}

class Book {
    private String name;
    private String url;

    public Book(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
