-- V12__add_memo_image_to_check_ins.sql
ALTER TABLE check_ins ADD COLUMN memo TEXT NULL;

CREATE TABLE check_in_images
(
    check_in_images_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    check_in_id        BIGINT       NOT NULL,
    image_url          VARCHAR(500) NOT NULL,
    created_at         DATETIME(6)  NULL,
    updated_at         DATETIME(6)  NULL,
    deleted_at         DATETIME(6)  NULL,
    CONSTRAINT fk_check_in_images_check_in FOREIGN KEY (check_in_id) REFERENCES check_ins (check_ins_id)
);
