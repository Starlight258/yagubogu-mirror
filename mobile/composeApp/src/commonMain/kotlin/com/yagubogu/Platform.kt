package com.yagubogu

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform