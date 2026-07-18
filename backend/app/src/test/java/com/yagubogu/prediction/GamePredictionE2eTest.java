package com.yagubogu.prediction;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.hamcrest.CoreMatchers.is;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.game.domain.Game;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.Role;
import com.yagubogu.prediction.domain.GamePrediction;
import com.yagubogu.prediction.domain.PredictionPick;
import com.yagubogu.prediction.domain.PredictionStatus;
import com.yagubogu.prediction.dto.v1.CreateGamePredictionRequest;
import com.yagubogu.prediction.dto.v1.GamePredictionResponse;
import com.yagubogu.prediction.dto.v1.UpdateGamePredictionRequest;
import com.yagubogu.prediction.repository.GamePredictionRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.auth.AuthFactory;
import com.yagubogu.support.base.E2eTestBase;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
public class GamePredictionE2eTest extends E2eTestBase {

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

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("승부 예측을 제출한다")
    @Test
    void submitPrediction() {
        // given
        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .date(LocalDate.now().plusDays(1)));
        Member member = memberFactory.save(b -> b.team(homeTeam));
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        CreateGamePredictionRequest request = new CreateGamePredictionRequest(game.getId(), PredictionPick.HOME);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(request)
                .when()
                .post("/api/v1/predictions")
                .then().log().all()
                .statusCode(201)
                .body("gameId", is(game.getId().intValue()))
                .body("pick", is("HOME"))
                .body("status", is("SUBMITTED"));
    }

    @DisplayName("예외: 이미 해당 경기에 예측을 제출했으면 409를 반환한다")
    @Test
    void submitPrediction_alreadyExists() {
        // given
        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .date(LocalDate.now().plusDays(1)));
        Member member = memberFactory.save(b -> b.team(homeTeam));
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        CreateGamePredictionRequest request = new CreateGamePredictionRequest(game.getId(), PredictionPick.HOME);
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(request)
                .when()
                .post("/api/v1/predictions");

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(request)
                .when()
                .post("/api/v1/predictions")
                .then().log().all()
                .statusCode(409);
    }

    @DisplayName("본인의 승부 예측을 조회한다")
    @Test
    void findPrediction() {
        // given
        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .date(LocalDate.now().plusDays(1)));
        Member member = memberFactory.save(b -> b.team(homeTeam));
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        CreateGamePredictionRequest request = new CreateGamePredictionRequest(game.getId(), PredictionPick.AWAY);
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(request)
                .when()
                .post("/api/v1/predictions");

        // when
        GamePredictionResponse actual = RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("gameId", game.getId())
                .when()
                .get("/api/v1/predictions")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(GamePredictionResponse.class);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.gameId()).isEqualTo(game.getId());
            softAssertions.assertThat(actual.pick()).isEqualTo(PredictionPick.AWAY);
            softAssertions.assertThat(actual.status()).isEqualTo(PredictionStatus.SUBMITTED);
        });
    }

    @DisplayName("예외: 예측이 없으면 404를 반환한다")
    @Test
    void findPrediction_notFound() {
        // given
        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .date(LocalDate.now().plusDays(1)));
        Member member = memberFactory.save(b -> b.team(homeTeam));
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        // when & then
        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("gameId", game.getId())
                .when()
                .get("/api/v1/predictions")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("승부 예측을 수정한다")
    @Test
    void updatePrediction() {
        // given
        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .date(LocalDate.now().plusDays(1)));
        Member member = memberFactory.save(b -> b.team(homeTeam));
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        CreateGamePredictionRequest createRequest = new CreateGamePredictionRequest(game.getId(), PredictionPick.HOME);
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(createRequest)
                .when()
                .post("/api/v1/predictions");

        UpdateGamePredictionRequest updateRequest = new UpdateGamePredictionRequest(game.getId(), PredictionPick.AWAY);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(updateRequest)
                .when()
                .put("/api/v1/predictions")
                .then().log().all()
                .statusCode(200)
                .body("gameId", is(game.getId().intValue()))
                .body("pick", is("AWAY"))
                .body("status", is("SUBMITTED"));
    }

    @DisplayName("예외: 예측이 없으면 수정 시 404를 반환한다")
    @Test
    void updatePrediction_notFound() {
        // given
        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .date(LocalDate.now().plusDays(1)));
        Member member = memberFactory.save(b -> b.team(homeTeam));
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        UpdateGamePredictionRequest updateRequest = new UpdateGamePredictionRequest(game.getId(), PredictionPick.AWAY);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(updateRequest)
                .when()
                .put("/api/v1/predictions")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("예외: 경기가 이미 시작했으면 제출 시 422를 반환한다")
    @Test
    void submitPrediction_afterClose() {
        // given
        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .date(LocalDate.now().minusDays(1)));
        Member member = memberFactory.save(b -> b.team(homeTeam));
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        CreateGamePredictionRequest request = new CreateGamePredictionRequest(game.getId(), PredictionPick.HOME);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(request)
                .when()
                .post("/api/v1/predictions")
                .then().log().all()
                .statusCode(422);
    }

    @DisplayName("예외: 경기가 이미 시작했으면 수정 시 422를 반환한다")
    @Test
    void updatePrediction_afterClose() {
        // given
        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .date(LocalDate.now().minusDays(1)));
        Member member = memberFactory.save(b -> b.team(homeTeam));
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        gamePredictionRepository.save(new GamePrediction(member, game, PredictionPick.HOME));

        UpdateGamePredictionRequest updateRequest = new UpdateGamePredictionRequest(game.getId(), PredictionPick.AWAY);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(updateRequest)
                .when()
                .put("/api/v1/predictions")
                .then().log().all()
                .statusCode(422);
    }

    @DisplayName("예외: pick이 없으면 예측 제출 시 400을 반환한다")
    @Test
    void submitPrediction_missingPick() {
        // given
        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .date(LocalDate.now().plusDays(1)));
        Member member = memberFactory.save(b -> b.team(homeTeam));
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        CreateGamePredictionRequest request = new CreateGamePredictionRequest(game.getId(), null);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(request)
                .when()
                .post("/api/v1/predictions")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("예외: pick이 없으면 예측 수정 시 400을 반환한다")
    @Test
    void updatePrediction_missingPick() {
        // given
        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .date(LocalDate.now().plusDays(1)));
        Member member = memberFactory.save(b -> b.team(homeTeam));
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        gamePredictionRepository.save(new GamePrediction(member, game, PredictionPick.HOME));

        UpdateGamePredictionRequest updateRequest = new UpdateGamePredictionRequest(game.getId(), null);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(updateRequest)
                .when()
                .put("/api/v1/predictions")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("예외: 존재하지 않는 경기를 예측하면 404를 반환한다")
    @Test
    void submitPrediction_notFoundGame() {
        // given
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Member member = memberFactory.save(b -> b.team(homeTeam));
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        long invalidGameId = 999999L;
        CreateGamePredictionRequest request = new CreateGamePredictionRequest(invalidGameId, PredictionPick.HOME);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(request)
                .when()
                .post("/api/v1/predictions")
                .then().log().all()
                .statusCode(404);
    }
}
