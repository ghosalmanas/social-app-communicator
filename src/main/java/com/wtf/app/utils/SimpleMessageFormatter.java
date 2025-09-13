package com.wtf.app.utils;

import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class SimpleMessageFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        return new Date()+" : "+ record.getMessage() + System.lineSeparator();
    }
}