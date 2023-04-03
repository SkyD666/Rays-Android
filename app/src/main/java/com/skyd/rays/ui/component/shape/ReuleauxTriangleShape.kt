package com.skyd.rays.ui.component.shape

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.skyd.rays.ext.toRadians
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class ReuleauxTriangleShape(
    private val rotateAngle: Float = 0f
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val l = min(size.width, size.height)
        val r = l / 2
        val center = Offset(x = r, y = r)
        val startBottomPoint = Offset(
            x = center.x - r * sin((60f + rotateAngle).toRadians()),
            y = center.y + r * cos((60f + rotateAngle).toRadians())
        )
        val startBottomRect = Rect(center = startBottomPoint, radius = 2 * r * cos(30f.toRadians()))
        path.moveTo(startBottomPoint.x, startBottomPoint.y)
        path.arcTo(
            startBottomRect,
            startAngleDegrees = -60f + rotateAngle,
            sweepAngleDegrees = 60f,
            forceMoveTo = false
        )
        val endBottomPoint = Offset(
            x = center.x + r * sin((60f - rotateAngle).toRadians()),
            y = center.y + r * cos((60f - rotateAngle).toRadians())
        )
        val endBottomRect = Rect(center = endBottomPoint, radius = 2 * r * cos(30f.toRadians()))
        path.arcTo(
            endBottomRect,
            startAngleDegrees = 180f + rotateAngle,
            sweepAngleDegrees = 60f,
            forceMoveTo = false
        )
        val centerTopPoint = Offset(
            x = l / 2 + r * sin(rotateAngle.toRadians()),
            y = 0f + r - r * cos(rotateAngle.toRadians())
        )
        val centerTopRect = Rect(center = centerTopPoint, radius = 2 * r * cos(30f.toRadians()))
        path.arcTo(
            centerTopRect,
            startAngleDegrees = 60f + rotateAngle,
            sweepAngleDegrees = 60f,
            forceMoveTo = false
        )
        path.close()
        return Outline.Generic(path)
    }
}