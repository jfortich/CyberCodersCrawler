package com.jasminefortich.rest.exceptions;

import com.jasminefortich.rest.services.CrawlerService;

public class CrawlerException extends Exception {

    public CrawlerException() { }

    public CrawlerException(String message) { super(message); }

    public CrawlerException(Throwable throwable) { super(throwable); }

    public CrawlerException(String message, Throwable throwable) { super(message, throwable); }

}
