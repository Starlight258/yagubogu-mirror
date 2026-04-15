package com.yagubogu.game.repository;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameHitterRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameHitterRecordRepository extends JpaRepository<GameHitterRecord, Long> {

    void deleteAllByGame(Game game);
}