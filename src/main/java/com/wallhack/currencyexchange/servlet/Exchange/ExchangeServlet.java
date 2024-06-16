package com.wallhack.currencyexchange.servlet.Exchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallhack.currencyexchange.model.ErrorResponse;
import com.wallhack.currencyexchange.model.ExchangeDTO;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.wallhack.currencyexchange.utils.ServletUtils.*;
import static jakarta.servlet.http.HttpServletResponse.*;

@WebServlet(name = "ExchangeServlet" , value = "/exchange")
public class ExchangeServlet extends HttpServlet {
    private Connection connection;
    private ExchangeService exchangeService;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = Logger.getLogger(ExchangeServlet.class.getName());

    @Override
    public void init()  {
        this.connection = SingletonDataBaseConnection.getInstance().getConnection();
        this.exchangeService = new ExchangeService(connection);
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        prepareResponse(resp);

        var fromCurrencyCode = req.getParameter("from");
        var toCurrencyCode = req.getParameter("to");
        var amountParam = req.getParameter("amount");

        if (stringIsNotEmpty(fromCurrencyCode, toCurrencyCode, amountParam)) {
            resp.setStatus(SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter() , new ErrorResponse(SC_BAD_REQUEST , "Some parameters are missing"));
        }

        try {
            Optional<ExchangeDTO> exchangeDTO = exchangeService.getSumAfterExchange(fromCurrencyCode, toCurrencyCode
                    , Float.parseFloat(amountParam));

            if (exchangeDTO.isPresent()) {
                resp.setStatus(HttpServletResponse.SC_OK);
                mapper.writeValue(resp.getWriter(), exchangeDTO.get());
            }

        } catch (SQLException e) {
            handleResponseError(resp , logger , mapper , e , SC_INTERNAL_SERVER_ERROR
                    , "Something went wrong with database, try again later");
        }catch (NumberFormatException e){
            handleResponseError(resp , logger , mapper , e ,SC_NOT_ACCEPTABLE
                    , "Wrong rate format , only numbers are acceptable");
        }
    }
}
