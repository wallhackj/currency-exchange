package com.wallhack.currencyexchange.service;


import com.wallhack.currencyexchange.dao.ImplExchangeRateDAO;
import com.wallhack.currencyexchange.model.ExchangeRateDTO;


import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
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

    public List<ExchangeRateDTO> getExchangeRateByCurrencyId(int id) throws SQLException {
        return exchangeRateDAO.findByCurrency(id);
    }

    public Optional<ExchangeRateDTO> getExchangeRateByBothCurrencyIds(int source, int target) throws SQLException {
        return exchangeRateDAO.findExchangeByBothCurrencies(source, target);
    }

    public BigDecimal getSumAfterExchange(ExchangeRateDTO exchangeRate, BigDecimal amount) throws SQLException {
        BigDecimal sum = BigDecimal.ZERO;

        Optional<ExchangeRateDTO> exchangeRateDTO = getExchangeRateByBothCurrencyIds(exchangeRate.targetCurrency()
                , exchangeRate.baseCurrency());

        if (getExchangeRateById(exchangeRate.id()).isPresent()){
            sum = exchangeRate.rate().multiply(amount);
        }else if (exchangeRateDTO.isPresent()) {
            sum = inverseRate(exchangeRateDTO.get()).multiply(amount);

        } else if (!Objects.equals(crossExchangeRate(exchangeRate), BigDecimal.ZERO)){
           sum = crossExchangeRate(exchangeRate).multiply(amount);

        }

        return sum;
    }

    private BigDecimal inverseRate(ExchangeRateDTO exchangeRateDTO){
       BigDecimal one = BigDecimal.ONE;

       return one.divide(exchangeRateDTO.rate(), MathContext.DECIMAL128);
    }

    private BigDecimal crossExchangeRate(ExchangeRateDTO exchangeRateDTO) throws SQLException {
        BigDecimal result = BigDecimal.ZERO;

        Optional<ExchangeRateDTO> sourceExchangeRate = getExchangeRateByCurrencyId(exchangeRateDTO.baseCurrency())
                .stream()
                .findFirst();
        Optional<ExchangeRateDTO> targetExchangeRate = getExchangeRateByCurrencyId(exchangeRateDTO.targetCurrency())
                .stream()
                .findFirst();

        if (sourceExchangeRate.isPresent() && targetExchangeRate.isPresent()){
            try {
                if (sourceExchangeRate.get().baseCurrency() == exchangeRateDTO.baseCurrency()
                        && targetExchangeRate.get().baseCurrency() == exchangeRateDTO.targetCurrency()) {

                    result = sourceExchangeRate.get().rate().divide(targetExchangeRate.get().rate(), MathContext.DECIMAL128);
                }else if (sourceExchangeRate.get().targetCurrency() == exchangeRateDTO.baseCurrency()
                        && targetExchangeRate.get().baseCurrency() == exchangeRateDTO.targetCurrency()){

                    result = inverseRate(sourceExchangeRate.get()).divide(targetExchangeRate.get().rate(), MathContext.DECIMAL128);
                } else if (sourceExchangeRate.get().baseCurrency() == exchangeRateDTO.baseCurrency()
                        && targetExchangeRate.get().targetCurrency() == exchangeRateDTO.targetCurrency()) {

                    result = sourceExchangeRate.get().rate().divide(inverseRate(targetExchangeRate.get()), MathContext.DECIMAL128);
                }else {
                    result = inverseRate(sourceExchangeRate.get()).divide(inverseRate(targetExchangeRate.get()), MathContext.DECIMAL128);
                }
            }catch (ArithmeticException e){
                e.printStackTrace();
            }
        }

        return result;
    }


}
