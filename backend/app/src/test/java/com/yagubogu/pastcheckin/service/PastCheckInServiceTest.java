package com.yagubogu.pastcheckin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.checkin.domain.CheckIn;
import com.yagubogu.checkin.domain.CheckInType;
import com.yagubogu.checkin.dto.CreatePastCheckInRequest;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.checkin.service.PastCheckInService;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.global.exception.BadRequestException;
import com.yagubogu.global.exception.ConflictException;
import com.yagubogu.global.exception.ForbiddenException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.TestFixture;
import com.yagubogu.support.checkin.CheckInFactory;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
@DataJpaTest
class PastCheckInServiceTest {

    private PastCheckInService pastCheckInService;

    @Autowired
    private CheckInFactory checkInFactory;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private GameFactory gameFactory;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private CheckInRepository checkInRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private GameRepository gameRepository;

    private Team lotte, kia;
    private Stadium stadiumGocheok;

    @BeforeEach
    void setUp() {
        pastCheckInService = new PastCheckInService(
                memberRepository,
                gameRepository,
                checkInRepository
        );

        lotte = teamRepository.findByTeamCode("LT").orElseThrow();
        kia = teamRepository.findByTeamCode("HT").orElseThrow();
        stadiumGocheok = stadiumRepository.findById(3L).orElseThrow();
    }

    @DisplayName("PastCheckIn을 생성한다")
    @Test
    void createPastCheckIn_success() {
        // given
        Member member = memberFactory.save(b -> b.team(lotte));
        LocalDate date = LocalDate.of(2025, 1, 1);

        // game 생성 (stadium + date 매칭)
        Game game = gameFactory.save(b -> b
                .stadium(stadiumGocheok)
                .date(date)
                .homeTeam(lotte).homeScore(5).homeScoreBoard(TestFixture.getHomeScoreBoard())
                .awayTeam(kia).awayScore(3).awayScoreBoard(TestFixture.getAwayScoreBoard())
                .gameState(GameState.COMPLETED)
        );

        CreatePastCheckInRequest request = new CreatePastCheckInRequest(game.getId());

        // when
        pastCheckInService.createPastCheckIn(member.getId(), request);

        // then
        boolean exists = checkInRepository.existsByMemberAndGame(member, game);
        assertThat(exists).isTrue();
    }

    @DisplayName("예외: 동일 날짜에 이미 CheckIn이 있는 경우 BadRequest가 발생한다")
    @Test
    void createPastCheckIn_whenCheckInExists() {
        // given
        Member member = memberFactory.save(b -> b.team(lotte));
        LocalDate date = LocalDate.of(2025, 2, 2);

        Game game = gameFactory.save(b -> b
                .stadium(stadiumGocheok)
                .date(date)
                .homeTeam(lotte).homeScore(4).homeScoreBoard(TestFixture.getHomeScoreBoard())
                .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard())
                .gameState(GameState.COMPLETED)
        );

        checkInFactory.save(b -> b.team(lotte).member(member).game(game));

        CreatePastCheckInRequest request = new CreatePastCheckInRequest(game.getId());

