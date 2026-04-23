package com.yagubogu.domain.attendance

data class AttendanceDiary(
    val memo: String?,
    val images: List<Image>,
) {
    val hasContent: Boolean = memo != null || images.isNotEmpty()

    data class Image(
        val id: Long,
        val url: String,
    )
}
