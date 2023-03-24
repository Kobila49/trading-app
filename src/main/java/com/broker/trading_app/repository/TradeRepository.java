package com.broker.trading_app.repository;

import com.broker.trading_app.entity.Trade;
import com.broker.trading_app.enums.TradeStatus;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TradeRepository extends CrudRepository<Trade, UUID> {

    @Query("SELECT status FROM trade WHERE id = :id")
    TradeStatus findStatusById(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE trade SET status = :status WHERE id = :id")
    void updateStatus(
            @Param("id") UUID id,
            @Param("status") TradeStatus status);

    @Modifying
    @Query("UPDATE trade SET status = :status, reason = :reason WHERE id = :id")
    void updateStatusAndReason(
            @Param("id") UUID id,
            @Param("status") TradeStatus status,
            @Param("reason") String reason);
}