        // when & then
        assertThatThrownBy(() -> pastCheckInService.createPastCheckIn(member.getId(), request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("CheckIn already exists");
    }

    @DisplayName("예외: 동일 날짜에 이미 PastCheckIn이 있는 경우 BadRequest가 발생한다")
    @Test
    void createPastCheckIn_fail_whenPastCheckInExists() {
        // given
        Member member = memberFactory.save(b -> b.team(lotte));
        LocalDate date = LocalDate.of(2025, 3, 3);

        Game game = gameFactory.save(b -> b
                .stadium(stadiumGocheok)
                .date(date)
                .homeTeam(lotte).homeScore(6).homeScoreBoard(TestFixture.getHomeScoreBoard())
                .awayTeam(kia).awayScore(2).awayScoreBoard(TestFixture.getAwayScoreBoard())
                .gameState(GameState.COMPLETED)
        );

        checkInFactory.save(b -> b.game(game).member(member).team(lotte));

        CreatePastCheckInRequest request = new CreatePastCheckInRequest(game.getId());

        // when & then
        assertThatThrownBy(() -> pastCheckInService.createPastCheckIn(member.getId(), request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("CheckIn already exists");
    }

    @DisplayName("예외: 경기가 존재하지 않으면 NotFoundException이 발생한다")
    @Test
    void createPastCheckIn_fail_whenStadiumNotFound() {
        // given
        Member member = memberFactory.save(b -> b.team(lotte));

        long nonExistingGameId = 9999L;
        CreatePastCheckInRequest request = new CreatePastCheckInRequest(nonExistingGameId);

        // when / then
        assertThatThrownBy(() -> pastCheckInService.createPastCheckIn(member.getId(), request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Game is not found");
    }

    @DisplayName("예외: game이 존재하지 않으면 NotFoundException 발생한다")
    @Test
    void createPastCheckIn_fail_whenGameNotFound() {
        // given
        Member member = memberFactory.save(b -> b.team(lotte));

        // 같은 구장 but 해당 날짜에 게임이 없음
        CreatePastCheckInRequest request = new CreatePastCheckInRequest(999L);

        // when & then
        assertThatThrownBy(() -> pastCheckInService.createPastCheckIn(member.getId(), request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Game is not found");
    }

    @DisplayName("에외: member가 존재하지 않으면 NotFoundException 발생한다")
    @Test
    void createPastCheckIn_fail_whenMemberNotFound() {
        // given
        long nonExistingMemberId = 9999L;
        LocalDate date = LocalDate.of(2025, 6, 6);

        // game은 존재하도록 생성
        Game game = gameFactory.save(b -> b
                .stadium(stadiumGocheok)
                .date(date)
                .homeTeam(lotte).homeScore(2).homeScoreBoard(TestFixture.getHomeScoreBoard())
                .awayTeam(kia).awayScore(0).awayScoreBoard(TestFixture.getAwayScoreBoard())
                .gameState(GameState.COMPLETED)
        );

        CreatePastCheckInRequest request = new CreatePastCheckInRequest(game.getId());

        // when & then
        assertThatThrownBy(() -> pastCheckInService.createPastCheckIn(nonExistingMemberId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Member is not found");
    }

    @DisplayName("PastCheckIn(NON_LOCATION_CHECK_IN)을 삭제한다")
    @Test
    void deletePastCheckIn_success() {
        // given
        Member member = memberFactory.save(b -> b.team(lotte));
        LocalDate date = LocalDate.of(2025, 7, 7);

        Game game = gameFactory.save(b -> b
                .stadium(stadiumGocheok)
                .date(date)
                .homeTeam(lotte).homeScore(3).homeScoreBoard(TestFixture.getHomeScoreBoard())
                .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard())
                .gameState(GameState.COMPLETED)
        );

        CheckIn checkIn = checkInFactory.save(b -> b
                .game(game).member(member).team(lotte)
                .checkInType(CheckInType.NON_LOCATION_CHECK_IN)
        );

        // when
        pastCheckInService.deletePastCheckIn(member.getId(), checkIn.getId());

        // then
        boolean exists = checkInRepository.existsById(checkIn.getId());
        assertThat(exists).isFalse();
    }

    @DisplayName("예외: 존재하지 않는 CheckIn을 삭제하면 NotFoundException이 발생한다")
    @Test
    void deletePastCheckIn_fail_whenCheckInNotFound() {
        // given
        Member member = memberFactory.save(b -> b.team(lotte));
        long nonExistingCheckInId = 9999L;

        // when & then
        assertThatThrownBy(() -> pastCheckInService.deletePastCheckIn(member.getId(), nonExistingCheckInId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("CheckIn is not found");
    }

    @DisplayName("예외: LOCATION_CHECK_IN은 삭제할 수 없다")
    @Test
    void deletePastCheckIn_fail_whenLocationCheckIn() {
        // given
        Member member = memberFactory.save(b -> b.team(lotte));
        LocalDate date = LocalDate.of(2025, 8, 8);

        Game game = gameFactory.save(b -> b
                .stadium(stadiumGocheok)
                .date(date)
                .homeTeam(lotte).homeScore(5).homeScoreBoard(TestFixture.getHomeScoreBoard())
                .awayTeam(kia).awayScore(4).awayScoreBoard(TestFixture.getAwayScoreBoard())
                .gameState(GameState.COMPLETED)
        );

        CheckIn checkIn = checkInFactory.save(b -> b
                .game(game).member(member).team(lotte)
                .checkInType(CheckInType.LOCATION_CHECK_IN)
        );

        // when & then
        assertThatThrownBy(() -> pastCheckInService.deletePastCheckIn(member.getId(), checkIn.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Location-based check-in cannot be deleted");
    }

    @DisplayName("예외: 다른 회원의 CheckIn은 삭제할 수 없다")
    @Test
    void deletePastCheckIn_fail_whenNotOwner() {
        // given
        Member owner = memberFactory.save(b -> b.team(lotte));
        Member otherMember = memberFactory.save(b -> b.team(lotte));
        LocalDate date = LocalDate.of(2025, 9, 9);

        Game game = gameFactory.save(b -> b
                .stadium(stadiumGocheok)
                .date(date)
                .homeTeam(lotte).homeScore(7).homeScoreBoard(TestFixture.getHomeScoreBoard())
                .awayTeam(kia).awayScore(2).awayScoreBoard(TestFixture.getAwayScoreBoard())
                .gameState(GameState.COMPLETED)
        );

        CheckIn checkIn = checkInFactory.save(b -> b
                .game(game).member(owner).team(lotte)
                .checkInType(CheckInType.NON_LOCATION_CHECK_IN)
        );

        // when & then
        assertThatThrownBy(() -> pastCheckInService.deletePastCheckIn(otherMember.getId(), checkIn.getId()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Only your own check-in can be deleted");
    }
}

