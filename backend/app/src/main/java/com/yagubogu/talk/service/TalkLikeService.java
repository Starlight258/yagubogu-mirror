package com.yagubogu.talk.service;

import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.talk.domain.Talk;
import com.yagubogu.talk.domain.TalkLike;
import com.yagubogu.talk.dto.v1.TalkLikeResponse;
import com.yagubogu.talk.repository.TalkLikeRepository;
import com.yagubogu.talk.repository.TalkRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class TalkLikeService {

    private final TalkLikeRepository talkLikeRepository;
    private final TalkRepository talkRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public TalkLikeResponse toggleLike(final long talkId, final long memberId) {
        Talk talk = getTalk(talkId);
        Member member = getMember(memberId);

        if (talkLikeRepository.existsByTalkIdAndMemberId(talkId, memberId)) {
            talkLikeRepository.deleteByTalkIdAndMemberId(talkId, memberId);
            long likeCount = talkLikeRepository.countByTalkId(talkId);
            return new TalkLikeResponse(false, likeCount);
        }

        talkLikeRepository.save(new TalkLike(talk, member, LocalDateTime.now()));
        long likeCount = talkLikeRepository.countByTalkId(talkId);
        return new TalkLikeResponse(true, likeCount);
    }

    private Talk getTalk(final long talkId) {
        return talkRepository.findById(talkId)
                .orElseThrow(() -> new NotFoundException("Talk is not found"));
    }

    private Member getMember(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member is not found"));
    }
}
