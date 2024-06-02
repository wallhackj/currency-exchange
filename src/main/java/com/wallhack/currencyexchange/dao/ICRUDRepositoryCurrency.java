package com.wallhack.currencyexchange.dao;

import com.wallhack.currencyexchange.model.CurrencyDTO;

import java.sql.SQLException;
import java.util.Optional;

public interface ICRUDRepositoryCurrency extends ICRUDRepository<CurrencyDTO> {
    Optional<CurrencyDTO> findByCode(String code) throws SQLException;
}
