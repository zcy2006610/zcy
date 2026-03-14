package com.zcy.forum.utils;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 数据库时间类型 ↔ Long时间戳 转换工具类
 * 核心时区：东八区（Asia/Shanghai），避免时区偏移
 * 支持类型：DATE/DATETIME/TIMESTAMP ↔ 毫秒级(13位)/秒级(10位)时间戳
 */
public class DbTimeToLongUtil {
    // 核心时区（固定东八区，禁止修改）
    public static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Shanghai");
    // 数据库日期格式（适配DATE/DATETIME字符串解析）
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ===================== 数据库时间类型 → Long 时间戳（核心方法） =====================

    /**
     * 数据库DATE类型（仅年月日）→ 毫秒级时间戳（13位，当天00:00:00）
     * @param sqlDate 数据库java.sql.Date对象（对应DATE字段）
     * @return 毫秒级时间戳 | null（入参为null时）
     */
    public static Long dbDateToMilliTimestamp(Date sqlDate) {
        if (sqlDate == null) {
            return null;
        }
        LocalDate localDate = sqlDate.toLocalDate();
        // DATE转当天0点的Instant，再转毫秒时间戳
        return localDate.atStartOfDay(DEFAULT_ZONE).toInstant().toEpochMilli();
    }

    /**
     * 数据库DATE类型（仅年月日）→ 秒级时间戳（10位，当天00:00:00）
     * @param sqlDate 数据库java.sql.Date对象
     * @return 秒级时间戳 | null
     */
    public static Long dbDateToSecondTimestamp(Date sqlDate) {
        Long milliTs = dbDateToMilliTimestamp(sqlDate);
        return milliTs == null ? null : milliTs / 1000;
    }

    /**
     * 数据库DATETIME/TIMESTAMP类型 → 毫秒级时间戳（13位）
     * @param sqlTimestamp 数据库java.sql.Timestamp对象（对应DATETIME/TIMESTAMP字段）
     * @return 毫秒级时间戳 | null
     */
    public static Long dbTimestampToMilliTimestamp(Timestamp sqlTimestamp) {
        if (sqlTimestamp == null) {
            return null;
        }
        // Timestamp本身带时区，直接转毫秒
        return sqlTimestamp.getTime();
    }

    /**
     * 数据库DATETIME/TIMESTAMP类型 → 秒级时间戳（10位）
     * @param sqlTimestamp 数据库java.sql.Timestamp对象
     * @return 秒级时间戳 | null
     */
    public static Long dbTimestampToSecondTimestamp(Timestamp sqlTimestamp) {
        Long milliTs = dbTimestampToMilliTimestamp(sqlTimestamp);
        return milliTs == null ? null : milliTs / 1000;
    }

    /**
     * 数据库时间字符串（如"2026-03-01"、"2026-03-01 12:30:45"）→ 毫秒级时间戳
     * @param dbTimeStr 数据库时间字符串
     * @return 毫秒级时间戳 | null（格式错误/入参null时）
     */
    public static Long dbTimeStrToMilliTimestamp(String dbTimeStr) {
        if (dbTimeStr == null || dbTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            // 区分DATE（仅年月日）和DATETIME（含时分秒）
            if (dbTimeStr.length() == 10) { // yyyy-MM-dd
                LocalDate localDate = LocalDate.parse(dbTimeStr, DATE_FORMATTER);
                return localDate.atStartOfDay(DEFAULT_ZONE).toInstant().toEpochMilli();
            } else { // yyyy-MM-dd HH:mm:ss
                LocalDateTime localDateTime = LocalDateTime.parse(dbTimeStr, DATETIME_FORMATTER);
                return localDateTime.atZone(DEFAULT_ZONE).toInstant().toEpochMilli();
            }
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("数据库时间字符串格式错误：" + dbTimeStr, e);
        }
    }

    // ===================== Long时间戳 → 数据库时间类型（反向验证） =====================

    /**
     * 毫秒级时间戳 → 数据库java.sql.Date（DATE类型，仅年月日）
     * @param milliTimestamp 13位毫秒级时间戳
     * @return java.sql.Date | null
     */
    public static Date milliTimestampToDbDate(Long milliTimestamp) {
        if (milliTimestamp == null) {
            return null;
        }
        Instant instant = Instant.ofEpochMilli(milliTimestamp);
        LocalDate localDate = LocalDate.ofInstant(instant, DEFAULT_ZONE);
        return Date.valueOf(localDate);
    }

    /**
     * 毫秒级时间戳 → 数据库java.sql.Timestamp（DATETIME/TIMESTAMP类型）
     * @param milliTimestamp 13位毫秒级时间戳
     * @return java.sql.Timestamp | null
     */
    public static Timestamp milliTimestampToDbTimestamp(Long milliTimestamp) {
        if (milliTimestamp == null) {
            return null;
        }
        return new Timestamp(milliTimestamp);
    }

    /**
     * 自动识别时间戳单位（10位=秒/13位=毫秒）→ 数据库时间字符串（yyyy-MM-dd HH:mm:ss）
     * 用于快速验证转换结果是否和数据库一致
     * @param timestamp 10位秒级/13位毫秒级时间戳
     * @return 格式化时间字符串 | null
     */
    public static String autoTsToDbTimeStr(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        String tsStr = timestamp.toString();
        Instant instant;
        if (tsStr.length() == 10) { // 秒级
            instant = Instant.ofEpochSecond(timestamp);
        } else if (tsStr.length() == 13) { // 毫秒级
            instant = Instant.ofEpochMilli(timestamp);
        } else {
            throw new IllegalArgumentException("无效的时间戳（仅支持10位秒级/13位毫秒级）：" + timestamp);
        }
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, DEFAULT_ZONE);
        return localDateTime.format(DATETIME_FORMATTER);
    }

    // ===================== 测试示例（验证转换准确性） =====================

}