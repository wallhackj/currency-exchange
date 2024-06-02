package com.wallhack.currencyexchange.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ICRUDRepository<T> {
    Optional<T> findById(long id) throws SQLException;
    List<T> findAll() throws SQLException;
    void save(T entity) throws SQLException;
    void update(T entity) throws SQLException;
    void delete(long id) throws SQLException;
}
