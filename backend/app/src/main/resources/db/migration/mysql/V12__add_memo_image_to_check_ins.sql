-- V12__add_memo_image_to_check_ins.sql
ALTER TABLE check_ins ADD COLUMN memo TEXT NULL;
ALTER TABLE check_ins ADD COLUMN image_url VARCHAR(500) NULL;