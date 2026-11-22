package com.dimensiondetector.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dimensiondetector.ui.theme.Amber400
import com.dimensiondetector.ui.theme.Cyan400
import com.dimensiondetector.ui.theme.Emerald400
import com.dimensiondetector.ui.theme.Rose400
import com.dimensiondetector.ui.theme.Slate400
import com.dimensiondetector.ui.theme.Slate700
import com.dimensiondetector.ui.theme.Slate900

enum class BadgeType {
    SUCCESS,
    WARNING,
    ERROR,
    INFO,
    NEUTRAL
}

@Composable
fun StatusBadge(
    text: String,
    type: BadgeType,
    modifier: Modifier = Modifier
) {
    val (dotColor, bgColor, textColor) = when (type) {
        BadgeType.SUCCESS -> Triple(Emerald400, Emerald400.copy(alpha = 0.15f), Emerald400)
        BadgeType.WARNING -> Triple(Amber400, Amber400.copy(alpha = 0.15f), Amber400)
        BadgeType.ERROR -> Triple(Rose400, Rose400.copy(alpha = 0.15f), Rose400)
        BadgeType.INFO -> Triple(Cyan400, Cyan400.copy(alpha = 0.15f), Cyan400)
        BadgeType.NEUTRAL -> Triple(Slate400, Slate700, Slate400)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
