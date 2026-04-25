package com.yagubogu.checkin.dto.v1;

import com.yagubogu.game.domain.GameHitterRecord;
import com.yagubogu.game.domain.GamePitcherRecord;
import com.yagubogu.game.domain.HitterPosition;
import com.yagubogu.game.domain.Innings;
import java.util.List;

public record CheckInReviewResponse(
        List<HitterRecordItem> homeHitters,
        List<HitterRecordItem> awayHitters,
        List<PitcherRecordItem> homePitchers,
        List<PitcherRecordItem> awayPitchers
) {

    public static CheckInReviewResponse from(
            final List<GameHitterRecord> hitters,
            final List<GamePitcherRecord> pitchers
    ) {
        List<HitterRecordItem> homeHitters = hitters.stream()
                .filter(GameHitterRecord::isHomeTeam)
                .map(HitterRecordItem::from)
                .toList();
        List<HitterRecordItem> awayHitters = hitters.stream()
                .filter(r -> !r.isHomeTeam())
                .map(HitterRecordItem::from)
                .toList();
        List<PitcherRecordItem> homePitchers = pitchers.stream()
                .filter(GamePitcherRecord::isHomeTeam)
                .map(PitcherRecordItem::from)
                .toList();
        List<PitcherRecordItem> awayPitchers = pitchers.stream()
                .filter(r -> !r.isHomeTeam())
                .map(PitcherRecordItem::from)
                .toList();

        return new CheckInReviewResponse(homeHitters, awayHitters, homePitchers, awayPitchers);
    }

    public record HitterRecordItem(
            int battingOrder,
            String position,
            String playerName,
            int atBats,
            int hits,
            int rbi,
            int runs
    ) {
        public static HitterRecordItem from(final GameHitterRecord record) {
            return new HitterRecordItem(
                    record.getBattingOrder(),
                    HitterPosition.normalize(record.getPosition()),
                    record.getPlayerName(),
                    record.getAtBats(),
                    record.getHits(),
                    record.getRbi(),
                    record.getRuns()
            );
        }
    }

    public record PitcherRecordItem(
            String playerName,
            String result,
            double innings,
            int battersFaced,
            int pitchCount,
            int atBats,
            int hitsAllowed,
            int homeRunsAllowed,
            int walksAndHbp,
            int strikeouts,
            int runsAllowed,
            int earnedRuns
    ) {
        public static PitcherRecordItem from(final GamePitcherRecord record) {
            return new PitcherRecordItem(
                    record.getPlayerName(),
                    record.getResult(),
                    Innings.parse(record.getInnings()),
                    record.getBattersFaced(),
                    record.getPitchCount(),
                    record.getAtBats(),
                    record.getHitsAllowed(),
                    record.getHomeRunsAllowed(),
                    record.getWalksAndHbp(),
                    record.getStrikeouts(),
                    record.getRunsAllowed(),
                    record.getEarnedRuns()
            );
        }
    }
}
