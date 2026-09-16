package com.b101.dib.member.command.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MemberWithdrawalScheduler {

    private final MemberWithdrawalService memberWithdrawalService;

    @Scheduled(fixedDelay = 60_000)
    @SchedulerLock(
            name = "memberWithdrawalCompletion",
            lockAtLeastFor = "PT1M",
            lockAtMostFor = "PT5M"
    )
    public void run() {
        int completedCount = memberWithdrawalService.completeDueWithdrawals();
        if (completedCount > 0) {
            log.info("회원 탈퇴 유예 만료 처리 완료 count={}", completedCount);
        }
    }
}
