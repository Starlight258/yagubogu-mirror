package com.yagubogu.reward.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.global.exception.ConflictException;
import com.yagubogu.global.exception.UnprocessableEntityException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.prediction.domain.GamePrediction;
import com.yagubogu.prediction.domain.PredictionPick;
import com.yagubogu.prediction.repository.GamePredictionRepository;
import com.yagubogu.prediction.service.PredictionResultService;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
class WeeklyRewardDrawServiceUsingMysqlTest extends ServiceUsingMysqlTestBase {

    @Autowired
    private WeeklyRewardDrawService weeklyRewardDrawService;

    @Autowired
    private WeeklyRewardDrawBatchService weeklyRewardDrawBatchService;

    @Autowired
    private PredictionResultService predictionResultService;

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
    private LocalDate monday;

    @BeforeEach
    void setUp() {
        homeTeam = teamRepository.findByTeamCode("HT").orElseThrow();
        awayTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        stadium = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();

        LocalDate today = LocalDate.now();
        LocalDate mostRecentSunday = today.minusDays(today.getDayOfWeek().getValue() % 7);
        monday = mostRecentSunday.minusDays(6);
    }

    @DisplayName("이번 주 최고 점수 달성자 중 3명을 추첨해 수신자 정보 대기 상태로 발급한다")
    @Test
    void drawWinners_picksTopScorers() {
        // given: twoWins가 최고점(2), oneWin은 1점
        Member twoWins = memberFactory.save(b -> b.team(homeTeam));
        Member oneWin = memberFactory.save(b -> b.team(homeTeam));
        saveResolvedPredictions(twoWins, oneWin);

        // when
        WeeklyRewardDrawResult result = weeklyRewardDrawService.drawWinnersForLastCompletedWeek();

        // then
        WeeklyTopScore weeklyTopScore = weeklyTopScoreRepository.findByWeekStart(monday).orElseThrow();
        List<GifticonIssuance> issuances = gifticonIssuanceRepository.findAllByWeeklyTopScore(weeklyTopScore);

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(result).isEqualTo(WeeklyRewardDrawResult.DRAWN);
            softAssertions.assertThat(weeklyTopScore.getTopScore()).isEqualTo(2);
            softAssertions.assertThat(issuances).hasSize(1);
            softAssertions.assertThat(issuances.get(0).getMember().getId()).isEqualTo(twoWins.getId());
            softAssertions.assertThat(issuances.get(0).getStatus())
                    .isEqualTo(GifticonIssuanceStatus.AWAITING_RECIPIENT_INFO);
            softAssertions.assertThat(issuances.get(0).getExternalOrderId()).isNotBlank();
        });
    }

    @DisplayName("최고 점수 동점자가 3명을 넘으면 그중 3명만 뽑는다")
    @Test
    void drawWinners_limitsToThreeWinnersAmongTies() {
        // given: 4명 모두 1승씩(동점 최고점)
        Game game = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(homeTeam).awayTeam(awayTeam)
                .date(monday)
                .homeScore(5).awayScore(3)
                .gameState(GameState.COMPLETED));

        for (int i = 0; i < 4; i++) {
            Member member = memberFactory.save(b -> b.team(homeTeam));
            gamePredictionRepository.save(new GamePrediction(member, game, PredictionPick.HOME));
        }
        predictionResultService.reconcileUngradedPredictions();

        // when
        weeklyRewardDrawService.drawWinnersForLastCompletedWeek();

        // then
        WeeklyTopScore weeklyTopScore = weeklyTopScoreRepository.findByWeekStart(monday).orElseThrow();
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
    void drawWinners_idempotent_evenWhenMoreCandidatesThanWinnersExist() {
        // given: 5명 모두 1승씩
        Game game = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(homeTeam).awayTeam(awayTeam)
                .date(monday)
                .homeScore(5).awayScore(3)
                .gameState(GameState.COMPLETED));

        for (int i = 0; i < 5; i++) {
            Member member = memberFactory.save(b -> b.team(homeTeam));
            gamePredictionRepository.save(new GamePrediction(member, game, PredictionPick.HOME));
        }
        predictionResultService.reconcileUngradedPredictions();

        // when
        weeklyRewardDrawService.drawWinnersForLastCompletedWeek();
        WeeklyTopScore firstDraw = weeklyTopScoreRepository.findByWeekStart(monday).orElseThrow();
        Set<Long> firstWinnerIds = winnerMemberIds(firstDraw);

        weeklyRewardDrawService.drawWinnersForLastCompletedWeek();
        WeeklyTopScore secondDraw = weeklyTopScoreRepository.findByWeekStart(monday).orElseThrow();
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
    void drawWinners_idempotent() {
        // given
        Member twoWins = memberFactory.save(b -> b.team(homeTeam));
        Member oneWin = memberFactory.save(b -> b.team(homeTeam));
        saveResolvedPredictions(twoWins, oneWin);

        // when
        WeeklyRewardDrawResult firstResult = weeklyRewardDrawService.drawWinnersForLastCompletedWeek();
        WeeklyRewardDrawResult secondResult = weeklyRewardDrawService.drawWinnersForLastCompletedWeek();

        // then
        WeeklyTopScore weeklyTopScore = weeklyTopScoreRepository.findByWeekStart(monday).orElseThrow();
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(firstResult).isEqualTo(WeeklyRewardDrawResult.DRAWN);
            softAssertions.assertThat(secondResult).isEqualTo(WeeklyRewardDrawResult.ALREADY_DRAWN);
            softAssertions.assertThat(weeklyTopScoreRepository.findAll())
                    .filteredOn(score -> score.getWeekStart().equals(monday))
                    .hasSize(1);
            softAssertions.assertThat(gifticonIssuanceRepository.findAllByWeeklyTopScore(weeklyTopScore)).hasSize(1);
        });
    }

    @DisplayName("같은 주의 추첨이 동시에 실행되어도 한 번만 확정한다")
    @Test
    void drawWinners_absorbsConcurrentDuplicateDraw() throws Exception {
        // given
        Member twoWins = memberFactory.save(b -> b.team(homeTeam));
        Member oneWin = memberFactory.save(b -> b.team(homeTeam));
        saveResolvedPredictions(twoWins, oneWin);

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        try {
            Future<WeeklyRewardDrawResult> first = executorService.submit(
                    () -> drawAfterSignal(readyLatch, startLatch));
            Future<WeeklyRewardDrawResult> second = executorService.submit(
                    () -> drawAfterSignal(readyLatch, startLatch));

            assertThat(readyLatch.await(10, TimeUnit.SECONDS)).isTrue();
            startLatch.countDown();

            List<WeeklyRewardDrawResult> results = List.of(
                    first.get(10, TimeUnit.SECONDS),
                    second.get(10, TimeUnit.SECONDS)
            );

            WeeklyTopScore weeklyTopScore = weeklyTopScoreRepository.findByWeekStart(monday).orElseThrow();
            assertSoftly(softAssertions -> {
                softAssertions.assertThat(results)
                        .containsExactlyInAnyOrder(
                                WeeklyRewardDrawResult.DRAWN,
                                WeeklyRewardDrawResult.ALREADY_DRAWN);
                softAssertions.assertThat(weeklyTopScoreRepository.findAll())
                        .filteredOn(score -> score.getWeekStart().equals(monday))
                        .hasSize(1);
                softAssertions.assertThat(gifticonIssuanceRepository.findAllByWeeklyTopScore(weeklyTopScore))
                        .hasSize(1);
            });
        } finally {
            executorService.shutdownNow();
        }
    }

    private WeeklyRewardDrawResult drawAfterSignal(
            final CountDownLatch readyLatch,
            final CountDownLatch startLatch
    ) throws InterruptedException {
        readyLatch.countDown();
        startLatch.await();
        return weeklyRewardDrawService.drawWinnersForLastCompletedWeek();
    }

    @DisplayName("이번 주에 확정된 예측이 하나도 없으면 추첨하지 않는다")
    @Test
    void drawWinners_noParticipants_noDraw() {
        // when
        WeeklyRewardDrawResult result = weeklyRewardDrawService.drawWinnersForLastCompletedWeek();

        // then
        Optional<WeeklyTopScore> weeklyTopScore = weeklyTopScoreRepository.findByWeekStart(monday);
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(result).isEqualTo(WeeklyRewardDrawResult.NO_PARTICIPANTS);
            softAssertions.assertThat(weeklyTopScore).isEmpty();
        });
    }

    @DisplayName("그 주에 아직 채점되지 않은(SUBMITTED) 예측이 남아있으면 추첨을 건너뛴다")
    @Test
    void drawWinners_skipsWhenUngradedPredictionsExist() {
        // given
        Member twoWins = memberFactory.save(b -> b.team(homeTeam));
        Member oneWin = memberFactory.save(b -> b.team(homeTeam));
        saveResolvedPredictions(twoWins, oneWin);
        saveUngradedPrediction();

        // when
        WeeklyRewardDrawResult result = weeklyRewardDrawService.drawWinnersForLastCompletedWeek();

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(result).isEqualTo(WeeklyRewardDrawResult.UNGRADED_PREDICTIONS_EXIST);
            softAssertions.assertThat(weeklyTopScoreRepository.findByWeekStart(monday)).isEmpty();
        });
    }

    @DisplayName("주간 배치는 미채점 예측을 보정한 후 같은 실행에서 당첨자를 추첨한다")
    @Test
    void reconcilePredictionsAndDrawWinners_reconcilesBeforeDraw() {
        // given
        Member twoWins = memberFactory.save(b -> b.team(homeTeam));
        Member oneWin = memberFactory.save(b -> b.team(homeTeam));
        saveSubmittedPredictions(twoWins, oneWin);

        // when
        WeeklyRewardDrawResult result = weeklyRewardDrawBatchService.reconcilePredictionsAndDrawWinners();

        // then
        WeeklyTopScore weeklyTopScore = weeklyTopScoreRepository.findByWeekStart(monday).orElseThrow();
        List<GifticonIssuance> issuances = gifticonIssuanceRepository.findAllByWeeklyTopScore(weeklyTopScore);
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(result).isEqualTo(WeeklyRewardDrawResult.DRAWN);
            softAssertions.assertThat(weeklyTopScore.getTopScore()).isEqualTo(2);
            softAssertions.assertThat(issuances).hasSize(1);
            softAssertions.assertThat(issuances.get(0).getMember().getId()).isEqualTo(twoWins.getId());
        });
    }

    @DisplayName("그 주에 아직 채점되지 않은(SUBMITTED) 예측이 남아있으면 수동 추첨도 거부한다")
    @Test
    void drawWinnersForWeek_throwsWhenUngradedPredictionsExist() {
        // given
        saveUngradedPrediction();

        // when & then
        assertThatThrownBy(() -> weeklyRewardDrawService.drawWinnersForWeek(monday))
                .isInstanceOf(UnprocessableEntityException.class);
    }

    private void saveUngradedPrediction() {
        Game game = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(homeTeam).awayTeam(awayTeam)
                .date(monday.plusDays(2))
                .homeScore(5).awayScore(3)
                .gameState(GameState.COMPLETED));
        Member submitter = memberFactory.save(b -> b.team(homeTeam));
        gamePredictionRepository.save(new GamePrediction(submitter, game, PredictionPick.HOME));
    }

    @DisplayName("정기 추첨을 건너뛴 주는 채점 완료 후 수동으로 추첨할 수 있다")
    @Test
    void drawWinnersForWeek_drawsSkippedWeek() {
        // given
        Member twoWins = memberFactory.save(b -> b.team(homeTeam));
        Member oneWin = memberFactory.save(b -> b.team(homeTeam));
        saveSubmittedPredictions(twoWins, oneWin);
        weeklyRewardDrawService.drawWinnersForLastCompletedWeek();
        assertThat(weeklyTopScoreRepository.findByWeekStart(monday)).isEmpty();
        predictionResultService.reconcileUngradedPredictions();

        // when
        weeklyRewardDrawService.drawWinnersForWeek(monday);

        // then
        WeeklyTopScore weeklyTopScore = weeklyTopScoreRepository.findByWeekStart(monday).orElseThrow();
        List<GifticonIssuance> issuances = gifticonIssuanceRepository.findAllByWeeklyTopScore(weeklyTopScore);

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(weeklyTopScore.getTopScore()).isEqualTo(2);
            softAssertions.assertThat(issuances).hasSize(1);
            softAssertions.assertThat(issuances.get(0).getMember().getId()).isEqualTo(twoWins.getId());
        });
    }

    @DisplayName("이미 당첨자를 뽑은 주는 수동 추첨을 거부하고 기존 기록을 유지한다")
    @Test
    void drawWinnersForWeek_rejectsAlreadyDrawnWeek() {
        // given
        Member twoWins = memberFactory.save(b -> b.team(homeTeam));
        Member oneWin = memberFactory.save(b -> b.team(homeTeam));
        saveResolvedPredictions(twoWins, oneWin);
        weeklyRewardDrawService.drawWinnersForLastCompletedWeek();
        WeeklyTopScore existingDraw = weeklyTopScoreRepository.findByWeekStart(monday).orElseThrow();
        Set<Long> existingWinnerIds = winnerMemberIds(existingDraw);

        // when & then
        assertThatThrownBy(() -> weeklyRewardDrawService.drawWinnersForWeek(monday))
                .isInstanceOf(ConflictException.class);
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(weeklyTopScoreRepository.findById(existingDraw.getId())).isPresent();
            softAssertions.assertThat(winnerMemberIds(existingDraw)).isEqualTo(existingWinnerIds);
        });
    }

    private void saveResolvedPredictions(final Member twoWins, final Member oneWin) {
        saveSubmittedPredictions(twoWins, oneWin);
        predictionResultService.reconcileUngradedPredictions();
    }

    private void saveSubmittedPredictions(final Member twoWins, final Member oneWin) {
        Game game1 = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(homeTeam).awayTeam(awayTeam)
                .date(monday)
                .homeScore(5).awayScore(3)
                .gameState(GameState.COMPLETED));
        Game game2 = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(homeTeam).awayTeam(awayTeam)
                .date(monday.plusDays(1))
                .homeScore(4).awayScore(1)
                .gameState(GameState.COMPLETED));

        gamePredictionRepository.save(new GamePrediction(twoWins, game1, PredictionPick.HOME));
        gamePredictionRepository.save(new GamePrediction(twoWins, game2, PredictionPick.HOME));
        gamePredictionRepository.save(new GamePrediction(oneWin, game1, PredictionPick.HOME));
    }
}
