package com.yagubogu.support.talk;

import com.yagubogu.talk.domain.TalkLike;
import com.yagubogu.talk.repository.TalkLikeRepository;
import java.util.function.Consumer;

public class TalkLikeFactory {

    private final TalkLikeRepository talkLikeRepository;

    public TalkLikeFactory(final TalkLikeRepository talkLikeRepository) {
        this.talkLikeRepository = talkLikeRepository;
    }

    public TalkLike save(final Consumer<TalkLikeBuilder> customizer) {
        TalkLikeBuilder builder = new TalkLikeBuilder();
        customizer.accept(builder);
        TalkLike talkLike = builder.build();

        return talkLikeRepository.save(talkLike);
    }
}
