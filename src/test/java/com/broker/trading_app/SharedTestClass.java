package com.broker.trading_app;

import com.broker.trading_app.repository.TradeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
public class SharedTestClass {
    @Autowired
    protected
    TradeRepository repository;
}
