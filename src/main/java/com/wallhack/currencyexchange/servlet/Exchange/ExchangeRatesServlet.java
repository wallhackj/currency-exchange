package com.wallhack.currencyexchange.servlet.Exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallhack.currencyexchange.model.CurrencyDTO;
import com.wallhack.currencyexchange.model.ExchangeRateDTO;
import com.wallhack.currencyexchange.service.CurrencyService;
import com.wallhack.currencyexchange.service.ExchangeService;
import com.wallhack.currencyexchange.utils.SingletonDataBaseConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;


import static com.wallhack.currencyexchange.utils.ServletUtils.stringIsNotEmpty;

@WebServlet(name = "ExchangeRatesServlet", value = "/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private CurrencyService currencyService;
    private ExchangeService exchangeService;
    private Connection connection;

    @Override
    public void init(){
        connection = SingletonDataBaseConnection.getInstance().getConnection();
        this.exchangeService = new ExchangeService(connection);
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            List<ExchangeRateDTO> exchangeRates = exchangeService.getAllExchangeRates();
            mapper.writeValue(resp.getWriter(), exchangeRates);
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp){
        var baseCurrencyCode = req.getParameter("baseCurrencyCode");
        var targetCurrencyCode = req.getParameter("targetCurrencyCode");
        var rate = req.getParameter("rate");

        if (stringIsNotEmpty(baseCurrencyCode, targetCurrencyCode, rate)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            Optional<CurrencyDTO> baseCurrency = currencyService.getCurrencyByCode(baseCurrencyCode);
            Optional<CurrencyDTO> targetCurrency = currencyService.getCurrencyByCode(targetCurrencyCode);
            BigDecimal bigRate;

            try {
                bigRate = new BigDecimal(rate);
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            if (baseCurrency.isPresent() && targetCurrency.isPresent()) {
                Optional<ExchangeRateDTO> existingExchange = exchangeService.getExchangeRateByBothCurrencyIds(baseCurrency.get().id(), targetCurrency.get().id());
                if (existingExchange.isPresent()) {
                    resp.setStatus(HttpServletResponse.SC_CONFLICT);
                } else {
                    exchangeService.insertExchangeRate(new ExchangeRateDTO(-1, baseCurrency.get().id(), targetCurrency.get().id(), bigRate));
                    resp.setStatus(HttpServletResponse.SC_CREATED);
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
