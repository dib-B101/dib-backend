package com.b101.dib.consumedEvent.command.service;

// Consumer 멱등성. 처리 트랜잭션 안에서 부른다: 처음 보는 이벤트면 기록하고 true, 이미 처리했으면 false
public interface ConsumedEventService {
    boolean claim(String consumerGroup, String eventId);

    // 트랜잭션이 없는 Consumer(외부 API 호출 등)용. 자기 트랜잭션으로 바로 커밋한다
    boolean claimInNewTransaction(String consumerGroup, String eventId);
}
