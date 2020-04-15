package com.github.mengjincn.ms365.e5.service;

import com.github.javafaker.Faker;
import com.github.javafaker.service.FakeValuesService;
import com.github.javafaker.service.RandomService;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

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
}
