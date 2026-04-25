package com.yagubogu.checkin.repository;

import com.yagubogu.checkin.domain.CheckInImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckInImageRepository extends JpaRepository<CheckInImage, Long> {

    List<CheckInImage> findByCheckInId(Long checkInId);

    List<CheckInImage> findByCheckInIdIn(List<Long> checkInIds);
}
