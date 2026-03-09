package com.sentbe.global.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventPublisher {
  private final ApplicationEventPublisher applicationEventPublisher;

  // 이벤트 발행
  public void publish(Object event) {
    applicationEventPublisher.publishEvent(event);
  }
}
