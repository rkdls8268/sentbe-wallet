package com.sentbe.cash.in.dto;

import java.time.LocalDateTime;

public record MemberDto(
  Long id,
  String email,
  String nickname,
  LocalDateTime createdAt,
  LocalDateTime updatedAt
) {}
