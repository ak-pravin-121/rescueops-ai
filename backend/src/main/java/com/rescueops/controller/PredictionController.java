package com.rescueops.controller;

import com.rescueops.dto.PredictionDtos.PredictionResponse;
import com.rescueops.prediction.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    @GetMapping
    public ResponseEntity<List<PredictionResponse>> recent() {
        return ResponseEntity.ok(predictionService.getRecentPredictions());
    }

    @PostMapping("/run")
    public ResponseEntity<PredictionResponse> run(@RequestParam String serviceName) {
        return ResponseEntity.ok(predictionService.runPredictionForService(serviceName));
    }

    @GetMapping("/{serviceName}/latest")
    public ResponseEntity<PredictionResponse> latest(@PathVariable String serviceName) {
        PredictionResponse response = predictionService.getLatestForService(serviceName);
        return response != null ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }
}
