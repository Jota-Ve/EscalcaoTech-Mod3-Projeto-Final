package com.ada.transferscheduling.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Util {

    public static LocalDateTime StringToLocalDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(date, formatter);
    }
}
