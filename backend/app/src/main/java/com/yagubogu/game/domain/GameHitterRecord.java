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
@Table(name = "game_hitter_records")
@Entity
public class GameHitterRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "is_home_team", nullable = false)
    private boolean homeTeam;

    @Column(name = "batting_order", nullable = false)
    private int battingOrder;

    @Column(name = "position", nullable = false)
    private String position;

    @Column(name = "player_name", nullable = false)
    private String playerName;

    @Column(name = "at_bats", nullable = false)
    private int atBats;

    @Column(name = "hits", nullable = false)
    private int hits;

    @Column(name = "rbi", nullable = false)
    private int rbi;

    @Column(name = "runs", nullable = false)
    private int runs;

    public GameHitterRecord(final Game game, final boolean homeTeam, final int battingOrder,
                            final String position, final String playerName,
                            final int atBats, final int hits, final int rbi, final int runs) {
        this.game = game;
        this.homeTeam = homeTeam;
        this.battingOrder = battingOrder;
        this.position = position;
        this.playerName = playerName;
        this.atBats = atBats;
        this.hits = hits;
        this.rbi = rbi;
        this.runs = runs;
    }
}
