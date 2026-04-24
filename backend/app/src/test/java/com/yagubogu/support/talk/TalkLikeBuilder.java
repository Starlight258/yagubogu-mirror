package com.yagubogu.support.talk;

import com.yagubogu.member.domain.Member;
import com.yagubogu.talk.domain.Talk;
import com.yagubogu.talk.domain.TalkLike;
import java.time.LocalDateTime;

public class TalkLikeBuilder {

    private Talk talk;
    private Member member;
    private LocalDateTime createdAt = LocalDateTime.now();

    public TalkLikeBuilder talk(final Talk talk) {
        this.talk = talk;
        return this;
    }

    public TalkLikeBuilder member(final Member member) {
        this.member = member;
        return this;
    }

    public TalkLikeBuilder createdAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public TalkLike build() {
        return new TalkLike(talk, member, createdAt);
    }
}
