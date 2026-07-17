package com.yagubogu.outbox.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.yagubogu.outbox.exception.InvalidOutboxPayloadException;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameCompletedOutboxPayloadTest {

    @DisplayName("GAME_COMPLETED outbox payload를 생성한다")
    @Test
    void create() {
        LocalDate date = LocalDate.of(2025, 7, 21);

        GameCompletedOutboxPayload payload = new GameCompletedOutboxPayload("20250721HTLT0", date);

        assertThat(payload.gameCode()).isEqualTo("20250721HTLT0");
        assertThat(payload.date()).isEqualTo(date);
    }

    @DisplayName("gameCode가 null이거나 blank이면 payload를 생성할 수 없다")
    @Test
    void create_blankGameCode() {
        LocalDate date = LocalDate.of(2025, 7, 21);

        assertThatThrownBy(() -> new GameCompletedOutboxPayload(null, date))
                .isInstanceOf(InvalidOutboxPayloadException.class)
                .hasMessage("gameCode must not be blank");
        assertThatThrownBy(() -> new GameCompletedOutboxPayload(" ", date))
                .isInstanceOf(InvalidOutboxPayloadException.class)
                .hasMessage("gameCode must not be blank");
    }

    @DisplayName("date가 null이면 payload를 생성할 수 없다")
    @Test
    void create_nullDate() {
        assertThatThrownBy(() -> new GameCompletedOutboxPayload("20250721HTLT0", null))
                .isInstanceOf(InvalidOutboxPayloadException.class)
                .hasMessage("date must not be null");
    }
}
