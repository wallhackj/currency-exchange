package com.wallhack.currencyexchange.dao;

import com.wallhack.currencyexchange.model.ExchangeRateDTO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class ImplExchangeRateDAO implements ICRUDRepositoryExchangeRate {
    private final Connection connection;
    private final ImplCurrencyDAO implCurrencyDAO;

    public ImplExchangeRateDAO(Connection connection){
        this.connection = connection;
        this.implCurrencyDAO = new ImplCurrencyDAO(connection);
    }

    private Optional<ExchangeRateDTO> getExchangeRateDTO(ResultSet resultSet) throws SQLException {
        var exchangeRateId = resultSet.getLong("id");
        var baseCurrencyID = resultSet.getLong("baseCurrencyID");
        var targetCurrencyID = resultSet.getLong("targetCurrencyID");
        BigDecimal rate = resultSet.getBigDecimal("rate");

        if (implCurrencyDAO.findById(baseCurrencyID).isEmpty() && implCurrencyDAO.findById(targetCurrencyID).isEmpty()) {
           return Optional.empty();
        }

        return Optional.of(new ExchangeRateDTO(exchangeRateId, implCurrencyDAO.findById(baseCurrencyID).get()
                ,implCurrencyDAO.findById(targetCurrencyID).get(), rate));
    }

    @Override
    public Optional<ExchangeRateDTO> findById(long id) throws SQLException {
        var queryFindById = "SELECT * FROM ExchangeRates WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryFindById)) {
            preparedStatement.setLong(1, id);
            var resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                if (getExchangeRateDTO(resultSet).isPresent()){
                    ExchangeRateDTO result = getExchangeRateDTO(resultSet).get();
                    return Optional.of(result);
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public List<ExchangeRateDTO> findAll() throws SQLException {
        List<ExchangeRateDTO> exchangeRateDTOS = new ArrayList<>();
        var queryFindAll = "SELECT * FROM ExchangeRates";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryFindAll)) {
            var resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                if (getExchangeRateDTO(resultSet).isPresent()){
                    exchangeRateDTOS.add(getExchangeRateDTO(resultSet).get());
                }
            }
        }

        return exchangeRateDTOS;
    }


    @Override
    public void save(ExchangeRateDTO entity) throws SQLException {
        var querySave = "INSERT INTO ExchangeRates (baseCurrencyID, targetCurrencyID, rate) VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(querySave)) {
            preparedStatement.setLong(1, entity.baseCurrency().id());
            preparedStatement.setLong(2, entity.targetCurrency().id());
            preparedStatement.setBigDecimal(3, entity.rate());
            preparedStatement.executeUpdate();
        }

    }

    @Override
    public void update(ExchangeRateDTO entity) throws SQLException {
        var queryUpdate = "UPDATE ExchangeRates SET rate = ? WHERE id = ?";

        if (findExchangeByBothCurrencies(entity.baseCurrency().code() , entity.targetCurrency().code()).isPresent()) {

            var entityId = findExchangeByBothCurrencies(entity.baseCurrency().code(), entity.targetCurrency().code()).get().id();

            try (PreparedStatement preparedStatement = connection.prepareStatement(queryUpdate)) {
                preparedStatement.setBigDecimal(1, entity.rate());
                preparedStatement.setLong(2, entityId);
                preparedStatement.executeUpdate();
            }
        }
    }

    @Override
    public void delete(long id) throws SQLException {
        var queryDelete = "DELETE FROM ExchangeRates WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryDelete)) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        }

    }

    @Override
    public Optional<ExchangeRateDTO> findExchangeByBothCurrencies(String baseCurrency, String targetCurrency) throws SQLException{
        var queryFindExchangeByBothCurrencies = "SELECT * FROM ExchangeRates WHERE baseCurrencyID = ? AND targetCurrencyID = ?";

        if (implCurrencyDAO.findByCode(baseCurrency).isPresent() && implCurrencyDAO.findByCode(targetCurrency).isPresent()) {
            var baseCurrencyId = implCurrencyDAO.findByCode(baseCurrency).get().id();
            var targetCurrencyId = implCurrencyDAO.findByCode(targetCurrency).get().id();

            try (PreparedStatement preparedStatement = connection.prepareStatement(queryFindExchangeByBothCurrencies)) {
                preparedStatement.setLong(1, baseCurrencyId);
                preparedStatement.setLong(2, targetCurrencyId);
                var resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    if (getExchangeRateDTO(resultSet).isPresent()){
                        return Optional.of(getExchangeRateDTO(resultSet).get());
                    }
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public List<ExchangeRateDTO> findByCurrencyWithUSD(String currency) throws SQLException{
        List<ExchangeRateDTO> exchangeRateDTOS = new ArrayList<>();
        var queryFindByCurrency = "SELECT * FROM ExchangeRates WHERE baseCurrencyID = ? OR targetCurrencyID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryFindByCurrency)) {
          preparedStatement.setString(1, currency);
          preparedStatement.setString(2, currency);
          var resultSet = preparedStatement.executeQuery();

          while (resultSet.next()) {
            if (getExchangeRateDTO(resultSet).isPresent())
              exchangeRateDTOS.add(getExchangeRateDTO(resultSet).get());
          }
        }

        return exchangeRateDTOS;
    }
}
