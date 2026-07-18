package com.yagubogu.prediction.controller.v1;

import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.prediction.dto.v1.CreateGamePredictionRequest;
import com.yagubogu.prediction.dto.v1.GamePredictionResponse;
import com.yagubogu.prediction.dto.v1.UpdateGamePredictionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "GamePrediction", description = "승부 예측 관련 API")
@RequestMapping("/predictions")
public interface GamePredictionControllerInterface {

    @Operation(summary = "승부 예측 제출", description = "지정한 경기의 승/패를 예측합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "예측 제출 성공"),
            @ApiResponse(responseCode = "404", description = "회원 또는 경기를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 해당 경기에 예측을 제출함"),
            @ApiResponse(responseCode = "422", description = "경기가 이미 시작해 예측을 제출할 수 없음")
    })
    @PostMapping
    ResponseEntity<GamePredictionResponse> submitPrediction(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @Valid @RequestBody CreateGamePredictionRequest request
    );

    @Operation(summary = "승부 예측 수정", description = "마감 전인 경기의 예측을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "예측 수정 성공"),
            @ApiResponse(responseCode = "404", description = "회원, 경기 또는 예측을 찾을 수 없음"),
            @ApiResponse(responseCode = "422", description = "경기가 이미 시작해 예측을 수정할 수 없음")
    })
    @PutMapping
    ResponseEntity<GamePredictionResponse> updatePrediction(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @Valid @RequestBody UpdateGamePredictionRequest request
    );

    @Operation(summary = "내 승부 예측 조회", description = "지정한 경기에 대한 본인의 예측을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "예측을 찾을 수 없음")
    })
    @GetMapping
    ResponseEntity<GamePredictionResponse> findPrediction(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @RequestParam Long gameId
    );
}
