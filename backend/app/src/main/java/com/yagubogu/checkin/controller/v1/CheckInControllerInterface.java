package com.yagubogu.checkin.controller.v1;

import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.checkin.domain.CheckInOrderFilter;
import com.yagubogu.checkin.domain.CheckInResultFilter;
import com.yagubogu.checkin.dto.v1.AddCheckInImageRequest;
import com.yagubogu.checkin.dto.v1.CheckInCountsResponse;
import com.yagubogu.checkin.dto.v1.CheckInHistoryResponse;
import com.yagubogu.checkin.dto.v1.CheckInImageParam;
import com.yagubogu.checkin.dto.v1.CheckInImagesResponse;
import com.yagubogu.checkin.dto.v1.CheckInMemoResponse;
import com.yagubogu.checkin.dto.v1.CheckInReviewResponse;
import com.yagubogu.checkin.dto.v1.CheckInStatusResponse;
import com.yagubogu.checkin.dto.v1.CreateCheckInRequest;
import com.yagubogu.checkin.dto.v1.FanRateResponse;
import com.yagubogu.checkin.dto.v1.StadiumCheckInCountsResponse;
import com.yagubogu.checkin.dto.v1.UpdateCheckInMemoRequest;
import com.yagubogu.member.dto.v1.PreSignedUrlStartRequest;
import com.yagubogu.member.dto.v1.PresignedUrlStartResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "CheckIn", description = "경기 인증 관련 API")
@RequestMapping("/check-ins")
public interface CheckInControllerInterface {

    @Operation(summary = "경기 인증 생성", description = "지정한 경기의 인증을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "인증 생성 성공"),
            @ApiResponse(responseCode = "404", description = "회원, 경기 또는 야구장을 찾을 수 없음")
    })
    @PostMapping
    ResponseEntity<Void> createCheckIn(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @Valid @RequestBody CreateCheckInRequest request
    );

    @Operation(summary = "경기 인증 삭제", description = "지정한 경기 인증을 삭제합니다. 본인의 인증만 삭제할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "인증 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "본인의 체크인만 삭제할 수 있음"),
            @ApiResponse(responseCode = "404", description = "체크인을 찾을 수 없음")
    })
    @DeleteMapping("/{checkInId}")
    ResponseEntity<Void> deleteCheckIn(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @PathVariable Long checkInId
    );

    @Operation(summary = "인증 수 조회", description = "연도별로 회원의 총 인증 수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증 수 조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @GetMapping("/counts")
    ResponseEntity<CheckInCountsResponse> findCheckInCounts(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @RequestParam int year
    );

    @Operation(summary = "인증 내역 조회", description = "연도, 월별 인증 내역을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증 내역 조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @GetMapping("/members")
    ResponseEntity<CheckInHistoryResponse> findCheckInHistory(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(name = "result", defaultValue = "ALL") CheckInResultFilter resultFilter,
            @RequestParam(name = "order", defaultValue = "LATEST") CheckInOrderFilter orderFilter
    );

    @Operation(summary = "구장별 팬 점유율 조회", description = "해당 날짜의 구장별 팬 점유율을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팬 점유율 조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원 또는 경기 정보를 찾을 수 없음")
    })
    @GetMapping("/stadiums/fan-rates")
    ResponseEntity<FanRateResponse> findFanRatesByStadiums(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @RequestParam LocalDate date
    );

    @Operation(summary = "당일 인증 여부 조회", description = "해당 날짜에 사용자가 인증했는지 여부를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증 여부 조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @GetMapping("/status")
    ResponseEntity<CheckInStatusResponse> findCheckInStatus(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @RequestParam LocalDate date
    );

    @Operation(summary = "직관 경기 리뷰 조회", description = "checkInId에 해당하는 경기의 타자/투수 기록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "리뷰 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 직관 내역을 찾을 수 없음")
    })
    @GetMapping("/{checkInId}/review")
    ResponseEntity<CheckInReviewResponse> findCheckInReview(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @PathVariable long checkInId
    );

    @Operation(summary = "구장별 방문 횟수 조회", description = "사용자의 현재 연도 기준 구장별 체크인 횟수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "구장별 체크인 횟수 조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @GetMapping("/stadiums/counts")
    ResponseEntity<StadiumCheckInCountsResponse> findStadiumCheckInCount(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @RequestParam(required = false) Integer year
    );

    // ── 메모 CRUD ──────────────────────────────────────────────────────────────

    @Operation(summary = "직관 기록 메모 조회", description = "직관 기록의 메모를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메모 조회 성공"),
            @ApiResponse(responseCode = "404", description = "직관 기록 또는 회원을 찾을 수 없음")
    })
    @GetMapping("/{checkInId}/memo")
    ResponseEntity<CheckInMemoResponse> getMemo(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @PathVariable Long checkInId
    );

    @Operation(summary = "직관 기록 메모 수정", description = "직관 기록의 메모를 추가하거나 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메모 수정 성공"),
            @ApiResponse(responseCode = "404", description = "직관 기록 또는 회원을 찾을 수 없음")
    })
    @PutMapping("/{checkInId}/memo")
    ResponseEntity<Void> updateMemo(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @PathVariable Long checkInId,
            @Valid @RequestBody UpdateCheckInMemoRequest request
    );

    @Operation(summary = "직관 기록 메모 삭제", description = "직관 기록의 메모를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "메모 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "직관 기록 또는 회원을 찾을 수 없음")
    })
    @DeleteMapping("/{checkInId}/memo")
    ResponseEntity<Void> deleteMemo(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @PathVariable Long checkInId
    );

    // ── 이미지 CRUD ────────────────────────────────────────────────────────────

    @Operation(summary = "직관 기록 이미지 업로드용 Presigned URL 발급", description = "직관 기록 이미지 업로드를 위한 R2 Presigned URL을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Presigned URL 발급 성공")
    })
    @PostMapping("/image/presigned-url")
    ResponseEntity<PresignedUrlStartResponse> issueImagePresignedUrl(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @Valid @RequestBody PreSignedUrlStartRequest request
    );

    @Operation(summary = "직관 기록 이미지 목록 조회", description = "직관 기록에 첨부된 이미지 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이미지 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "직관 기록 또는 회원을 찾을 수 없음")
    })
    @GetMapping("/{checkInId}/images")
    ResponseEntity<CheckInImagesResponse> getImages(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @PathVariable Long checkInId
    );

    @Operation(summary = "직관 기록 이미지 추가", description = "직관 기록에 이미지를 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "이미지 추가 성공"),
            @ApiResponse(responseCode = "404", description = "직관 기록, 회원 또는 이미지를 찾을 수 없음")
    })
    @PostMapping("/{checkInId}/images")
    ResponseEntity<CheckInImageParam> addImage(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @PathVariable Long checkInId,
            @Valid @RequestBody AddCheckInImageRequest request
    );

    @Operation(summary = "직관 기록 이미지 삭제", description = "직관 기록의 특정 이미지를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "이미지 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "직관 기록, 회원 또는 이미지를 찾을 수 없음")
    })
    @DeleteMapping("/{checkInId}/images/{imageId}")
    ResponseEntity<Void> deleteImage(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @PathVariable Long checkInId,
            @PathVariable Long imageId
    );
}
