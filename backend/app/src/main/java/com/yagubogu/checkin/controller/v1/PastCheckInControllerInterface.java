package com.yagubogu.checkin.controller.v1;

import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.checkin.dto.CreatePastCheckInRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "과거 직관 인증")
@RequestMapping("/past-check-ins")
public interface PastCheckInControllerInterface {

    @Operation(summary = "과거 직관 인증 생성", description = "과거 경기에 대한 직관 인증을 생성합니다. 위치 인증이 필요하지 않으며, 승리 요정 랭킹에는 포함되지 않습니다.")
    @PostMapping
    ResponseEntity<Void> createPastCheckIn(
            @Parameter(hidden = true) final MemberClaims memberClaims,
            @Valid @RequestBody final CreatePastCheckInRequest request
    );

    @Operation(summary = "과거 직관 인증 삭제", description = "과거 직관 인증을 삭제합니다. 위치 인증이 아닌 직관 인증(NON_LOCATION_CHECK_IN)만 삭제할 수 있으며, 본인의 인증만 삭제할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "과거 직관 인증 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "위치 인증 기반 체크인은 삭제할 수 없음"),
            @ApiResponse(responseCode = "403", description = "본인의 체크인만 삭제할 수 있음"),
            @ApiResponse(responseCode = "404", description = "체크인을 찾을 수 없음")
    })
    @DeleteMapping("/{checkInId}")
    ResponseEntity<Void> deletePastCheckIn(
            @Parameter(hidden = true) final MemberClaims memberClaims,
            @PathVariable final Long checkInId
    );
}
