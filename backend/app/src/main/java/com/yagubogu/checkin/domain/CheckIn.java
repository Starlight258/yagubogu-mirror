package com.yagubogu.checkin.domain;

import com.yagubogu.game.domain.Game;
import com.yagubogu.global.domain.BaseEntity;
import com.yagubogu.member.domain.Member;
import com.yagubogu.team.domain.Team;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(
        name = "check_ins",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"game_id", "member_id"})
        })
@Entity
public class CheckIn extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "check_ins_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(name = "check_in_type", nullable = false)
    private CheckInType checkInType;

    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;

    public CheckIn(final Game game, final Member member, final Team team, final CheckInType checkInType, final String memo, final String imageUrl) {
        this.game = game;
        this.member = member;
        this.team = team;
        this.checkInType = checkInType;
        this.memo = memo;
    }

    public void updateMemo(final String memo) {
        this.memo = memo;
    }
}
