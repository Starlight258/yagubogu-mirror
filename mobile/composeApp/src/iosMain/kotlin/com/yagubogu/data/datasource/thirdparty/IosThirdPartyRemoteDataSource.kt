package com.yagubogu.data.datasource.thirdparty

import com.yagubogu.data.service.ThirdPartyApiService
import com.yagubogu.data.util.safeApiCall
import io.ktor.http.ContentType
import io.ktor.http.content.OutgoingContent
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.posix.memcpy

@ExperimentalForeignApi
class IosThirdPartyRemoteDataSource(
    private val thirdPartyApiService: ThirdPartyApiService,
) : ThirdPartyDataSource {

    override suspend fun uploadImageToS3(
        url: String,
        imageFileUri: String,
        contentType: String,
        contentLength: Long,
    ): Result<Unit> = safeApiCall {
        val nsUrl = NSURL(string = imageFileUri)
        val nsData = NSData.dataWithContentsOfURL(nsUrl)
            ?: throw IllegalStateException("Failed to read data from $imageFileUri")

        val byteArray = ByteArray(nsData.length.toInt())
        if (byteArray.isNotEmpty()) {
            byteArray.usePinned { pinned ->
                memcpy(pinned.addressOf(0), nsData.bytes, nsData.length)
            }
        }

        val requestBody = object : OutgoingContent.ByteArrayContent() {
            override val contentType: ContentType = ContentType.parse(contentType)
            override val contentLength: Long = contentLength
            override fun bytes(): ByteArray = byteArray
        }

        thirdPartyApiService.putImageToS3(url, requestBody)
    }
}
