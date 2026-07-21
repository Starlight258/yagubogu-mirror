ALTER TABLE gifticon_issuances
    ADD COLUMN recipient_phone_number VARCHAR(20) NULL AFTER external_order_id,
    ADD COLUMN reserve_trace_id BIGINT NULL AFTER status,
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0 AFTER reserve_trace_id,
    MODIFY COLUMN status VARCHAR(30) NOT NULL;

UPDATE gifticon_issuances
SET status = 'AWAITING_RECIPIENT_INFO'
WHERE status = 'READY'
  AND recipient_phone_number IS NULL;
