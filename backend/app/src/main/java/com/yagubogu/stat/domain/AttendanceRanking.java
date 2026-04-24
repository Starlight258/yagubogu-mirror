package com.yagubogu.stat.domain;

import com.yagubogu.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(
        name = "attendance_rankings",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_attendance_rankings_member_year", columnNames = {"member_id", "game_year"})
        },
        indexes = {
                @Index(name = "idx_attendance_rankings_year_count_member",
                        columnList = "game_year, check_in_count DESC, member_id ASC")
        }
)
@Entity
@EntityListeners(AuditingEntityListener.class)
public class AttendanceRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_ranking_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "check_in_count", nullable = false)
    private int checkInCount = 0;

    @Column(name = "game_year", nullable = false)
    private int gameYear;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public AttendanceRanking(final Member member, final int checkInCount, final int gameYear) {
        this.member = member;
        this.checkInCount = checkInCount;
        this.gameYear = gameYear;
    }
}
