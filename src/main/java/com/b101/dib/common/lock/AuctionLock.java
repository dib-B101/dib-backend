package com.b101.dib.common.lock;

import com.b101.dib.common.exception.BusinessException;
import com.b101.dib.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

// 경매 단위 Redis 분산락 lock:auction:{id}. 안쪽 body 가 트랜잭션(findByIdForUpdate 포함)이고, 락 해제는 body 가 커밋되고 돌아온 뒤.
// 최종 정합성은 DB 행 락 + UNIQUE 가 보장하고, 이 락은 여러 Pod 의 요청이 DB 락 대기열에 몰리는 것을 줄인다.
// Redis 가 없거나(빈 없음) 죽어 있으면 락 없이 body 만 실행 — 입찰이 Redis 때문에 실패하지 않는다
@Component
@Slf4j
public class AuctionLock {
    public static final long WAIT_MS = 1_000;
    public static final long LEASE_MS = 3_000;

    private final ObjectProvider<RedissonClient> redissonClient;

    public AuctionLock(ObjectProvider<RedissonClient> redissonClient) {
        this.redissonClient = redissonClient;
    }

    public <T> T run(Long auctionId, Supplier<T> body) {
        RedissonClient client = redissonClient.getIfAvailable();
        if (client == null) {
            return body.get();
        }
        RLock lock = client.getLock("lock:auction:" + auctionId);
        boolean locked;
        try {
            locked = lock.tryLock(WAIT_MS, LEASE_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.AUCTION_BUSY);
        } catch (RuntimeException e) {
            log.warn("Redis 락 획득 실패 — DB 락만으로 진행 auctionId={} : {}", auctionId, e.getMessage());
            return body.get();
        }
        if (!locked) {
            throw new BusinessException(ErrorCode.AUCTION_BUSY);
        }
        try {
            return body.get();
        } finally {
            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (RuntimeException e) {
                log.warn("Redis 락 해제 실패 auctionId={} : {}", auctionId, e.getMessage());   // lease 만료로 풀린다
            }
        }
    }
}
