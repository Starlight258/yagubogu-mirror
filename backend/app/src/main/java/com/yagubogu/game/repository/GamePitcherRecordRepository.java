package com.yagubogu.game.repository;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GamePitcherRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GamePitcherRecordRepository extends JpaRepository<GamePitcherRecord, Long> {

    void deleteAllByGame(Game game);
}
