package com.yagubogu.prediction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.game.domain.Game;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.member.domain.Member;
import com.yagubogu.prediction.domain.GamePrediction;
import com.yagubogu.prediction.domain.PredictionPick;
import com.yagubogu.prediction.repository.GamePredictionRepository;
import com.yagubogu.sse.dto.GameWithPredictionRateParam;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.base.ServiceUsingMysqlTestBase;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
class GamePredictionServiceUsingMysqlTest extends ServiceUsingMysqlTestBase {

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

    @Autowired
    private Clock clock;

    private Team homeTeam, awayTeam;
    private Stadium stadium;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now(clock);
        homeTeam = teamRepository.findByTeamCode("HT").orElseThrow();
        awayTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        stadium = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();
    }

    @DisplayName("오늘 경기의 홈/원정 예측 비율을 계산한다")
    @Test
    void buildPredictionEventData_calculatesRate() {
        // given
        Game game = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(homeTeam).awayTeam(awayTeam)
                .date(today));

        Member homePicker1 = memberFactory.save(b -> b.team(homeTeam));
        Member homePicker2 = memberFactory.save(b -> b.team(homeTeam));
        Member homePicker3 = memberFactory.save(b -> b.team(homeTeam));
        Member awayPicker = memberFactory.save(b -> b.team(awayTeam));

        gamePredictionRepository.save(new GamePrediction(homePicker1, game, PredictionPick.HOME));
        gamePredictionRepository.save(new GamePrediction(homePicker2, game, PredictionPick.HOME));
        gamePredictionRepository.save(new GamePrediction(homePicker3, game, PredictionPick.HOME));
        gamePredictionRepository.save(new GamePrediction(awayPicker, game, PredictionPick.AWAY));

        // when
        List<GameWithPredictionRateParam> results = gamePredictionService.buildPredictionEventData();

        // then
        GameWithPredictionRateParam actual = results.stream()
                .filter(r -> r.gameId() == game.getId())
                .findFirst().orElseThrow();

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.homeTeam().predictionRate()).isEqualTo(75.0);
            softAssertions.assertThat(actual.awayTeam().predictionRate()).isEqualTo(25.0);
        });
    }

    @DisplayName("예측이 하나도 없는 오늘 경기는 비율이 0으로 나온다")
    @Test
    void buildPredictionEventData_zeroWhenNoPredictions() {
        // given
        Game game = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(homeTeam).awayTeam(awayTeam)
                .date(today));

        // when
        List<GameWithPredictionRateParam> results = gamePredictionService.buildPredictionEventData();

        // then
        GameWithPredictionRateParam actual = results.stream()
                .filter(r -> r.gameId() == game.getId())
                .findFirst().orElseThrow();

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.homeTeam().predictionRate()).isEqualTo(0.0);
            softAssertions.assertThat(actual.awayTeam().predictionRate()).isEqualTo(0.0);
        });
    }

    @DisplayName("오늘이 아닌 경기는 결과에 포함되지 않는다")
    @Test
    void buildPredictionEventData_excludesOtherDates() {
        // given
        Game game = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(homeTeam).awayTeam(awayTeam)
                .date(today.minusDays(1)));

        // when
        List<GameWithPredictionRateParam> results = gamePredictionService.buildPredictionEventData();

        // then
        assertThat(results).noneMatch(r -> r.gameId() == game.getId());
    }
}
