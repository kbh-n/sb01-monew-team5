package com.example.part35teammonew.domain.interestUserList.Dto;

import java.util.Set;
import java.util.UUID;

public record InterestUserListDto(UUID interest, Set<UUID> subscribedUser) {

}
