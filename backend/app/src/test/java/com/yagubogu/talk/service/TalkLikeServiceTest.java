package com.yagubogu.talk.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.support.talk.TalkFactory;
import com.yagubogu.support.talk.TalkLikeFactory;
import com.yagubogu.talk.domain.Talk;
import com.yagubogu.talk.dto.v1.TalkLikeResponse;
import com.yagubogu.talk.repository.TalkLikeRepository;
import com.yagubogu.talk.repository.TalkRepository;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@DataJpaTest
class TalkLikeServiceTest {

    private TalkLikeService talkLikeService;

    @Autowired
    private TalkLikeRepository talkLikeRepository;

    @Autowired
    private TalkRepository talkRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TalkFactory talkFactory;

    @Autowired
    private TalkLikeFactory talkLikeFactory;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private GameFactory gameFactory;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private GameRepository gameRepository;

    @BeforeEach
    void setUp() {
        talkLikeService = new TalkLikeService(talkLikeRepository, talkRepository, memberRepository);
    }

    @DisplayName("톡에 좋아요를 누르면 liked=true와 likeCount=1이 반환된다")
    @Test
    void toggleLike_like() {
        // given
        Team team = teamRepository.findByTeamCode("HH").orElseThrow();
        Member me = memberFactory.save(builder -> builder.team(team));
        Member writer = memberFactory.save(builder -> builder.team(team));

        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(homeTeam).awayTeam(awayTeam).stadium(stadium));

        Talk talk = talkFactory.save(builder -> builder.member(writer).game(game));

        // when
        TalkLikeResponse response = talkLikeService.toggleLike(talk.getId(), me.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.liked()).isTrue();
            softly.assertThat(response.likeCount()).isEqualTo(1L);
        });
    }

    @DisplayName("좋아요를 누른 톡에 다시 누르면 liked=false와 likeCount=0이 반환된다")
    @Test
    void toggleLike_unlike() {
        // given
        Team team = teamRepository.findByTeamCode("HH").orElseThrow();
        Member me = memberFactory.save(builder -> builder.team(team));
        Member writer = memberFactory.save(builder -> builder.team(team));

        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(homeTeam).awayTeam(awayTeam).stadium(stadium));

        Talk talk = talkFactory.save(builder -> builder.member(writer).game(game));
        talkLikeFactory.save(builder -> builder.talk(talk).member(me));

        // when
        TalkLikeResponse response = talkLikeService.toggleLike(talk.getId(), me.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.liked()).isFalse();
            softly.assertThat(response.likeCount()).isEqualTo(0L);
        });
    }

    @DisplayName("여러 명이 좋아요를 누르면 likeCount가 누적된다")
    @Test
    void toggleLike_multipleUsers() {
        // given
        Team team = teamRepository.findByTeamCode("HH").orElseThrow();
        Member me = memberFactory.save(builder -> builder.team(team));
        Member other1 = memberFactory.save(builder -> builder.team(team));
        Member other2 = memberFactory.save(builder -> builder.team(team));
        Member writer = memberFactory.save(builder -> builder.team(team));

        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(homeTeam).awayTeam(awayTeam).stadium(stadium));

        Talk talk = talkFactory.save(builder -> builder.member(writer).game(game));
        talkLikeFactory.save(builder -> builder.talk(talk).member(other1));
        talkLikeFactory.save(builder -> builder.talk(talk).member(other2));

        // when
        TalkLikeResponse response = talkLikeService.toggleLike(talk.getId(), me.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.liked()).isTrue();
            softly.assertThat(response.likeCount()).isEqualTo(3L);
        });
    }

    @DisplayName("좋아요를 누른 후 취소하면 다시 누를 수 있다")
    @Test
    void toggleLike_likeAfterUnlike() {
        // given
        Team team = teamRepository.findByTeamCode("HH").orElseThrow();
        Member me = memberFactory.save(builder -> builder.team(team));
        Member writer = memberFactory.save(builder -> builder.team(team));

        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(homeTeam).awayTeam(awayTeam).stadium(stadium));

        Talk talk = talkFactory.save(builder -> builder.member(writer).game(game));

        talkLikeService.toggleLike(talk.getId(), me.getId()); // 좋아요
        talkLikeService.toggleLike(talk.getId(), me.getId()); // 취소

        // when
        TalkLikeResponse response = talkLikeService.toggleLike(talk.getId(), me.getId()); // 다시 좋아요

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.liked()).isTrue();
            softly.assertThat(response.likeCount()).isEqualTo(1L);
        });
    }

    @DisplayName("자신이 작성한 톡에도 좋아요를 누를 수 있다")
    @Test
    void toggleLike_ownTalk() {
        // given
        Team team = teamRepository.findByTeamCode("HH").orElseThrow();
        Member me = memberFactory.save(builder -> builder.team(team));

        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(homeTeam).awayTeam(awayTeam).stadium(stadium));

        Talk myTalk = talkFactory.save(builder -> builder.member(me).game(game));

        // when
        TalkLikeResponse response = talkLikeService.toggleLike(myTalk.getId(), me.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.liked()).isTrue();
            softly.assertThat(response.likeCount()).isEqualTo(1L);
        });
    }
}
