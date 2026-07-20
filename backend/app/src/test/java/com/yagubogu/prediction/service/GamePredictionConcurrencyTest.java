package com.yagubogu.prediction.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.game.domain.Game;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.global.exception.ConflictException;
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
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
class GamePredictionConcurrencyTest extends ServiceUsingMysqlTestBase {

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

    @DisplayName("같은 회원이 같은 경기에 동시에 여러 번 제출해도 하나만 성공한다")
    @Test
    void submitPrediction_concurrentDuplicateRequests_onlyOneSucceeds() throws InterruptedException {
        // given
        Game game = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(homeTeam).awayTeam(awayTeam)
                .date(LocalDate.now().plusDays(1)));
        Member member = memberFactory.save(b -> b.team(homeTeam));
        CreateGamePredictionRequest request = new CreateGamePredictionRequest(game.getId(), PredictionPick.HOME);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger conflictCount = new AtomicInteger();

        // when
        ConcurrencyTestRunner.runConcurrently(THREAD_COUNT, () -> {
            try {
                gamePredictionService.submitPrediction(member.getId(), request);
                successCount.incrementAndGet();
            } catch (ConflictException e) {
                conflictCount.incrementAndGet();
            }
        });

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(successCount.get()).isEqualTo(1);
            softAssertions.assertThat(conflictCount.get()).isEqualTo(THREAD_COUNT - 1);
            softAssertions.assertThat(gamePredictionRepository.existsByMemberAndGame(member, game)).isTrue();
        });
    }

}
