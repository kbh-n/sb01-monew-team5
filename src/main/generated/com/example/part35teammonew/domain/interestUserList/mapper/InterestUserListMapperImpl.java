package com.example.part35teammonew.domain.interestUserList.mapper;

import com.example.part35teammonew.domain.interestUserList.Dto.InterestUserListDto;
import com.example.part35teammonew.domain.interestUserList.entity.InterestUserList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-28T16:35:21+0900",
    comments = "version: 1.5.3.Final, compiler: javac, environment: Java 17.0.12 (Oracle Corporation)"
)
@Component
public class InterestUserListMapperImpl implements InterestUserListMapper {

    @Override
    public InterestUserListDto toDto(InterestUserList interestUserList) {
        if ( interestUserList == null ) {
            return null;
        }

        UUID interest = null;
        Set<UUID> subscribedUser = null;

        interest = interestUserList.getInterest();
        Set<UUID> set = interestUserList.getSubscribedUser();
        if ( set != null ) {
            subscribedUser = new LinkedHashSet<UUID>( set );
        }

        InterestUserListDto interestUserListDto = new InterestUserListDto( interest, subscribedUser );

        return interestUserListDto;
    }
}
