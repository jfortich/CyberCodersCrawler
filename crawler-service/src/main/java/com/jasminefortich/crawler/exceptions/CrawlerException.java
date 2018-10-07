package com.jasminefortich.crawler.exceptions;

public class CrawlerException extends Exception {

    public CrawlerException() { }

    public CrawlerException(String message) { super(message); }

    public CrawlerException(Throwable throwable) { super(throwable); }

    public CrawlerException(String message, Throwable throwable) { super(message, throwable); }

}
