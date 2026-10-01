package com.ahmad.notifyhub.mapper;

import com.ahmad.notifyhub.dto.response.TagResponse;
import com.ahmad.notifyhub.entity.Tag;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {

    public TagResponse toResponse(Tag tag) {
        return new TagResponse(
                tag.getId(),
                tag.getName(),
                tag.getCreatedAt(),
                tag.getUpdatedAt()
        );
    }
}
