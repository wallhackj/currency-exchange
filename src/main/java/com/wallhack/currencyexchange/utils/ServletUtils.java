package com.wallhack.currencyexchange.utils;


public class ServletUtils {

    public static boolean stringIsNotEmpty(String str1, String str2, String str3) {
        return str1 == null || str2 == null || str3 == null
                || str1.isEmpty() || str2.isEmpty() || str3.isEmpty();
    }
}
