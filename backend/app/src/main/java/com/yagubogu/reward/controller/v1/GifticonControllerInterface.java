package com.yagubogu.reward.controller.v1;

import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.reward.dto.v1.GifticonIssuanceResponse;
import com.yagubogu.reward.dto.v1.GifticonIssuancesResponse;
import com.yagubogu.reward.dto.v1.GifticonRecipientRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Gifticon", description = "기프티콘 발급 관련 API")
@RequestMapping("/rewards/gifticons")
public interface GifticonControllerInterface {

    @Operation(summary = "내 기프티콘 당첨 내역 조회")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    ResponseEntity<GifticonIssuancesResponse> findGifticons(
            @Parameter(hidden = true) MemberClaims memberClaims
    );

    @Operation(summary = "기프티콘 수신자 전화번호 등록")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전화번호 등록 및 발급 요청 성공"),
            @ApiResponse(responseCode = "404", description = "본인의 발급 건을 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "전화번호를 등록하거나 발급을 요청할 수 없는 상태"),
            @ApiResponse(responseCode = "422", description = "유효하지 않은 전화번호"),
            @ApiResponse(responseCode = "502", description = "카카오 발급 요청 실패")
    })
    @PutMapping("/{gifticonIssuanceId}/recipient")
    ResponseEntity<GifticonIssuanceResponse> registerRecipient(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @PathVariable long gifticonIssuanceId,
            @Valid @RequestBody GifticonRecipientRequest request
    );
}
