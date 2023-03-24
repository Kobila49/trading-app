package com.broker.trading_app.service;

import com.broker.trading_app.SharedTestClass;
import com.broker.trading_app.dto.TradeDTO;
import com.broker.trading_app.entity.Trade;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.broker.external.BrokerTradeSide.BUY;
import static com.broker.external.BrokerTradeSide.SELL;
import static com.broker.trading_app.enums.TradeStatus.PENDING_EXECUTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

@Slf4j
class TradingServiceTest extends SharedTestClass {

    @Autowired
    TradingService tradingService;


    @BeforeEach
    public void setUp() {
        this.repository.deleteAll();
    }


    @Test
    void makeBuyTrade() {
        var tradeDTO = generateValidTradeDTO();
        var id = this.tradingService.makeTrade(tradeDTO, BUY);
        var trade = this.repository.findById(id);
        assertShareFields(trade);
        assertThat(trade.get().getSide()).isEqualTo(BUY);
        await()
                .atMost(4, TimeUnit.SECONDS)
                .until(() -> this.repository.findStatusById(id), not(equalTo(PENDING_EXECUTION)));
        trade = this.repository.findById(id);
        assertThat(trade).isNotEmpty();
        assertThat(trade.get().getStatus()).isNotEqualTo(PENDING_EXECUTION);
    }

    @Test
    void makeSellTrade() {
        var tradeDTO = generateValidTradeDTO();
        var id = this.tradingService.makeTrade(tradeDTO, SELL);
        var trade = this.repository.findById(id);
        assertShareFields(trade);
        assertThat(trade.get().getSide()).isEqualTo(SELL);
        await()
                .atMost(4, TimeUnit.SECONDS)
                .until(() -> this.repository.findStatusById(id), not(equalTo(PENDING_EXECUTION)));
        trade = this.repository.findById(id);
        assertThat(trade).isNotEmpty();
        assertThat(trade.get().getStatus()).isNotEqualTo(PENDING_EXECUTION);
    }

    private static void assertShareFields(Optional<Trade> trade) {
        assertThat(trade).isNotEmpty();
        assertThat(trade.get().getStatus()).isEqualTo(PENDING_EXECUTION);
        assertThat(trade.get().getQuantity()).isEqualTo(1000);
        assertThat(trade.get().getPrice()).isEqualTo(1.56);
    }

    private TradeDTO generateValidTradeDTO() {
        return new TradeDTO("EUR/USD", 1000, 1.56);
    }

}
