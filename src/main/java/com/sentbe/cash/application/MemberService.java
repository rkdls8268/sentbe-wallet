package com.sentbe.cash.application;

import com.sentbe.cash.domain.Member;
import com.sentbe.cash.out.MemberRepository;
import com.sentbe.global.publisher.EventPublisher;
import com.sentbe.shared.event.MemberCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;
  private final EventPublisher eventPublisher;

  public void register(String email, String nickname, String password) {
    Member member = Member.create(email, nickname, password);
    memberRepository.save(member);

    eventPublisher.publish(new MemberCreatedEvent(member.toDto()));
  }

  public long count() {
    return memberRepository.count();
  }

}
