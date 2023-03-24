package com.broker.trading_app.controller;

import com.broker.external.BrokerTradeSide;
import com.broker.trading_app.dto.StatusDTO;
import com.broker.trading_app.dto.TradeDTO;
import com.broker.trading_app.entity.Trade;
import com.broker.trading_app.service.TradingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api")
public class TradingController {

    private final TradingService tradingService;

    public TradingController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    @GetMapping("/trades")
    public ResponseEntity<List<Trade>> allTrades() {
        return ResponseEntity.ok(this.tradingService.getAllTrades());
    }

    @GetMapping("/trades/{id}")
    public ResponseEntity<Trade> tradeById(@PathVariable("id") UUID id) {
        return this.tradingService.getTrade(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/trades/{id}/status")
    public ResponseEntity<StatusDTO> tradeStatusById(@PathVariable("id") UUID id) {
        return this.tradingService.getTrade(id)
                .map(t-> ResponseEntity.ok(new StatusDTO(t.getStatus())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/buy")

    public ResponseEntity<Void> buyTrade(@Valid @RequestBody TradeDTO tradeDTO) {
        return makeTrade(tradeDTO, BrokerTradeSide.BUY);
    }

    @PostMapping("/sell")
    public ResponseEntity<Void> sellTrade(@Valid @RequestBody TradeDTO tradeDTO) {
        return makeTrade(tradeDTO, BrokerTradeSide.SELL);
    }

    private ResponseEntity<Void> makeTrade(TradeDTO tradeDTO, BrokerTradeSide side) {

        var uuid = this.tradingService.makeTrade(tradeDTO, side);

        URI uri = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .replacePath("api/trades/{id}/status")
                .buildAndExpand(uuid)
                .toUri();

        return ResponseEntity.created(uri).build();
    }


}
