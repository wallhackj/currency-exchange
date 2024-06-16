package com.wallhack.currencyexchange.utils;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallhack.currencyexchange.model.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServletUtils {

    public static boolean stringIsNotEmpty(String str1, String str2, String str3) {
        return str1 == null || str2 == null || str3 == null
                || str1.isEmpty() || str2.isEmpty() || str3.isEmpty();
    }

    public static void prepareResponse(HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
    }

    public static void handleResponseError(HttpServletResponse resp, Logger logger, ObjectMapper mapper, Exception e, int statusCode, String errorMessage) {
        logger.log(Level.SEVERE, errorMessage, e);
        try {
            mapper.writeValue(resp.getWriter(), new ErrorResponse(statusCode, errorMessage));
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error writing error response", ex);
        }
    }


}
