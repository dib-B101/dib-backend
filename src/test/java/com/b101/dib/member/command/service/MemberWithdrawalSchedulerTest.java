package com.b101.dib.member.command.service;

import java.lang.reflect.Method;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberWithdrawalSchedulerTest {

    @Mock
    private MemberWithdrawalService memberWithdrawalService;

    @InjectMocks
    private MemberWithdrawalScheduler memberWithdrawalScheduler;

    @Test
    void completesDueWithdrawalsWithSingleBulkUpdate() {
        given(memberWithdrawalService.completeDueWithdrawals()).willReturn(3);

        memberWithdrawalScheduler.run();

        verify(memberWithdrawalService).completeDueWithdrawals();
    }

    @Test
    void holdsDistributedLockForOneMinuteWithFiveMinuteSafetyLimit() throws Exception {
        Method run = MemberWithdrawalScheduler.class.getDeclaredMethod("run");
        SchedulerLock schedulerLock = run.getAnnotation(SchedulerLock.class);

        assertThat(schedulerLock).isNotNull();
        assertThat(schedulerLock.name()).isEqualTo("memberWithdrawalCompletion");
        assertThat(schedulerLock.lockAtLeastFor()).isEqualTo("PT1M");
        assertThat(schedulerLock.lockAtMostFor()).isEqualTo("PT5M");
    }
}
