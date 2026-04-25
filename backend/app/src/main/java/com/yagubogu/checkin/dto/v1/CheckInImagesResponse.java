package com.yagubogu.checkin.dto.v1;

import java.util.List;

public record CheckInImagesResponse(
        List<CheckInImageParam> images
) {
}
