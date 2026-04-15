package com.yagubogu.game.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "game_pitcher_records")
@Entity
public class GamePitcherRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "is_home_team", nullable = false)
    private boolean homeTeam;

    @Column(name = "player_name", nullable = false)
    private String playerName;

    @Column(name = "result")
    private String result;

    @Column(name = "innings", nullable = false)
    private String innings;

    @Column(name = "batters_faced", nullable = false)
    private int battersFaced;

    @Column(name = "pitch_count", nullable = false)
    private int pitchCount;

    @Column(name = "at_bats", nullable = false)
    private int atBats;

    @Column(name = "hits_allowed", nullable = false)
    private int hitsAllowed;

    @Column(name = "home_runs_allowed", nullable = false)
    private int homeRunsAllowed;

    @Column(name = "walks_and_hbp", nullable = false)
    private int walksAndHbp;

    @Column(name = "strikeouts", nullable = false)
    private int strikeouts;

    @Column(name = "runs_allowed", nullable = false)
    private int runsAllowed;

    @Column(name = "earned_runs", nullable = false)
    private int earnedRuns;

    public GamePitcherRecord(final Game game, final boolean homeTeam, final String playerName,
                             final String result, final String innings, final int battersFaced,
                             final int pitchCount, final int atBats, final int hitsAllowed,
                             final int homeRunsAllowed, final int walksAndHbp, final int strikeouts,
                             final int runsAllowed, final int earnedRuns) {
        this.game = game;
        this.homeTeam = homeTeam;
        this.playerName = playerName;
        this.result = result;
        this.innings = innings;
        this.battersFaced = battersFaced;
        this.pitchCount = pitchCount;
        this.atBats = atBats;
        this.hitsAllowed = hitsAllowed;
        this.homeRunsAllowed = homeRunsAllowed;
        this.walksAndHbp = walksAndHbp;
        this.strikeouts = strikeouts;
        this.runsAllowed = runsAllowed;
        this.earnedRuns = earnedRuns;
    }
}
