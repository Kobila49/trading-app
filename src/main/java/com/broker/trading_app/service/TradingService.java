package com.broker.trading_app.service;


import com.broker.external.BrokerTradeSide;
import com.broker.trading_app.dto.TradeDTO;
import com.broker.trading_app.entity.Trade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TradingService {

    UUID makeTrade(TradeDTO trade, BrokerTradeSide tradeSide);

    List<Trade> getAllTrades();

    Optional<Trade> getTrade(UUID id);
}
