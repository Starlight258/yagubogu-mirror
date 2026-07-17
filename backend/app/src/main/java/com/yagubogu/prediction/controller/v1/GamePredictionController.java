package com.yagubogu.prediction.controller.v1;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.prediction.dto.v1.CreateGamePredictionRequest;
import com.yagubogu.prediction.dto.v1.GamePredictionResponse;
import com.yagubogu.prediction.dto.v1.UpdateGamePredictionRequest;
import com.yagubogu.prediction.service.GamePredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequireRole
@RestController
public class GamePredictionController implements GamePredictionControllerInterface {

    private final GamePredictionService gamePredictionService;

    @Override
    public ResponseEntity<GamePredictionResponse> submitPrediction(
            final MemberClaims memberClaims,
            @RequestBody final CreateGamePredictionRequest request
    ) {
        GamePredictionResponse response = gamePredictionService.submitPrediction(memberClaims.id(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<GamePredictionResponse> updatePrediction(
            final MemberClaims memberClaims,
            @RequestBody final UpdateGamePredictionRequest request
    ) {
        GamePredictionResponse response = gamePredictionService.updatePrediction(memberClaims.id(), request);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<GamePredictionResponse> findPrediction(
            final MemberClaims memberClaims,
            @RequestParam final Long gameId
    ) {
        GamePredictionResponse response = gamePredictionService.findPrediction(memberClaims.id(), gameId);

        return ResponseEntity.ok(response);
    }
}
