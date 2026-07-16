ALTER TABLE review_crawl_retries
    ADD UNIQUE KEY uk_review_retry_game (game_code);
