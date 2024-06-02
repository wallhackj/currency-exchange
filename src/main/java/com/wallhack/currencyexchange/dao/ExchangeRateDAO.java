package com.wallhack.currencyexchange.dao;

import com.wallhack.currencyexchange.model.ExchangeRateDTO;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class ExchangeRateDAO implements ICRUDRepositoryExchangeRate {
    private Connection connection;

    private static ExchangeRateDTO getExchangeRateDTO(ResultSet resultSet) throws SQLException {
        var exchangeRateId = resultSet.getLong("id");
        var baseCurrencyID = resultSet.getInt("baseCurrencyID");
        var targetCurrencyID = resultSet.getInt("targetCurrencyID");
        BigDecimal rate = resultSet.getBigDecimal("rate");

        return new ExchangeRateDTO(exchangeRateId, baseCurrencyID, targetCurrencyID, rate);
    }

    @Override
    public Optional<ExchangeRateDTO> findById(long id) throws SQLException {
        var queryFindById = "SELECT * FROM ExchangeRates WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryFindById)) {
            preparedStatement.setLong(1, id);
            var resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                ExchangeRateDTO result = getExchangeRateDTO(resultSet);

                return Optional.of(result);
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
                ExchangeRateDTO result = getExchangeRateDTO(resultSet);

                exchangeRateDTOS.add(result);
            }
        }

        return exchangeRateDTOS;
    }

    private static void firstThreePreparedStatements(ExchangeRateDTO entity, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setInt(1, entity.baseCurrency());
        preparedStatement.setInt(2, entity.targetCurrency());
        preparedStatement.setBigDecimal(3, entity.rate());
        preparedStatement.executeUpdate();

    }

    @Override
    public void save(ExchangeRateDTO entity) throws SQLException {
        var querySave = "INSERT INTO ExchangeRates (baseCurrencyID, targetCurrencyID, rate) VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(querySave)) {
            firstThreePreparedStatements(entity, preparedStatement);
        }

    }

    @Override
    public void update(ExchangeRateDTO entity) throws SQLException {
        var queryUpdate = "UPDATE ExchangeRates SET baseCurrencyID = ?, targetCurrencyID = ?, rate = ? WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryUpdate)) {
            preparedStatement.setLong(4, entity.id());
            firstThreePreparedStatements(entity, preparedStatement);
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
    public Optional<ExchangeRateDTO> findExchangeByBothCurrencies(int baseCurrency, int targetCurrency) throws SQLException{
        var queryFindExchangeByBothCurrencies = "SELECT * FROM ExchangeRates WHERE baseCurrencyID = ? AND targetCurrencyID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryFindExchangeByBothCurrencies)) {
            preparedStatement.setInt(1, baseCurrency);
            preparedStatement.setInt(2, targetCurrency);
            var resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                ExchangeRateDTO result = getExchangeRateDTO(resultSet);

                return Optional.of(result);
            }
        }

        return Optional.empty();
    }

    @Override
    public List<ExchangeRateDTO> findByCurrency(int currency) throws SQLException{
        List<ExchangeRateDTO> exchangeRateDTOS = new ArrayList<>();
        var queryFindByCurrency = "SELECT * FROM ExchangeRates WHERE baseCurrencyID = ? OR targetCurrencyID = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryFindByCurrency)) {
          preparedStatement.setInt(1, currency);
          preparedStatement.setInt(2, currency);
          var resultSet = preparedStatement.executeQuery();

          while (resultSet.next()) {
              ExchangeRateDTO result = getExchangeRateDTO(resultSet);
              exchangeRateDTOS.add(result);
          }
        }

        return exchangeRateDTOS;
    }
}
