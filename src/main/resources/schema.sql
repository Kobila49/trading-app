CREATE TABLE trade
(
    id        UUID,
    quantity  BIGINT,
    symbol    VARCHAR(255),
    price     NUMERIC(20,2),
    side      VARCHAR(255),
    status    VARCHAR(255),
    reason    VARCHAR(255),
    timestamp TIMESTAMP
);
