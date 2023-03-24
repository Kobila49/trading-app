package com.broker.trading_app.service.impl;

import com.broker.external.BrokerResponseCallback;
import com.broker.trading_app.enums.TradeStatus;
import com.broker.trading_app.repository.TradeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class BrokerResponseCallbackImpl implements BrokerResponseCallback {

    private static final String NO_AVAILABLE_QUOTES = "No available quotes";
    private final TradeRepository repository;

    public BrokerResponseCallbackImpl(TradeRepository repository) {
        this.repository = repository;
    }

    @Override
    public void successful(UUID tradeId) {
        log.info("Trade with UUID %s successful.".formatted(tradeId));
        this.repository.updateStatus(tradeId, TradeStatus.EXECUTED);
    }

    @Override
    public void unsuccessful(UUID tradeId, String reason) {
        log.info("Trade with UUID %s unsuccessful.".formatted(tradeId));
        this.repository.updateStatusAndReason(tradeId, TradeStatus.EXECUTED, NO_AVAILABLE_QUOTES);

    }
}
