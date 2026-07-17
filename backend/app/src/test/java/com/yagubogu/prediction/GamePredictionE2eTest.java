package com.yagubogu.prediction;

import static org.hamcrest.CoreMatchers.is;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.game.domain.Game;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.Role;
import com.yagubogu.prediction.domain.PredictionPick;
import com.yagubogu.prediction.dto.v1.CreateGamePredictionRequest;
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
                .stadium(stadium));
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
