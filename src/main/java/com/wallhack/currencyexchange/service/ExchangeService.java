package com.wallhack.currencyexchange.service;


import com.wallhack.currencyexchange.dao.ImplExchangeRateDAO;
import com.wallhack.currencyexchange.model.ExchangeRateDTO;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ExchangeService {
    private final ImplExchangeRateDAO exchangeRateDAO;

    public ExchangeService(Connection connection) {
        this.exchangeRateDAO = new ImplExchangeRateDAO(connection);
    }

    public void insertExchangeRate(ExchangeRateDTO exchangeRate) throws SQLException {
        exchangeRateDAO.save(exchangeRate);
    }

    public void updateExchangeRate(ExchangeRateDTO exchangeRate) throws SQLException {
        exchangeRateDAO.update(exchangeRate);
    }

    public void deleteExchangeRate(long id) throws SQLException {
        exchangeRateDAO.delete(id);
    }

    public Optional<ExchangeRateDTO> getExchangeRateById(long id) throws SQLException {
        return exchangeRateDAO.findById(id);
    }

    public List<ExchangeRateDTO> getAllExchangeRates() throws SQLException {
        return exchangeRateDAO.findAll();
    }

    public List<ExchangeRateDTO> getExchangeRateByCurrency(String currency) throws SQLException {
        return exchangeRateDAO.findByCurrencyWithUSD(currency);
    }

    public Optional<ExchangeRateDTO> getExchangeRateByBothCurrency(String source, String target) throws SQLException {
        return exchangeRateDAO.findExchangeByBothCurrencies(source, target);
    }
}
