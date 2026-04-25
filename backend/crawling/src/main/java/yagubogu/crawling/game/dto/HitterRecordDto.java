package yagubogu.crawling.game.dto;

public record HitterRecordDto(
        int battingOrder,
        String position,
        String playerName,
        int atBats,
        int hits,
        int rbi,
        int runs
) {
}
