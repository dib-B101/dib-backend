package com.b101.dib.fraudLabel.command.controller;

import com.b101.dib.fraudLabel.command.dto.CreateFraudLabelRequest;
import com.b101.dib.fraudLabel.command.service.FraudLabelCommandService;
import com.b101.dib.fraudLabel.domain.FraudLabel;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/fraud-labels")
@RequiredArgsConstructor
public class AdminFraudLabelCommandController {
    private final FraudLabelCommandService fraudLabelCommandService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody CreateFraudLabelRequest request) {
        FraudLabel fraudLabel = fraudLabelCommandService.create(request);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "이상입찰 라벨 확정 성공");
        map.put("fraudLabelId", fraudLabel.getFraudLabelId());
        map.put("confirmedAt", fraudLabel.getConfirmedAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(map);
    }
}
