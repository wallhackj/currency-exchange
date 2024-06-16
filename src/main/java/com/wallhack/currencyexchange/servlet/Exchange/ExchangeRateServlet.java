package com.wallhack.currencyexchange.servlet.Exchange;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallhack.currencyexchange.model.ErrorResponse;
import com.wallhack.currencyexchange.model.ExchangeRateDTO;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.wallhack.currencyexchange.utils.ServletUtils.*;
import static jakarta.servlet.http.HttpServletResponse.*;

@WebServlet(name = "ExchangeRateServlet", value = "/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {
    private Connection connection;
    private ExchangeService exchangeService;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = Logger.getLogger(ExchangeRateServlet.class.getName());

    @Override
    public void init(){
        connection = SingletonDataBaseConnection.getInstance().getConnection();
        this.exchangeService = new ExchangeService(connection);
    }

    @Override
    public void destroy() {
        try {
            connection.close();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error closing database connection", e);
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
        prepareResponse(resp);

        var currencyInfo = req.getPathInfo();

        if (currencyInfo.length() != 7) {
            resp.setStatus(SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(), new ErrorResponse(SC_BAD_REQUEST , "Non-existing path parameters"));
        }

        try {
            Optional<ExchangeRateDTO> exchangeRateDTO = exchangeService.getExchangeRateByBothCurrency(currencyInfo.substring(1, 4)
                        , currencyInfo.substring(4, 7));

            if (exchangeRateDTO.isPresent()){
                resp.setStatus(HttpServletResponse.SC_OK);
                mapper.writeValue(resp.getWriter(),exchangeRateDTO.get());

            }else {
                resp.setStatus(SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter() , new ErrorResponse(SC_BAD_REQUEST , "Exchange rate not found"));
            }

        }catch (SQLException e){
            handleResponseError(resp , logger , mapper , e ,SC_INTERNAL_SERVER_ERROR
                    , "Something went wrong with database, try again later");
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        prepareResponse(resp);

        var currencyInfo = req.getPathInfo();

        if (currencyInfo.length() != 7) {
            resp.setStatus(SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(), new ErrorResponse(SC_BAD_REQUEST , "Non-existing path parameters"));
        }

        var baseCurrencyCode = currencyInfo.substring(1, 4);
        var targetCurrencyCode = currencyInfo.substring(4, 7);
        var rate = req.getReader().readLine().replace("rate=", "");

        if (stringIsNotEmpty(baseCurrencyCode, targetCurrencyCode, rate)) {
            resp.setStatus(SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(), new ErrorResponse(SC_BAD_REQUEST , "Some or all parameters are empty"));
            return;
        }

        try {
            BigDecimal bigRate = new BigDecimal(rate);

            Optional<ExchangeRateDTO> existingExchange = exchangeService
                        .getExchangeRateByBothCurrency(baseCurrencyCode, targetCurrencyCode);

            if (existingExchange.isPresent()) {

                ExchangeRateDTO updatedExchangeDTO = new ExchangeRateDTO(-1, existingExchange.get().baseCurrency()
                        , existingExchange.get().targetCurrency() , bigRate);
                resp.setStatus(HttpServletResponse.SC_CREATED);
                exchangeService.updateExchangeRate(updatedExchangeDTO);
                mapper.writeValue(resp.getWriter(), updatedExchangeDTO);

            }else {
                resp.setStatus(SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(), new ErrorResponse(SC_NOT_FOUND, "Exchange rate not found"));
            }

        } catch (SQLException e) {
            handleResponseError(resp , logger , mapper , e , SC_INTERNAL_SERVER_ERROR
                    , "Something went wrong with database, try again later" );
        }catch (NumberFormatException e){
            handleResponseError(resp , logger , mapper , e , SC_INTERNAL_SERVER_ERROR
                    , "Wrong rate format , must be numbers");
        }
    }
}
