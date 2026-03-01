package com.yagubogu.ui.util

import androidx.annotation.StringRes
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.ArrowOrientationRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.compose.rememberBalloonBuilder
import yagubogu.composeapp.generated.resources.Res
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun rememberNoRippleInteractionSource(): MutableInteractionSource =
    remember {
        object : MutableInteractionSource {
            override val interactions: Flow<Interaction> = emptyFlow()

            override suspend fun emit(interaction: Interaction) {}

            override fun tryEmit(interaction: Interaction) = true
        }
    }

@Composable
fun rememberBalloonBuilder(
    @StringRes textResId: Int,
): Balloon.Builder =
    rememberBalloonBuilder {
        setTextResource(textResId)
        setWidthRatio(0.5f)
        setCornerRadius(8f)
        setPaddingHorizontal(10)
        setPaddingVertical(8)
        setTextColorResource(R.color.gray800)
        setBackgroundColorResource(R.color.gray200)
        setArrowTopPadding(4)
        setArrowOrientation(ArrowOrientation.TOP)
        setArrowOrientationRules(ArrowOrientationRules.ALIGN_FIXED)
    }

/**
 * Figma의 CSS box-shadow 스펙을 Compose의 [Shadow]로 변환합니다.
 *
 * Compose의 [Shadow.blurRadius]는 Gaussian blur의 sigma값을 사용하는 반면,
 * CSS/Figma는 sigma의 2배 값을 blur로 표현하기 때문에 변환이 필요합니다.
 *
 * @param color 그림자 색상
 * @param offsetX 그림자 X축 오프셋 (Figma 스펙값 그대로 입력)
 * @param offsetY 그림자 Y축 오프셋 (Figma 스펙값 그대로 입력)
 * @param blur 그림자 번짐 정도 (Figma 스펙값 그대로 입력, 내부적으로 sigma 변환 적용)
 */
@Composable
fun cssShadow(
    color: Color,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp,
    blur: Dp = 0.dp,
): Shadow {
    val density = LocalDensity.current
    return with(density) {
        Shadow(
            color = color,
            offset =
                Offset(
                    x = offsetX.toPx(),
                    y = offsetY.toPx(),
                ),
            blurRadius = blur.toPx() / 2f,
        )
    }
}
