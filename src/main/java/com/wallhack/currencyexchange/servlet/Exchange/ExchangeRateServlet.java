package com.wallhack.currencyexchange.servlet.Exchange;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallhack.currencyexchange.model.CurrencyDTO;
import com.wallhack.currencyexchange.model.ExchangeRateDTO;
import com.wallhack.currencyexchange.service.CurrencyService;
import com.wallhack.currencyexchange.service.ExchangeService;
import com.wallhack.currencyexchange.utils.SingletonDataBaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static com.wallhack.currencyexchange.utils.ServletUtils.stringIsNotEmpty;

@WebServlet(name = "ExchangeRateServlet", value = "/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    Connection connection;
    ExchangeService exchangeService;
    CurrencyService currencyService;

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
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getMethod();
        if (!method.equals("PATCH")) {
            super.service(req, resp);
        }

        this.doPatch(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        var currencyInfo = req.getPathInfo();

        if (currencyInfo.length() < 6) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        try {
            Optional<CurrencyDTO> baseCurrencyDTO = currencyService.getCurrencyByCode(currencyInfo.substring(1, 4));
            Optional<CurrencyDTO> targetCurrencyDTO = currencyService.getCurrencyByCode(currencyInfo.substring(4, 7));

            if (baseCurrencyDTO.isPresent() && targetCurrencyDTO.isPresent()) {
                Optional<ExchangeRateDTO> exchangeRateDTO = exchangeService.getExchangeRateByBothCurrency(baseCurrencyDTO.get().code(), targetCurrencyDTO.get().code());

                if (exchangeRateDTO.isPresent()){
                    mapper.writeValue(resp.getWriter(),exchangeRateDTO.get());
                    resp.setStatus(HttpServletResponse.SC_OK);
                }else resp.setStatus(HttpServletResponse.SC_NOT_FOUND);

            }else resp.setStatus(HttpServletResponse.SC_NOT_FOUND);

        }catch (SQLException e){
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        if (!"PATCH".equalsIgnoreCase(req.getMethod())) {
            resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        var currencyInfo = req.getPathInfo();

        if (currencyInfo.length() < 6) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        var baseCurrencyCode = currencyInfo.substring(1, 4);
        var targetCurrencyCode = currencyInfo.substring(4, 7);
        var rate = req.getReader().readLine().replace("rate=", "");

        if (stringIsNotEmpty(baseCurrencyCode, targetCurrencyCode, rate)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            Optional<CurrencyDTO> baseCurrency = currencyService.getCurrencyByCode(baseCurrencyCode);
            Optional<CurrencyDTO> targetCurrency = currencyService.getCurrencyByCode(targetCurrencyCode);
            BigDecimal bigRate = new BigDecimal(rate);

            if (baseCurrency.isPresent() && targetCurrency.isPresent()) {
                Optional<ExchangeRateDTO> existingExchange = exchangeService
                        .getExchangeRateByBothCurrency(baseCurrency.get().code(), targetCurrency.get().code());
                if (existingExchange.isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                } else {
                    exchangeService.updateExchangeRate(new ExchangeRateDTO(-1, baseCurrency.get(), targetCurrency.get(), bigRate));
                    resp.setStatus(HttpServletResponse.SC_CREATED);
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }catch (NumberFormatException e){
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
