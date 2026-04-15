package com.yagubogu.game.service;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameHitterRecord;
import com.yagubogu.game.domain.GamePitcherRecord;
import com.yagubogu.game.dto.HitterRecordParam;
import com.yagubogu.game.dto.PitcherRecordParam;
import com.yagubogu.game.repository.GameHitterRecordRepository;
import com.yagubogu.game.repository.GamePitcherRecordRepository;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class GameReviewService {

    private final GameRepository gameRepository;
    private final GameHitterRecordRepository hitterRecordRepository;
    private final GamePitcherRecordRepository pitcherRecordRepository;

    @Transactional
    public void saveReviewData(final String gameCode,
                               final List<HitterRecordParam> awayHitters,
                               final List<HitterRecordParam> homeHitters,
                               final List<PitcherRecordParam> awayPitchers,
                               final List<PitcherRecordParam> homePitchers) {
        Game game = gameRepository.findByGameCode(gameCode)
                .orElseThrow(() -> new NotFoundException("Game not found: " + gameCode));

        hitterRecordRepository.deleteAllByGame(game);
        pitcherRecordRepository.deleteAllByGame(game);

        awayHitters.forEach(p -> hitterRecordRepository.save(toHitterRecord(game, false, p)));
        homeHitters.forEach(p -> hitterRecordRepository.save(toHitterRecord(game, true, p)));
        awayPitchers.forEach(p -> pitcherRecordRepository.save(toPitcherRecord(game, false, p)));
        homePitchers.forEach(p -> pitcherRecordRepository.save(toPitcherRecord(game, true, p)));

        log.info("[REVIEW] Saved records: gameCode={}, awayHitters={}, homeHitters={}, awayPitchers={}, homePitchers={}",
                gameCode, awayHitters.size(), homeHitters.size(), awayPitchers.size(), homePitchers.size());
    }

    private GameHitterRecord toHitterRecord(final Game game, final boolean homeTeam,
                                            final HitterRecordParam p) {
        return new GameHitterRecord(game, homeTeam,
                p.battingOrder(), p.position(), p.playerName(),
                p.atBats(), p.hits(), p.rbi(), p.runs());
    }

    private GamePitcherRecord toPitcherRecord(final Game game, final boolean homeTeam,
                                              final PitcherRecordParam p) {
        return new GamePitcherRecord(game, homeTeam,
                p.playerName(), p.result(), p.innings(),
                p.battersFaced(), p.pitchCount(), p.atBats(),
                p.hitsAllowed(), p.homeRunsAllowed(), p.walksAndHbp(),
                p.strikeouts(), p.runsAllowed(), p.earnedRuns());
    }
}