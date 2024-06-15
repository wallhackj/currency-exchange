package com.wallhack.currencyexchange.servlet.Currency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallhack.currencyexchange.model.CurrencyDTO;
import com.wallhack.currencyexchange.service.CurrencyService;
import com.wallhack.currencyexchange.utils.SingletonDataBaseConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet(name = "CurrencyServlet", value = "/currency/*")
public class CurrencyServlet extends HttpServlet {
    private CurrencyService currencyService;
    private Connection connection;

    @Override
    public void init() {
        connection = SingletonDataBaseConnection.getInstance().getConnection();
        this.currencyService = new CurrencyService(connection);
    }

    @Override
    public void destroy() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException{
        ObjectMapper mapper = new ObjectMapper();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        var currencyInfo = req.getPathInfo();

        if (currencyInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        try {
            Optional<CurrencyDTO> currency = currencyService.getCurrencyByCode(currencyInfo.substring(1));

            if (currency.isPresent()){
                mapper.writeValue(resp.getWriter(), currency.get());
                resp.setStatus(HttpServletResponse.SC_OK);
            } else resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }

    }

}
