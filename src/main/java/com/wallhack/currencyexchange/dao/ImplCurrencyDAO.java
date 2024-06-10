package com.wallhack.currencyexchange.dao;

import com.wallhack.currencyexchange.model.CurrencyDTO;
import lombok.AllArgsConstructor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class ImplCurrencyDAO implements ICRUDRepositoryCurrency{
    private final Connection connection;

    private static CurrencyDTO getResult(ResultSet resultSet) throws SQLException {
        var currencyId = resultSet.getInt("id");
        var currencyCode = resultSet.getString("code");
        var currencyFullName = resultSet.getString("fullName");
        var currencySign = resultSet.getString("sign");

        return new CurrencyDTO(currencyId, currencyCode, currencyFullName, currencySign);
    }

    @Override
    public Optional<CurrencyDTO> findById(long id) throws SQLException {
        var queryFindById = "SELECT * FROM Currencies WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryFindById)) {
            preparedStatement.setLong(1, id);
            var resultSet = preparedStatement.executeQuery();

           if (resultSet.next()) {
               CurrencyDTO result = getResult(resultSet);
               return Optional.of(result);
           }
        }

        return Optional.empty();
    }

    @Override
    public List<CurrencyDTO> findAll() throws SQLException {
        List<CurrencyDTO> currencies = new ArrayList<>();
        var queryFindAll = "SELECT * FROM Currencies";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryFindAll)) {
            var resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                CurrencyDTO result = getResult(resultSet);
                currencies.add(result);
            }
        }

        return currencies;
    }

    private static void executeFirstThreePreparedStatement(CurrencyDTO entity, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, entity.code());
        preparedStatement.setString(2, entity.fullName());
        preparedStatement.setString(3, entity.sign());
        preparedStatement.executeUpdate();
    }

    @Override
    public void save(CurrencyDTO entity) throws SQLException {
        var querySave = "INSERT INTO Currencies (code, fullName, sign) VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(querySave)) {
            executeFirstThreePreparedStatement(entity, preparedStatement);
        }

    }

    @Override
    public void update(CurrencyDTO entity) throws SQLException {
        var queryUpdate = "UPDATE Currencies SET code = ?, fullName = ?, sign = ? WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryUpdate)) {
            preparedStatement.setLong(4, entity.id());
            executeFirstThreePreparedStatement(entity, preparedStatement);
        }

    }

    @Override
    public void delete(long id) throws SQLException {
        var queryDelete = "DELETE FROM Currencies WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryDelete)) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        }

    }

    @Override
    public Optional<CurrencyDTO> findByCode(String code) throws SQLException {
        var queryFindByCode = "SELECT * FROM Currencies WHERE code = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryFindByCode)){
            preparedStatement.setString(1, code);
            var preparedStatementResult = preparedStatement.executeQuery();

            if (preparedStatementResult.next()) {
                CurrencyDTO result = getResult(preparedStatementResult);

                return Optional.of(result);
            }
        }

        return Optional.empty();
    }
}
