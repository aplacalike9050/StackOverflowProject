package com.xiao.cs209a_project.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
//没用了可以删掉
public class DateConverter {

    public static LocalDateTime fromUnixTimestamp(Long timestamp) {
        if (timestamp == null) return null;
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneOffset.UTC);
    }

    public static Long toUnixTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.toEpochSecond(ZoneOffset.UTC);
    }
}