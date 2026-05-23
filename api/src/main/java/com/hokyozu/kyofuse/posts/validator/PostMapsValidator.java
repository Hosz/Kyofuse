package com.hokyozu.kyofuse.posts.validator;

import com.hokyozu.kyofuse.posts.dto.request.CreatePostRequest;
import com.hokyozu.kyofuse.shared.exception.BadRequestException;
import org.springframework.stereotype.Component;


@Component
public class PostMapsValidator {

    public void validate(CreatePostRequest request) {
        if (request.maps() != null) {
            long uniqueCount = request.maps()
                    .stream()
                    .distinct()
                    .count();

            if (uniqueCount != request.maps().size()) {
                throw new BadRequestException("Maps cannot contain duplicates.");
            }
        }
    }
}
