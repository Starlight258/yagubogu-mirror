package com.yagubogu.talk.dto.v1;

import com.yagubogu.talk.domain.Talk;
import java.time.LocalDateTime;

public record TalkResponse(
        long id,
        Long memberId,
        String nickname,
        String favorite,
        String imageUrl,
        String content,
        LocalDateTime createdAt,
        boolean isMine,
        int likeCount,
        boolean isLiked
) {

    public static TalkResponse from(Talk talk, long memberId) {
        return from(talk, memberId, 0, false);
    }

    public static TalkResponse from(Talk talk, long memberId, int likeCount, boolean isLiked) {
        if (talk.getMember() == null) {
            return new TalkResponse(
                    talk.getId(),
                    null,
                    null,
                    null,
                    null,
                    talk.getContent(),
                    talk.getCreatedAt(),
                    false,
                    likeCount,
                    isLiked
            );
        }

        return new TalkResponse(
                talk.getId(),
                talk.getMember().getId(),
                talk.getMember().getNickname().getValue(),
                talk.getMember().getTeam().getShortName(),
                talk.getMember().getImageUrl(),
                talk.getContent(),
                talk.getCreatedAt(),
                talk.getMember().isSameId(memberId),
                likeCount,
                isLiked
        );
    }

    public static TalkResponse hiddenFrom(TalkResponse talkResponse) {
        return new TalkResponse(
                talkResponse.id(),
                talkResponse.memberId(),
                talkResponse.nickname(),
                talkResponse.favorite(),
                talkResponse.imageUrl(),
                "숨김처리되었습니다",
                talkResponse.createdAt(),
                talkResponse.isMine(),
                talkResponse.likeCount(),
                talkResponse.isLiked()
        );
    }
}
