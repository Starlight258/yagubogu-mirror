package com.yagubogu.admin.controller;

import com.yagubogu.admin.dto.AdminCrawlingGamesRequest;
import com.yagubogu.admin.dto.AdminCrawlingGamesResponse;
import com.yagubogu.admin.service.AdminCrawlingService;
import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.member.domain.Role;
import com.yagubogu.prediction.service.PredictionResultService;
import com.yagubogu.stat.service.LocationCheckInRankingSyncService;
import com.yagubogu.stat.service.StatSyncService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequireRole(Role.ADMIN)
@RequestMapping("/admin")
@RestController
public class AdminController {

    private final StatSyncService statSyncService;
    private final LocationCheckInRankingSyncService locationCheckInRankingSyncService;
    private final AdminCrawlingService adminCrawlingService;
    private final PredictionResultService predictionResultService;

    @PostMapping("/victory-fairy-rankings/sync")
    public ResponseEntity<Void> syncVictoryRankings() {
        int year = LocalDate.now().getYear();
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
            statSyncService.updateRankings(date);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/location-check-in-rankings/sync")
    public ResponseEntity<Integer> syncLocationCheckInRankings() {
        int syncedCount = locationCheckInRankingSyncService.rebuildAll();

        return ResponseEntity.ok(syncedCount);
    }

    @PostMapping("/crawling/games")
    public ResponseEntity<AdminCrawlingGamesResponse> crawlGames(
            @Valid @RequestBody final AdminCrawlingGamesRequest request
    ) {
        AdminCrawlingGamesResponse response = adminCrawlingService.crawlGames(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/predictions/{gameCode}/result-recalculation")
    public ResponseEntity<Void> recalculatePredictionResults(@PathVariable final String gameCode) {
        predictionResultService.recalculateGamePredictionResults(gameCode);

        return ResponseEntity.ok().build();
    }
}
