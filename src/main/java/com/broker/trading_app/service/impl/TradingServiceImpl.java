package com.broker.trading_app.service.impl;

import com.broker.external.BrokerTrade;
import com.broker.external.BrokerTradeSide;
import com.broker.external.ExternalBroker;
import com.broker.trading_app.dto.TradeDTO;
import com.broker.trading_app.entity.Trade;
import com.broker.trading_app.enums.TradeStatus;
import com.broker.trading_app.repository.TradeRepository;
import com.broker.trading_app.service.TradingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.StreamSupport;

import static com.broker.trading_app.enums.TradeStatus.PENDING_EXECUTION;

@Slf4j
@Service
public class TradingServiceImpl implements TradingService {

    @Value("${trading.app.timeoutInSeconds}")
    private Integer timeout;

    private static final String TRADE_EXPIRED = "trade expired";
    private final ExternalBroker broker;
    private final TradeRepository repository;

    public TradingServiceImpl(
            BrokerResponseCallbackImpl callback,
            TradeRepository repository) {
        this.broker = new ExternalBroker(callback);
        this.repository = repository;
    }

    @Override
    public UUID makeTrade(TradeDTO trade, BrokerTradeSide tradeSide) {
        var savedTrade = initialStateSaved(trade, tradeSide);
        var brokerTrade = getBrokerTrade(savedTrade);

        CompletableFuture.runAsync(() -> {
                    executeTradeOnExternalBroker(brokerTrade);
                    var tradeStatus = repository.findStatusById(brokerTrade.getId());

                    while (PENDING_EXECUTION.equals(tradeStatus)) {
                        try {
                            // I realise this is not nice solution,
                            // but I didn't have time to investigate for a better solution
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        tradeStatus = repository.findStatusById(brokerTrade.getId());
                    }
                })
                .orTimeout(timeout, TimeUnit.SECONDS)
                .whenComplete((result, exception) -> {
                    if (exception instanceof TimeoutException) {
                        log.info("Trade with UUID %s failed with status NOT_EXECUTED.".formatted(brokerTrade.getId()));
                        tradeFailed(brokerTrade);
                        Thread.currentThread().interrupt();
                    }
                });

        return savedTrade.getId();
    }

    @Override
    public List<Trade> getAllTrades() {
        return StreamSupport.stream(this.repository.findAll().spliterator(), false).toList();
    }

    @Override
    public Optional<Trade> getTrade(UUID id) {
        return this.repository.findById(id);
    }

    private static BrokerTrade getBrokerTrade(Trade trade) {
        return new BrokerTrade(
                trade.getId(),
                trade.getSymbol(),
                trade.getQuantity(),
                trade.getSide(),
                BigDecimal.valueOf(trade.getPrice()));
    }

    private Trade initialStateSaved(TradeDTO brokerTrade, BrokerTradeSide side) {
        var trade = getTradeObject(brokerTrade, side);
        return this.repository.save(trade);
    }

    private void tradeFailed(BrokerTrade brokerTrade) {
        this.repository.updateStatusAndReason(
                brokerTrade.getId(), TradeStatus.NOT_EXECUTED, TRADE_EXPIRED);
    }

    private Trade getTradeObject(TradeDTO tradeDTO, BrokerTradeSide side) {
        return Trade.builder()
                .quantity(tradeDTO.quantity())
                .symbol(tradeDTO.symbol())
                .side(side)
                .price(tradeDTO.price())
                .status(PENDING_EXECUTION)
                .timestamp(LocalDateTime.now())
                .build();
    }


    private void executeTradeOnExternalBroker(BrokerTrade brokerTrade) {
        this.broker.execute(brokerTrade);
    }


}
