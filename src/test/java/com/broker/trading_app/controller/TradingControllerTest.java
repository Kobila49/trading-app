package com.broker.trading_app.controller;

import com.broker.trading_app.SharedTestClass;
import com.broker.trading_app.dto.StatusDTO;
import com.broker.trading_app.entity.Trade;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

import static com.broker.external.BrokerTradeSide.BUY;
import static com.broker.external.BrokerTradeSide.SELL;
import static com.broker.trading_app.enums.TradeStatus.PENDING_EXECUTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@AutoConfigureMockMvc
class TradingControllerTest extends SharedTestClass {

    private static final int PREFIX_LENGTH = "http://localhost".length();
    private static final int SUFFIX_LENGTH = "/status".length();
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        this.repository.deleteAll();
    }


    @Test
    void errorWhenRequestInvalid() throws Exception {
        var invalidRequest = """
                {
                    "symbol": "EUR/CHF",
                    "quantity": -1000,
                    "price": 0
                }
                """;

        var result = mockMvc.perform(post("/api/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertThat(result.getResponse().getContentAsString())
                .contains("Symbol valid values: USD/JPY, EUR/USD");
        assertThat(result.getResponse().getContentAsString())
                .contains("quantity must be greater than 0 and less than or equal to 1M");
        assertThat(result.getResponse().getContentAsString())
                .contains("price must be greater than 0");
    }

    @Test
    void tradeCreated() throws Exception {
        var validRequest = """
                {
                    "symbol": "EUR/USD",
                    "quantity": 1000,
                    "price": 3.14
                }
                """;

        var result = mockMvc.perform(post("/api/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
        assertThat(result.getResponse().getHeader("Location"))
                .containsSubsequence("api/trades/", "/status");
    }

    @Test
    void tradeStatusEndpoint() throws Exception {

        var result = makeTrade("sell");

        var url = Objects.requireNonNull(result.getResponse().getHeader("Location")).substring(PREFIX_LENGTH);

        result = mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andReturn();
        var statusObject = objectMapper
                .readValue(result.getResponse().getContentAsString(), StatusDTO.class);
        assertThat(statusObject.status()).isEqualTo(PENDING_EXECUTION);
    }

    @Test
    void tradeDetailsEndpoint() throws Exception {

        var result = makeTrade("sell");

        var url = Objects.requireNonNull(result.getResponse().getHeader("Location"))
                .substring(PREFIX_LENGTH);
        url = url.substring(0, url.length() - SUFFIX_LENGTH);

        result = mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andReturn();
        var trade = objectMapper
                .readValue(result.getResponse().getContentAsString(), Trade.class);
        assertThat(trade.getId()).isInstanceOf(UUID.class);
        assertThat(trade.getQuantity()).isEqualTo(1000L);
        assertThat(trade.getSymbol()).isEqualTo("EUR/USD");
        assertThat(trade.getPrice()).isEqualTo(3.14);
        assertThat(trade.getSide()).isEqualTo(SELL);
    }

    @Test
    void tradeListEndpoint() throws Exception {
        for (int i = 0; i < 5; i++) {
            var side = i % 2 == 0 ? "buy" : "sell";
            makeTrade(side);
        }

        // waiting until all trades change status from PENDING_EXECUTION to something
        await()
                .atMost(4, TimeUnit.SECONDS)
                .until(() -> !StreamSupport
                        .stream(this.repository.findAll()
                                .spliterator(), false)
                        .map(Trade::getStatus)
                        .toList()
                        .contains(PENDING_EXECUTION));

        var result = mockMvc.perform(get("/api/trades"))
                .andExpect(status().isOk())
                .andReturn();
        var tradeList = objectMapper
                .readValue(result.getResponse().getContentAsString(), new TypeReference<List<Trade>>() {
                });
        assertThat(tradeList)
                .isNotEmpty()
                .hasSize(5);
        assertThat(tradeList.stream().map(Trade::getStatus).toList()).doesNotContain(PENDING_EXECUTION);
        assertThat(tradeList.stream().map(Trade::getSide).toList()).contains(SELL, BUY);
        tradeList.forEach(System.out::println);
    }

    private MvcResult makeTrade(String side) throws Exception {
        return mockMvc.perform(post("/api/" + side)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private String validRequest() {
        return """
                {
                    "symbol": "EUR/USD",
                    "quantity": 1000,
                    "price": 3.14
                }
                """;
    }


}
