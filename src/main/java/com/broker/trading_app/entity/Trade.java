package com.broker.trading_app.entity;

import com.broker.external.BrokerTradeSide;
import com.broker.trading_app.enums.TradeStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;


@Table
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Trade {
    @Id
    private UUID id;
    @Column
    private Long quantity;
    @Column
    private String symbol;
    @Column
    private Double price;
    @Column
    private BrokerTradeSide side;
    @Column
    private TradeStatus status;
    @Column
    private String reason;
    @Column
    private LocalDateTime timestamp;
}
