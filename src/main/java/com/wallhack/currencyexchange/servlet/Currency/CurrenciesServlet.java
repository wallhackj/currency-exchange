package com.wallhack.currencyexchange.servlet.Currency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallhack.currencyexchange.model.CurrencyDTO;
import com.wallhack.currencyexchange.model.ErrorResponse;
import com.wallhack.currencyexchange.service.CurrencyService;
import com.wallhack.currencyexchange.utils.SingletonDataBaseConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.wallhack.currencyexchange.utils.ServletUtils.*;
import static com.wallhack.currencyexchange.utils.ServletUtils.handleResponseError;
import static jakarta.servlet.http.HttpServletResponse.*;

@WebServlet(name = "CurrenciesServlet", value = "/currencies")
public class CurrenciesServlet extends HttpServlet {
    private Connection connection;
    private CurrencyService currencyService;
    private final  ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = Logger.getLogger(CurrenciesServlet.class.getName());

    @Override
    public void init() {
        connection = SingletonDataBaseConnection.getInstance().getConnection();
        this.currencyService = new CurrencyService(connection);
    }

    @Override
    public void destroy() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error closing database connection", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        prepareResponse(resp);

        try {
            processGetCurrencies(resp);
        }catch (Exception e) {
            handleResponseError(resp , logger , mapper , e , SC_INTERNAL_SERVER_ERROR
                    , "Error writing IO Exception response");
        }
    }

    private void processGetCurrencies(HttpServletResponse resp) throws IOException, SQLException {
        List<CurrencyDTO> currencies = currencyService.getAllCurrencies();
        resp.setStatus(HttpServletResponse.SC_OK);
        mapper.writeValue(resp.getWriter(), currencies);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        prepareResponse(resp);

        try {
            processPostCurrencies(req , resp);
        }catch (Exception e) {
            handleResponseError(resp, logger , mapper , e , SC_INTERNAL_SERVER_ERROR , "Internal server error");
        }
    }

    private void processPostCurrencies(HttpServletRequest req, HttpServletResponse resp) throws IOException, SQLException {
        String fromCurrencyCode = req.getParameter("code");
        String fromCurrencyFullName = req.getParameter("name");
        String fromCurrencySign = req.getParameter("sign");

        if (!stringIsNotEmpty(fromCurrencyCode, fromCurrencyFullName, fromCurrencySign)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter() , new ErrorResponse(SC_BAD_REQUEST
                    , "Missing required parameters"));
            return;
        }

        Optional<CurrencyDTO> findByCode =  currencyService.getCurrencyByCode(fromCurrencyCode);

        if (findByCode.isPresent()) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            mapper.writeValue(resp.getWriter() , new ErrorResponse(SC_CONFLICT , "Currency already exists"));
        }else {
            CurrencyDTO insertedCurrency = new CurrencyDTO(-1, fromCurrencyCode, fromCurrencyFullName, fromCurrencySign);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            currencyService.insertCurrency(insertedCurrency);
            mapper.writeValue(resp.getWriter(), insertedCurrency);

        }
    }
}
