package com.yagubogu.stat.service;

import com.yagubogu.stat.repository.LocationCheckInRankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class LocationCheckInRankingSyncService {

    private final LocationCheckInRankingRepository locationCheckInRankingRepository;

    @Transactional
    public int rebuildAll() {
        locationCheckInRankingRepository.deleteRankings();

        return locationCheckInRankingRepository.insertAllFromCheckIns();
    }
}
