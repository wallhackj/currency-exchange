package com.wallhack.currencyexchange.service;

import com.wallhack.currencyexchange.model.CurrencyDTO;
import com.wallhack.currencyexchange.model.ExchangeDTO;
import com.wallhack.currencyexchange.model.ExchangeRateDTO;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.wallhack.currencyexchange.utils.ServletUtils.stringIsNotEmpty;

public class RateService {
    private final ExchangeService exchangeService;
    private final CurrencyService currencyService;
    private static final Logger logger = Logger.getLogger(RateService.class.getName());

    public RateService(Connection connection) {
        this.currencyService = new CurrencyService(connection);
        this.exchangeService = new ExchangeService(connection);
    }

    public Optional<ExchangeDTO> getSumAfterExchange(String source , String target, float amount) throws SQLException {
        var rate = 0f;
        ExchangeRateDTO exchangeRateDTO = null;

        if (stringIsNotEmpty(source, target, "1") || amount <= 0) {
            return Optional.empty();
        }

        Optional<CurrencyDTO> sourceCurrency = currencyService.getCurrencyByCode(source);
        Optional<CurrencyDTO> targetCurrency = currencyService.getCurrencyByCode(target);

        if (sourceCurrency.isPresent() && targetCurrency.isPresent()) {
            Optional<ExchangeRateDTO> primeExchangeRateDTO = exchangeService.getExchangeRateByBothCurrency(source, target);
            Optional<ExchangeRateDTO> invertedExchangeRateDTO = exchangeService.getExchangeRateByBothCurrency(target, source);

            if (primeExchangeRateDTO.isPresent()) {
                rate = primeExchangeRateDTO.get().rate().floatValue();
                exchangeRateDTO = primeExchangeRateDTO.get();
            } else if (invertedExchangeRateDTO.isPresent()) {
                rate = inverseRate(invertedExchangeRateDTO.get().rate()).floatValue();
                exchangeRateDTO = invertedExchangeRateDTO.get();
            } else {
                rate = crossExchangeRate(source, target).floatValue();
                BigDecimal bigRate = new BigDecimal(rate);
                exchangeService.insertExchangeRate(new ExchangeRateDTO(-1,sourceCurrency.get() , targetCurrency.get() , bigRate));
                exchangeRateDTO = exchangeService.getExchangeRateByBothCurrency(source , target).orElse(null);
            }
        }
        if (rate == 0) {
            return Optional.empty();
        }
        return Optional.of(new ExchangeDTO(exchangeRateDTO, rate , amount , rate * amount));
    }

    private BigDecimal inverseRate(BigDecimal rate) {
        BigDecimal one = BigDecimal.ONE;
        return one.divide(rate, MathContext.DECIMAL128);
    }

    private BigDecimal crossExchangeRate(String source, String target) throws SQLException {
        BigDecimal result = BigDecimal.ZERO;

        Optional<ExchangeRateDTO> sourceExchangeRate = exchangeService.getExchangeRateByBothCurrency("USD", source);
        Optional<ExchangeRateDTO> targetExchangeRate = exchangeService.getExchangeRateByBothCurrency("USD", target);

        if (sourceExchangeRate.isPresent() && targetExchangeRate.isPresent()){
            try {
                result = (sourceExchangeRate.get().rate()
                        .add(targetExchangeRate.get().rate())
                        .divide(BigDecimal.valueOf(2),MathContext.DECIMAL128));

                exchangeService.insertExchangeRate(new ExchangeRateDTO(-1, sourceExchangeRate.get().targetCurrency()
                        , targetExchangeRate.get().targetCurrency(), result));

            }catch (ArithmeticException e){
                logger.log(Level.SEVERE, "Number format is incorrect : ", e);
            }
        }

        return result;
    }

}
