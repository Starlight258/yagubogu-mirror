package com.yagubogu.reward.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.member.domain.Member;
import com.yagubogu.prediction.domain.GamePrediction;
import com.yagubogu.prediction.domain.PredictionPick;
import com.yagubogu.prediction.repository.GamePredictionRepository;
import com.yagubogu.prediction.service.PredictionSettlementService;
import com.yagubogu.reward.domain.GifticonIssuance;
import com.yagubogu.reward.domain.GifticonIssuanceStatus;
import com.yagubogu.reward.domain.WeeklyTopScore;
import com.yagubogu.reward.repository.GifticonIssuanceRepository;
import com.yagubogu.reward.repository.WeeklyTopScoreRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.base.ServiceUsingMysqlTestBase;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
class RewardDrawServiceUsingMysqlTest extends ServiceUsingMysqlTestBase {

    @Autowired
    private RewardDrawService rewardDrawService;

    @Autowired
    private PredictionSettlementService predictionSettlementService;

    @Autowired
    private WeeklyTopScoreRepository weeklyTopScoreRepository;

    @Autowired
    private GifticonIssuanceRepository gifticonIssuanceRepository;

    @Autowired
    private GamePredictionRepository gamePredictionRepository;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private GameFactory gameFactory;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    private Team homeTeam, awayTeam;
    private Stadium stadium;
    private LocalDate weekStart;

