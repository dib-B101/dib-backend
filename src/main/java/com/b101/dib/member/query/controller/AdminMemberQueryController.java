package com.b101.dib.member.query.controller;

import com.b101.dib.member.domain.MemberStatus;
import com.b101.dib.member.query.dto.AdminMemberQueryDto;
import com.b101.dib.member.query.service.MemberQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/members")
@RequiredArgsConstructor
public class AdminMemberQueryController {
    private final MemberQueryService memberQueryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> findAll(@RequestParam(name = "q", required = false) String q,
                                                       @RequestParam(name = "status", required = false) MemberStatus status,
                                                       @RequestParam(name = "warningCount", required = false) Integer warningCount) {
        List<AdminMemberQueryDto> dtoList = memberQueryService.findAll(q, status, warningCount);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "회원 목록 조회 성공");
        map.put("data", dtoList);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
}
