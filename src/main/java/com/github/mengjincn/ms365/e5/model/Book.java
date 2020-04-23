package com.github.mengjincn.ms365.e5.model;

import java.util.Objects;

public class Book {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        Book book = (Book) o;
        return getName().equals(book.getName()) &&
                getUrl().equals(book.getUrl());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getUrl());
    }
}