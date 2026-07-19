package com.yagubogu.prediction.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.game.domain.Game;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.global.exception.UnprocessableEntityException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.prediction.domain.PredictionPick;
import com.yagubogu.prediction.dto.v1.CreateGamePredictionRequest;
import com.yagubogu.prediction.repository.GamePredictionRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.base.ServiceUsingMysqlTestBase;
import com.yagubogu.support.concurrency.ConcurrencyTestRunner;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

/**
 * closes_at 정각을 실제 서버 Clock으로 고정해두고, 정밀한 마감 경계에서 동시 제출이
 * 어떻게 처리되는지 검증한다.
 */
@Import({
        AuthTestConfig.class,
        JpaAuditingConfig.class,
        GamePredictionCloseBoundaryConcurrencyTest.FixedClockConfig.class
})
class GamePredictionCloseBoundaryConcurrencyTest extends ServiceUsingMysqlTestBase {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final LocalDate GAME_DATE = LocalDate.of(2025, 7, 21);
    private static final LocalTime CLOSES_AT_TIME = LocalTime.of(18, 30, 0);

    private static final int THREAD_COUNT = 10;

    @Autowired
    private GamePredictionService gamePredictionService;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private GameFactory gameFactory;

    @Autowired
    private GamePredictionRepository gamePredictionRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    private Team homeTeam, awayTeam;
    private Stadium stadium;

    @BeforeEach
    void setUp() {
        homeTeam = teamRepository.findByTeamCode("HT").orElseThrow();
        awayTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        stadium = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();
    }

    @DisplayName("서버 시각이 closes_at과 정확히 같은 순간 여러 회원이 동시에 제출하면 전부 거절된다")
    @Test
    void submitPrediction_atExactCloseInstant_allRejected() throws InterruptedException {
        // given: closes_at == 고정된 서버 시각(now) 그 자체 -> now.isBefore(closesAt)는 false -> 마감 처리
        Game game = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(homeTeam).awayTeam(awayTeam)
                .date(GAME_DATE).startAt(CLOSES_AT_TIME));

        List<Member> members = createMembers(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger rejectedCount = new AtomicInteger();

        // when
        ConcurrencyTestRunner.runConcurrentlyPerItem(members, member -> {
            try {
                gamePredictionService.submitPrediction(
                        member.getId(), new CreateGamePredictionRequest(game.getId(), PredictionPick.HOME));
                successCount.incrementAndGet();
            } catch (UnprocessableEntityException e) {
                rejectedCount.incrementAndGet();
            }
        });

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(successCount.get()).isEqualTo(0);
            softAssertions.assertThat(rejectedCount.get()).isEqualTo(THREAD_COUNT);
        });
    }

    @DisplayName("서버 시각이 closes_at 1초 전인 순간 여러 회원이 동시에 제출하면 전부 성공한다")
    @Test
    void submitPrediction_oneSecondBeforeClose_allSucceed() throws InterruptedException {
        // given: closes_at == 고정된 서버 시각(now) + 1초 -> now.isBefore(closesAt)는 true -> 아직 마감 전
        Game game = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(homeTeam).awayTeam(awayTeam)
                .date(GAME_DATE).startAt(CLOSES_AT_TIME.plusSeconds(1)));

        List<Member> members = createMembers(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger rejectedCount = new AtomicInteger();

        // when
        ConcurrencyTestRunner.runConcurrentlyPerItem(members, member -> {
            try {
                gamePredictionService.submitPrediction(
                        member.getId(), new CreateGamePredictionRequest(game.getId(), PredictionPick.HOME));
                successCount.incrementAndGet();
            } catch (UnprocessableEntityException e) {
                rejectedCount.incrementAndGet();
            }
        });

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(successCount.get()).isEqualTo(THREAD_COUNT);
            softAssertions.assertThat(rejectedCount.get()).isEqualTo(0);
            softAssertions.assertThat(gamePredictionRepository.findAllByGame(game)).hasSize(THREAD_COUNT);
        });
    }

    private List<Member> createMembers(final int count) {
        List<Member> members = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            members.add(memberFactory.save(b -> b.team(homeTeam)));
        }
        return members;
    }

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        @Primary
        public Clock fixedClock() {
            return Clock.fixed(GAME_DATE.atTime(CLOSES_AT_TIME).atZone(ZONE).toInstant(), ZONE);
        }
    }
}
