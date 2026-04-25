package com.yagubogu.talk.dto.v1;

public record TalkLikeResponse(
        boolean liked,
        long likeCount
) {
}
