package com.yagubogu.stat.service;

import com.yagubogu.stat.repository.AttendanceRankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AttendanceRankingSyncService {

    private final AttendanceRankingRepository attendanceRankingRepository;

    @Transactional
    public int rebuildAll() {
        attendanceRankingRepository.deleteRankings();

        return attendanceRankingRepository.insertAllFromCheckIns();
    }
}
