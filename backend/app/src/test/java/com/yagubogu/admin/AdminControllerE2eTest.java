package com.yagubogu.admin;

import static org.assertj.core.api.Assertions.assertThat;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.Role;
import com.yagubogu.prediction.domain.GamePrediction;
import com.yagubogu.prediction.domain.PredictionPick;
import com.yagubogu.prediction.domain.PredictionStatus;
import com.yagubogu.prediction.repository.GamePredictionRepository;
import com.yagubogu.reward.domain.WeeklyTopScore;
import com.yagubogu.reward.repository.WeeklyTopScoreRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.auth.AuthFactory;
import com.yagubogu.support.base.E2eTestBase;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import io.restassured.RestAssured;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
class AdminControllerE2eTest extends E2eTestBase {

    @LocalServerPort
    private int port;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private GameFactory gameFactory;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private AuthFactory authFactory;

    @Autowired
    private GamePredictionRepository gamePredictionRepository;

    @Autowired
    private WeeklyTopScoreRepository weeklyTopScoreRepository;

    private Team homeTeam;
    private Team awayTeam;
    private Stadium stadium;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        homeTeam = teamRepository.findByTeamCode("HT").orElseThrow();
        awayTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        stadium = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();
    }

    @DisplayName("관리자가 종료된 경기의 예측을 채점한다")
    @Test
    void gradePredictionsForGame() {
        // given
        String gameCode = "completed-game";
        Game game = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .gameCode(gameCode)
                .homeScore(5)
                .awayScore(3)
                .gameState(GameState.COMPLETED));
        Member participant = memberFactory.save(b -> b.team(homeTeam));
        GamePrediction prediction = gamePredictionRepository.save(
                new GamePrediction(participant, game, PredictionPick.HOME));
        String accessToken = accessToken(Role.ADMIN);

        // when
        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .when()
                .post("/admin/predictions/{gameCode}/grading", gameCode)
                .then().log().all()
                .statusCode(200);

        // then
        GamePrediction actual = gamePredictionRepository.findById(prediction.getId()).orElseThrow();
        assertThat(actual.getStatus()).isEqualTo(PredictionStatus.WON);
    }

    @DisplayName("관리자가 종료되지 않은 경기의 예측 채점을 요청하면 422를 반환한다")
    @Test
    void gradePredictionsForGame_notFinalized() {
        // given
        String gameCode = "not-finalized-game";
        gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .gameCode(gameCode)
                .gameState(GameState.LIVE));
        String accessToken = accessToken(Role.ADMIN);

        // when & then
        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .when()
                .post("/admin/predictions/{gameCode}/grading", gameCode)
                .then().log().all()
                .statusCode(422);
    }

    @DisplayName("관리자가 미채점 예측이 남은 주의 추첨을 요청하면 422를 반환한다")
    @Test
    void drawWeeklyRewardWinners_ungradedPredictionsExist() {
        // given
        LocalDate monday = LocalDate.of(2025, 7, 21);
        Game game = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .date(monday)
                .gameState(GameState.COMPLETED));
        Member participant = memberFactory.save(b -> b.team(homeTeam));
        gamePredictionRepository.save(new GamePrediction(participant, game, PredictionPick.HOME));
        String accessToken = accessToken(Role.ADMIN);

        // when & then
        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("monday", monday.toString())
                .when()
                .post("/admin/rewards/weekly-draws")
                .then().log().all()
                .statusCode(422);
    }

    @DisplayName("관리자가 채점이 끝난 주의 보상 당첨자를 추첨한다")
    @Test
    void drawWeeklyRewardWinners() {
        // given
        LocalDate monday = LocalDate.of(2025, 7, 21);
        Game game = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .date(monday)
                .homeScore(5)
                .awayScore(3)
                .gameState(GameState.COMPLETED));
        Member participant = memberFactory.save(b -> b.team(homeTeam));
        GamePrediction prediction = new GamePrediction(participant, game, PredictionPick.HOME);
        prediction.grade(game);
        gamePredictionRepository.save(prediction);
        String accessToken = accessToken(Role.ADMIN);

        // when
        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("monday", monday.toString())
                .when()
                .post("/admin/rewards/weekly-draws")
                .then().log().all()
                .statusCode(200);

        // then
        WeeklyTopScore actual = weeklyTopScoreRepository.findByWeekStart(monday).orElseThrow();
        assertThat(actual.getTopScore()).isEqualTo(1);
    }

    @DisplayName("일반 사용자가 관리자 API를 호출하면 403을 반환한다")
    @Test
    void gradePredictionsForGame_forbiddenForUser() {
        // given
        String accessToken = accessToken(Role.USER);

        // when & then
        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .when()
                .post("/admin/predictions/{gameCode}/grading", "any-game")
                .then().log().all()
                .statusCode(403);
    }

    private String accessToken(final Role role) {
        Member member = memberFactory.save(b -> b.team(homeTeam).role(role));
        return authFactory.getAccessTokenByMemberId(member.getId(), role);
    }
}
