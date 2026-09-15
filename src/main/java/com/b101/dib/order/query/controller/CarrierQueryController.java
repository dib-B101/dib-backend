package com.b101.dib.order.query.controller;

import com.b101.dib.order.domain.Carrier;
import com.b101.dib.order.query.dto.CarrierQueryDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/carriers")
public class CarrierQueryController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> findAll() {
        List<CarrierQueryDto> list = new ArrayList<>();
        for (Carrier c : Carrier.values()) {
            if (c.isTestOnly()) {
                continue;
            }
            CarrierQueryDto dto = new CarrierQueryDto();
            dto.setCode(c.name());
            dto.setName(c.getDisplayName());
            list.add(dto);
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "택배사 목록 조회 성공");
        map.put("data", list);
        return ResponseEntity.ok(map);
    }
}
