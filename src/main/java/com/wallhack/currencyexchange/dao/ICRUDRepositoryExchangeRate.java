package com.wallhack.currencyexchange.dao;

import com.wallhack.currencyexchange.model.ExchangeRateDTO;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ICRUDRepositoryExchangeRate extends ICRUDRepository<ExchangeRateDTO>{
    Optional<ExchangeRateDTO> findExchangeByBothCurrencies(String baseCurrency, String targetCurrency) throws SQLException;
    List<ExchangeRateDTO> findByCurrencyWithUSD(String currency) throws SQLException;
}
