package kr.daejeonuinversity.lungexercise.util.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import kr.daejeonuinversity.lungexercise.R

class HeartTimerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint().apply {
        color = ContextCompat.getColor(context,R.color.color_timer_walking)
        style = Paint.Style.STROKE
        strokeWidth = 25f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 64f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private var sweepAngle = 360f
    private var remainingTimeText = "06:00"

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val padding = 40f
        val size = width.coerceAtMost(height).toFloat() - padding * 2
        val rect = RectF(padding, padding, padding + size, padding + size)

        // 그리기
        canvas.drawArc(rect, -90f, sweepAngle, false, paint)

        // 가운데 텍스트
        val x = width / 2f
        val y = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2
        canvas.drawText(remainingTimeText, x, y, textPaint)
    }

    fun updateProgress(percentage: Float, timeText: String) {
        sweepAngle = 360f * percentage
        remainingTimeText = timeText
        invalidate()
    }
}
