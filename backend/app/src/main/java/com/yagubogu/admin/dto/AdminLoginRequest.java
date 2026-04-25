package com.yagubogu.admin.dto;

import com.yagubogu.member.domain.OAuthProvider;

public record AdminLoginRequest(
        String idToken,
        OAuthProvider provider
) {
}
