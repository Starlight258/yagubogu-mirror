package yagubogu.crawling.game.dto;

import java.util.List;

public record ReviewData(
        String gameCode,
        List<HitterRecordDto> awayHitters,
        List<HitterRecordDto> homeHitters,
        List<PitcherRecordDto> awayPitchers,
        List<PitcherRecordDto> homePitchers
) {
}