    @BeforeEach
    void setUp() {
        homeTeam = teamRepository.findByTeamCode("HT").orElseThrow();
        awayTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        stadium = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();

        LocalDate today = LocalDate.now();
        weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1L);
    }

    @DisplayName("이번 주 최고 점수 달성자 중 3명을 추첨해 READY 상태로 발급한다")
    @Test
    void drawWeeklyReward_picksTopScorers() {
        // given: twoWins가 최고점(2), oneWin은 1점
        Member twoWins = memberFactory.save(b -> b.team(homeTeam));
        Member oneWin = memberFactory.save(b -> b.team(homeTeam));
        settleTwoWinsAndOneWin(twoWins, oneWin);

        // when
        rewardDrawService.drawWeeklyReward();

        // then
        WeeklyTopScore weeklyTopScore = weeklyTopScoreRepository.findByWeekStart(weekStart).orElseThrow();
        List<GifticonIssuance> issuances = gifticonIssuanceRepository.findAllByWeeklyTopScore(weeklyTopScore);

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(weeklyTopScore.getTopScore()).isEqualTo(2);
            softAssertions.assertThat(issuances).hasSize(1);
            softAssertions.assertThat(issuances.get(0).getMember().getId()).isEqualTo(twoWins.getId());
            softAssertions.assertThat(issuances.get(0).getStatus()).isEqualTo(GifticonIssuanceStatus.READY);
            softAssertions.assertThat(issuances.get(0).getExternalOrderId()).isNotBlank();
        });
    }

    @DisplayName("최고 점수 동점자가 3명을 넘으면 그중 3명만 뽑는다")
    @Test
    void drawWeeklyReward_limitsToThreeWinnersAmongTies() {
        // given: 4명 모두 1승씩(동점 최고점)
        Game game = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(homeTeam).awayTeam(awayTeam)
                .date(weekStart)
                .homeScore(5).awayScore(3)
                .gameState(GameState.COMPLETED));

        for (int i = 0; i < 4; i++) {
            Member member = memberFactory.save(b -> b.team(homeTeam));
            gamePredictionRepository.save(new GamePrediction(member, game, PredictionPick.HOME));
        }
        predictionSettlementService.settlePendingGames();

        // when
        rewardDrawService.drawWeeklyReward();

        // then
        WeeklyTopScore weeklyTopScore = weeklyTopScoreRepository.findByWeekStart(weekStart).orElseThrow();
        List<GifticonIssuance> issuances = gifticonIssuanceRepository.findAllByWeeklyTopScore(weeklyTopScore);

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(weeklyTopScore.getTopScore()).isEqualTo(1);
            softAssertions.assertThat(issuances).hasSize(3);
            softAssertions.assertThat(issuances)
                    .extracting(issuance -> issuance.getMember().getId())
                    .doesNotHaveDuplicates();
        });
    }

    @DisplayName("동점 후보가 3명보다 많아도 두 번 실행하면 뽑힌 당첨자 구성이 그대로 유지된다")
    @Test
    void drawWeeklyReward_idempotent_evenWhenMoreCandidatesThanWinnersExist() {
        // given: 5명 모두 1승씩
        Game game = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(homeTeam).awayTeam(awayTeam)
                .date(weekStart)
                .homeScore(5).awayScore(3)
                .gameState(GameState.COMPLETED));

        for (int i = 0; i < 5; i++) {
            Member member = memberFactory.save(b -> b.team(homeTeam));
            gamePredictionRepository.save(new GamePrediction(member, game, PredictionPick.HOME));
        }
        predictionSettlementService.settlePendingGames();

        // when
        rewardDrawService.drawWeeklyReward();
        WeeklyTopScore firstDraw = weeklyTopScoreRepository.findByWeekStart(weekStart).orElseThrow();
        Set<Long> firstWinnerIds = winnerMemberIds(firstDraw);

        rewardDrawService.drawWeeklyReward();
        WeeklyTopScore secondDraw = weeklyTopScoreRepository.findByWeekStart(weekStart).orElseThrow();
        Set<Long> secondWinnerIds = winnerMemberIds(secondDraw);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(secondDraw.getId()).isEqualTo(firstDraw.getId());
            softAssertions.assertThat(secondWinnerIds).isEqualTo(firstWinnerIds);
            softAssertions.assertThat(secondWinnerIds).hasSize(3);
        });
    }

    private Set<Long> winnerMemberIds(final WeeklyTopScore weeklyTopScore) {
        return gifticonIssuanceRepository.findAllByWeeklyTopScore(weeklyTopScore).stream()
                .map(issuance -> issuance.getMember().getId())
                .collect(Collectors.toSet());
    }

    @DisplayName("두 번 실행해도 당첨자와 발급 건수가 그대로 유지된다")
    @Test
    void drawWeeklyReward_idempotent() {
        // given
        Member twoWins = memberFactory.save(b -> b.team(homeTeam));
        Member oneWin = memberFactory.save(b -> b.team(homeTeam));
        settleTwoWinsAndOneWin(twoWins, oneWin);

        // when
        rewardDrawService.drawWeeklyReward();
        rewardDrawService.drawWeeklyReward();

        // then
        assertThat(weeklyTopScoreRepository.findAll())
                .filteredOn(score -> score.getWeekStart().equals(weekStart))
                .hasSize(1);

        WeeklyTopScore weeklyTopScore = weeklyTopScoreRepository.findByWeekStart(weekStart).orElseThrow();
        assertThat(gifticonIssuanceRepository.findAllByWeeklyTopScore(weeklyTopScore)).hasSize(1);
    }

    @DisplayName("이번 주에 확정된 예측이 하나도 없으면 추첨하지 않는다")
    @Test
    void drawWeeklyReward_noParticipants_noDraw() {
        // when
        rewardDrawService.drawWeeklyReward();

        // then
        Optional<WeeklyTopScore> weeklyTopScore = weeklyTopScoreRepository.findByWeekStart(weekStart);
        assertThat(weeklyTopScore).isEmpty();
    }

    private void settleTwoWinsAndOneWin(final Member twoWins, final Member oneWin) {
        Game game1 = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(homeTeam).awayTeam(awayTeam)
                .date(weekStart)
                .homeScore(5).awayScore(3)
                .gameState(GameState.COMPLETED));
        Game game2 = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(homeTeam).awayTeam(awayTeam)
                .date(weekStart.plusDays(1))
                .homeScore(4).awayScore(1)
                .gameState(GameState.COMPLETED));

        gamePredictionRepository.save(new GamePrediction(twoWins, game1, PredictionPick.HOME));
        gamePredictionRepository.save(new GamePrediction(twoWins, game2, PredictionPick.HOME));
        gamePredictionRepository.save(new GamePrediction(oneWin, game1, PredictionPick.HOME));

        predictionSettlementService.settlePendingGames();
    }
}
