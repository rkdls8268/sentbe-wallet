package com.sentbe.cash.application;

import com.sentbe.cash.domain.Member;
import com.sentbe.cash.out.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;

  public void register(String email, String nickname, String password) {
    Member cashMember = Member.create(email, nickname, password);
    memberRepository.save(cashMember);
  }

}
