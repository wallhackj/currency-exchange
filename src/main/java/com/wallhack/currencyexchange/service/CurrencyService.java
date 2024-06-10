package com.wallhack.currencyexchange.service;

import com.wallhack.currencyexchange.dao.ImplCurrencyDAO;
import com.wallhack.currencyexchange.model.CurrencyDTO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CurrencyService {
    private final ImplCurrencyDAO currencyDAO;

    public CurrencyService(Connection connection) {
        this.currencyDAO = new ImplCurrencyDAO(connection);
    }

    public void insertCurrency(CurrencyDTO currency) throws SQLException {
        currencyDAO.save(currency);
    }

    public void updateCurrency(CurrencyDTO currency) throws SQLException {
        currencyDAO.update(currency);
    }

    public void deleteCurrency(long id) throws SQLException {
        currencyDAO.delete(id);
    }

    public Optional<CurrencyDTO> getCurrencyById(long id) throws SQLException {
        return currencyDAO.findById(id);
    }

    public List<CurrencyDTO> getAllCurrencies() throws SQLException {
        return currencyDAO.findAll();
    }

    public Optional<CurrencyDTO> getCurrencyByCode(String code) throws SQLException {
        return currencyDAO.findByCode(code);
    }

}
