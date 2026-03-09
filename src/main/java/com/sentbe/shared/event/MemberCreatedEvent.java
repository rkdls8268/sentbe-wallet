package com.sentbe.shared.event;

import com.sentbe.cash.in.dto.MemberDto;

public record MemberCreatedEvent(MemberDto memberDto) {}
