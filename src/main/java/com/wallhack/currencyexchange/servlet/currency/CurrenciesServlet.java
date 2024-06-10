package com.wallhack.currencyexchange.servlet.currency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallhack.currencyexchange.model.CurrencyDTO;
import com.wallhack.currencyexchange.service.CurrencyService;
import com.wallhack.currencyexchange.utils.SingletonDataBaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;


@WebServlet(name = "CurrenciesServlet", value = "/currencies")
public class CurrenciesServlet extends HttpServlet {
    private CurrencyService currencyService;

    @Override
    public void init() {
        Connection connection = SingletonDataBaseConnection.getInstance().getConnection();
        this.currencyService = new CurrencyService(connection);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            List<CurrencyDTO> currencies = currencyService.getAllCurrencies();
            mapper.writeValue(resp.getWriter(), currencies);

            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        String fromCurrencyCode = req.getParameter("code");
        String fromCurrencyFullName = req.getParameter("name");
        String fromCurrencySign = req.getParameter("sign");

        if (fromCurrencyCode == null || fromCurrencyFullName == null || fromCurrencySign == null
                || fromCurrencyCode.isEmpty() || fromCurrencyFullName.isEmpty() || fromCurrencySign.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        CurrencyDTO currency = new CurrencyDTO(99999999, fromCurrencyCode, fromCurrencyFullName, fromCurrencySign);

        try {
           Optional<CurrencyDTO> findByCode =  currencyService.getCurrencyByCode(fromCurrencyCode);
           if (findByCode.isPresent()) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
           }

           currencyService.insertCurrency(currency);
           resp.setStatus(HttpServletResponse.SC_CREATED);

        }catch (SQLException e){
            e.printStackTrace();
        }

    }
}
