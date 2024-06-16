package com.wallhack.currencyexchange.servlet.Exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallhack.currencyexchange.model.CurrencyDTO;
import com.wallhack.currencyexchange.model.ErrorResponse;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.wallhack.currencyexchange.utils.ServletUtils.*;
import static jakarta.servlet.http.HttpServletResponse.*;

@WebServlet(name = "ExchangeRatesServlet", value = "/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {
    private Connection connection;
    private ExchangeService exchangeService;
    private CurrencyService currencyService;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = Logger.getLogger(ExchangeRatesServlet.class.getName());

    @Override
    public void init() {
        this.connection = SingletonDataBaseConnection.getInstance().getConnection();
        this.exchangeService = new ExchangeService(connection);
        this.currencyService = new CurrencyService(connection);
    }

    @Override
    public void destroy() {
       try {
           connection.close();
       }catch (SQLException e) {
           logger.log(Level.SEVERE, "Error closing database connection", e);
       }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        prepareResponse(resp);

        try {
            processGetExchangeRates(resp);
        }catch (IOException e){
            handleResponseError(resp , logger , mapper , e , SC_INTERNAL_SERVER_ERROR ,"Error writing IO Exception response" );
        }
    }

    private void processGetExchangeRates(HttpServletResponse resp) throws IOException {
        try {
            List<ExchangeRateDTO> allExchangeRates = exchangeService.getAllExchangeRates();
            resp.setStatus(HttpServletResponse.SC_OK);
            mapper.writeValue(resp.getWriter(), allExchangeRates);

        } catch (SQLException e) {
            handleResponseError(resp , logger , mapper , e , SC_INTERNAL_SERVER_ERROR
                    ,"Something went wrong with database, try again later" );
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        prepareResponse(resp);

        var baseCurrencyCode = req.getParameter("baseCurrencyCode");
        var targetCurrencyCode = req.getParameter("targetCurrencyCode");
        var rate = req.getParameter("rate");

        if (stringIsNotEmpty(baseCurrencyCode, targetCurrencyCode, rate)) {
            resp.setStatus(SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter() , new ErrorResponse(SC_BAD_REQUEST , "Missing required parameters"));
            return;
        }

        try {
            BigDecimal bigRate = new BigDecimal(rate);
            Optional<CurrencyDTO> baseCurrency = currencyService.getCurrencyByCode(baseCurrencyCode);
            Optional<CurrencyDTO> targetCurrency = currencyService.getCurrencyByCode(targetCurrencyCode);

            if (baseCurrency.isPresent() && targetCurrency.isPresent()){
            Optional<ExchangeRateDTO> existingExchange = exchangeService
                        .getExchangeRateByBothCurrency(baseCurrencyCode, targetCurrencyCode);

            if (existingExchange.isEmpty()) {
                ExchangeRateDTO insertedExchangeRate = new ExchangeRateDTO(-1, baseCurrency.get(), targetCurrency.get(), bigRate);
                resp.setStatus(HttpServletResponse.SC_CREATED);
                exchangeService.insertExchangeRate(insertedExchangeRate);
                mapper.writeValue(resp.getWriter(), insertedExchangeRate);

            }else {
                resp.setStatus(SC_CONFLICT);
                mapper.writeValue(resp.getWriter(), new ErrorResponse(SC_CONFLICT , "Exchange rate not found"));
            }

            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(), new ErrorResponse(SC_NOT_FOUND , "Currency not found"));
            }

        } catch (SQLException e) {
            handleResponseError(resp , logger , mapper , e ,SC_INTERNAL_SERVER_ERROR
                    , "Something went wrong with database, try again later");
        }catch (NumberFormatException e){
            handleResponseError(resp , logger , mapper , e ,SC_NOT_ACCEPTABLE
                    , "Wrong rate format , only numbers are acceptable");
        }
    }
}
