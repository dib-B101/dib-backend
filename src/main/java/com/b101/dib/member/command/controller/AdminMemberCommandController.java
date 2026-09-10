package com.b101.dib.member.command.controller;

import com.b101.dib.member.command.dto.SanctionMemberRequest;
import com.b101.dib.member.command.service.MemberCommandService;
import com.b101.dib.member.domain.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/members")
@RequiredArgsConstructor
public class AdminMemberCommandController {
    private final MemberCommandService memberCommandService;

    @PatchMapping("/{memberId}/sanction")
    public ResponseEntity<Map<String, Object>> sanction(@PathVariable("memberId") Long memberId,
                                                        @Valid @RequestBody SanctionMemberRequest request) {
        Member member = memberCommandService.sanction(memberId, request);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "회원 제재 적용 성공");
        map.put("memberId", member.getId());
        map.put("warningCount", member.getWarningCount());
        map.put("status", member.getStatus());
        map.put("suspendedAt", member.getSuspendedAt());
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @DeleteMapping("/{memberId}/sanction")
    public ResponseEntity<Map<String, Object>> releaseSanction(@PathVariable("memberId") Long memberId) {
        Member member = memberCommandService.releaseSanction(memberId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("message", "회원 정지 해제 성공");
        map.put("memberId", member.getId());
        map.put("status", member.getStatus());
        map.put("suspendedAt", member.getSuspendedAt());
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
}
