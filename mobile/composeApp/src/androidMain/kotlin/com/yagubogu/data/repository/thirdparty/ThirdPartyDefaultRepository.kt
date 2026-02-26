package com.yagubogu.data.repository.thirdparty

import android.net.Uri
import com.yagubogu.data.datasource.thirdparty.ThirdPartyDataSource

class ThirdPartyDefaultRepository(
    private val thirdPartyDataSource: ThirdPartyDataSource,
) : ThirdPartyRepository {
    override suspend fun uploadImageToS3(
        url: String,
        imageFileUri: Uri,
        contentType: String,
        contentLength: Long,
    ): Result<Unit> = thirdPartyDataSource.uploadImageToS3(url, imageFileUri, contentType, contentLength)
}
