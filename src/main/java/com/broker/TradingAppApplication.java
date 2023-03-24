package com.broker;

import com.broker.trading_app.entity.Trade;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback;

import java.util.UUID;


@SpringBootApplication
public class TradingAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradingAppApplication.class, args);
    }



    @Bean
    BeforeConvertCallback<Trade> beforeSaveCallback() {

        return trade -> {
            if (trade.getId() == null) {
                trade.setId(UUID.randomUUID());
            }
            return trade;
        };
    }
}
