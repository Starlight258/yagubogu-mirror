package yagubogu.crawling.game.dto;

public record PitcherRecordDto(
        String playerName,
        String result,
        String innings,
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
}