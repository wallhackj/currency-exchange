package com.wallhack.currencyexchange.service;


import com.wallhack.currencyexchange.dao.ImplExchangeRateDAO;
import com.wallhack.currencyexchange.model.ExchangeDTO;
import com.wallhack.currencyexchange.model.ExchangeRateDTO;


import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static com.wallhack.currencyexchange.utils.ServletUtils.stringIsNotEmpty;


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

    public Optional<ExchangeDTO> getSumAfterExchange(String source , String target, float amount) throws SQLException {
        var rate = 0f;

        if (!stringIsNotEmpty(source, target, "1") && amount <= 0) {
            return Optional.empty();
        }

        Optional<ExchangeRateDTO> primeExchangeRateDTO = getExchangeRateByBothCurrency(source, target);
        Optional<ExchangeRateDTO> invertedExchangeRateDTO = getExchangeRateByBothCurrency(target, source);

        if (primeExchangeRateDTO.isPresent()){
            rate = primeExchangeRateDTO.get().rate().floatValue();

        } else if (invertedExchangeRateDTO.isPresent()) {
            rate = inverseRate(invertedExchangeRateDTO.get().rate()).floatValue();
        }else {
            rate = crossExchangeRate(source, target).floatValue();
        }

        return Optional.of(new ExchangeDTO(getExchangeRateByBothCurrency(source, target).get(), rate, amount, rate * amount));
    }

    private BigDecimal inverseRate(BigDecimal rate) {
       BigDecimal one = BigDecimal.ONE;

       return one.divide(rate, MathContext.DECIMAL128);
    }

    private BigDecimal crossExchangeRate(String source, String target) throws SQLException {
        BigDecimal result = BigDecimal.ZERO;

        Optional<ExchangeRateDTO> sourceExchangeRate = getExchangeRateByBothCurrency("USD", source);
        Optional<ExchangeRateDTO> targetExchangeRate = getExchangeRateByBothCurrency("USD", target);

        if (sourceExchangeRate.isPresent() && targetExchangeRate.isPresent()){
            try {
                result = (sourceExchangeRate.get().rate()
                        .add(targetExchangeRate.get().rate())
                        .divide(BigDecimal.valueOf(2),MathContext.DECIMAL128));

                insertExchangeRate(new ExchangeRateDTO(-1, sourceExchangeRate.get().targetCurrency()
                        , targetExchangeRate.get().targetCurrency(), result));

            }catch (ArithmeticException e){
                e.printStackTrace();
            }
        }

        return result;
    }


}
