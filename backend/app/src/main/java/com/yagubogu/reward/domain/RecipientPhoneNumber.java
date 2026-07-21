package com.yagubogu.reward.domain;

import com.yagubogu.global.exception.UnprocessableEntityException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class RecipientPhoneNumber {

    private static final String KOREAN_MOBILE_PHONE_PATTERN = "^010\\d{8}$";

    @Column(name = "recipient_phone_number", length = 20)
    private String value;

    public RecipientPhoneNumber(final String value) {
        String normalized = normalize(value);
        if (!normalized.matches(KOREAN_MOBILE_PHONE_PATTERN)) {
            throw new UnprocessableEntityException("Invalid recipient phone number");
        }
        this.value = normalized;
    }

    private String normalize(final String value) {
        if (value == null) {
            return "";
        }
        return value.replace("-", "").replace(" ", "");
    }

    public String masked() {
        return value.substring(0, 3) + "-****-" + value.substring(7);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof RecipientPhoneNumber that)) {
            return false;
        }
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
