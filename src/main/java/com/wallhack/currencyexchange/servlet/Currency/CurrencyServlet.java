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
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.wallhack.currencyexchange.utils.ServletUtils.handleResponseError;
import static com.wallhack.currencyexchange.utils.ServletUtils.prepareResponse;
import static jakarta.servlet.http.HttpServletResponse.*;

@WebServlet(name = "CurrencyServlet", value = "/currency/*")
public class CurrencyServlet extends HttpServlet {
    private Connection connection;
    private CurrencyService currencyService;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Logger logger = Logger.getLogger(CurrencyServlet.class.getName());

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
            logger.log(Level.SEVERE, "Error closing database connection", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp){
        prepareResponse(resp);

        try {
            processGetCurrency(req , resp);
        }catch (IOException e){
            handleResponseError(resp , logger , mapper , e , SC_INTERNAL_SERVER_ERROR ,"Error writing IO Exception response" );
        }
    }

    private void processGetCurrency(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var currencyInfo = req.getPathInfo();

        if (currencyInfo == null || currencyInfo.equals("/")) {
            resp.setStatus(SC_BAD_REQUEST);
            mapper.writeValue(resp.getWriter(), new ErrorResponse(SC_BAD_REQUEST
                    , "Empty path parameter"));
            return;
        }

        try {
            Optional<CurrencyDTO> currency = currencyService.getCurrencyByCode(currencyInfo.substring(1));

            if (currency.isPresent()){
                resp.setStatus(HttpServletResponse.SC_OK);
                mapper.writeValue(resp.getWriter(), currency.get());

            } else {
                resp.setStatus(SC_NOT_FOUND);
                mapper.writeValue(resp.getWriter(), new ErrorResponse(SC_NOT_FOUND
                        , "Currency not found"));
            }
        } catch (SQLException e) {
            if (!resp.isCommitted()) {
                handleResponseError(resp , logger , mapper , e , SC_INTERNAL_SERVER_ERROR
                        , "Something went wrong with database, try again later");
            }
        }
    }
}
