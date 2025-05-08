package com.example.tez.utils

import android.graphics.Canvas
import android.graphics.RectF
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.buffer.BarBuffer
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler

class RoundedBarChartRenderer(
    chart: BarDataProvider,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : BarChartRenderer(chart, animator, viewPortHandler) {

    private val radius = 20f  // Oval yumuşaklık derecesi (dp)

    override fun drawDataSet(c: Canvas, dataSet: IBarDataSet, index: Int) {
        // mBarBuffers null kontrolü
        if (mBarBuffers == null) {
            return
        }
        // buffer dizisini güvenli şekilde al
        val buffer = mBarBuffers.getOrNull(index) ?: return
        if (buffer.buffer == null || buffer.buffer.isEmpty()) return

        val trans = mChart.getTransformer(dataSet.axisDependency)
        mBarBorderPaint.color = dataSet.barBorderColor
        mBarBorderPaint.strokeWidth = Utils.convertDpToPixel(dataSet.barBorderWidth)
        val drawBorder = dataSet.barBorderWidth > 0f
        val phaseX = mAnimator.phaseX
        val phaseY = mAnimator.phaseY

        buffer.setPhases(phaseX, phaseY)
        buffer.setDataSet(index)
        buffer.setInverted(mChart.isInverted(dataSet.axisDependency))
        buffer.setBarWidth(mChart.barData.barWidth)
        buffer.feed(dataSet)

        trans.pointValuesToPixel(buffer.buffer)

        for (j in buffer.buffer.indices step 4) {
            if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2])) continue
            if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j])) break

            mRenderPaint.color = dataSet.getColor(j / 4)

            val left = buffer.buffer[j]
            val top = buffer.buffer[j + 1]
            val right = buffer.buffer[j + 2]
            val bottom = buffer.buffer[j + 3]

            val rect = RectF(left, top, right, bottom)
            c.drawRoundRect(rect, radius, radius, mRenderPaint)

            if (drawBorder) {
                c.drawRoundRect(rect, radius, radius, mBarBorderPaint)
            }
        }
    }

}
