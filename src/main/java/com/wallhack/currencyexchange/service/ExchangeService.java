package com.wallhack.currencyexchange.service;

import com.wallhack.currencyexchange.dao.CurrencyDAO;
import com.wallhack.currencyexchange.dao.ExchangeRateDAO;
import com.wallhack.currencyexchange.utils.SingletonDataBaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class ExchangeService {

    public ExchangeService() {
        try {
            Connection connection = SingletonDataBaseConnection.getInstance().getConnection();

            CurrencyDAO currencyDAO = new CurrencyDAO(connection);
            ExchangeRateDAO exchangeRateDAO = new ExchangeRateDAO(connection);

        }catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
