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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;


@WebServlet(name = "ExchangeRatesServlet", value = "/exchangeRate/*")
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
            Optional<CurrencyDTO> targetCurrencyDTO = currencyService.getCurrencyByCode(currencyInfo.substring(3, 6));

            if (baseCurrencyDTO.isPresent() && targetCurrencyDTO.isPresent()) {
                Optional<ExchangeRateDTO> exchangeRateDTO = exchangeService
                        .getExchangeRateByBothCurrencyIds(baseCurrencyDTO.get().id(), targetCurrencyDTO.get().id());

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
}
