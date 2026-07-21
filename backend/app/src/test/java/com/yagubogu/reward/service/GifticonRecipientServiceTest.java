package com.yagubogu.reward.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.reward.domain.GifticonIssuance;
import com.yagubogu.reward.domain.GifticonIssuanceStatus;
import com.yagubogu.reward.repository.GifticonIssuanceRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GifticonRecipientServiceTest {

    @Mock
    private GifticonIssuanceRepository gifticonIssuanceRepository;

    private GifticonRecipientService gifticonRecipientService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-07-20T03:00:00Z"), ZoneOffset.UTC);
        gifticonRecipientService = new GifticonRecipientService(gifticonIssuanceRepository, clock);
    }

    @DisplayName("당첨자가 전화번호를 등록하면 발송 준비 상태가 된다")
    @Test
    void registerPhoneNumber() {
        GifticonIssuance issuance = new GifticonIssuance(null, null, "order-id", java.time.LocalDateTime.MIN);
        when(gifticonIssuanceRepository.findByIdAndMemberId(1L, 2L)).thenReturn(Optional.of(issuance));

        gifticonRecipientService.registerPhoneNumber(2L, 1L, "010-1234-5678");

        assertThat(issuance.getStatus()).isEqualTo(GifticonIssuanceStatus.READY);
        assertThat(issuance.getRecipientPhoneNumber().getValue()).isEqualTo("01012345678");
    }

    @DisplayName("본인의 당첨 발급 건이 아니면 전화번호를 등록할 수 없다")
    @Test
    void rejectUnknownIssuance() {
        when(gifticonIssuanceRepository.findByIdAndMemberId(1L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gifticonRecipientService.registerPhoneNumber(2L, 1L, "01012345678"))
                .isInstanceOf(NotFoundException.class);
    }
}
