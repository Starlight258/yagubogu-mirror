package com.yagubogu.reward.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.yagubogu.global.exception.UnprocessableEntityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RecipientPhoneNumberTest {

    @DisplayName("휴대전화 번호를 숫자로 정규화한다")
    @Test
    void normalize() {
        RecipientPhoneNumber phoneNumber = new RecipientPhoneNumber("010-1234-5678");

        assertThat(phoneNumber.getValue()).isEqualTo("01012345678");
    }

    @DisplayName("유효하지 않은 휴대전화 번호는 등록할 수 없다")
    @Test
    void rejectInvalidPhoneNumber() {
        assertThatThrownBy(() -> new RecipientPhoneNumber("02-1234-5678"))
                .isInstanceOf(UnprocessableEntityException.class);
    }

    @DisplayName("휴대전화 번호를 응답용으로 마스킹한다")
    @Test
    void mask() {
        RecipientPhoneNumber phoneNumber = new RecipientPhoneNumber("01012345678");

        assertThat(phoneNumber.masked()).isEqualTo("010-****-5678");
    }
}
