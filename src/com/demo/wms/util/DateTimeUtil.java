package com.demo.wms.util;

import com.demo.wms.exception.DataParseException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 时间工具类。
 * 统一项目中的时间格式，避免不同模块各自使用不同的显示和解析规则。
 */
public final class DateTimeUtil {
    public static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_PATTERN);

    private DateTimeUtil() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    public static String format(LocalDateTime dateTime) {
        return dateTime == null ? "" : DEFAULT_FORMATTER.format(dateTime);
    }

    public static String formatOrEmpty(LocalDateTime dateTime) {
        return format(dateTime);
    }

    public static LocalDateTime parse(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new DataParseException("时间字符串不能为空");
        }
        try {
            return LocalDateTime.parse(text.trim(), DEFAULT_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new DataParseException("时间格式错误: " + text, ex);
        }
    }

    public static LocalDateTime tryParse(String text) {
        try {
            return parse(text);
        } catch (DataParseException ex) {
            return null;
        }
    }
}
