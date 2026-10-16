package com.fortunegem.mosaic.core.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.fortunegem.mosaic.R
import com.fortunegem.mosaic.domain.model.MosaicPiece

class PieceTrayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val density: Float = resources.displayMetrics.density
    private val cardSize: Float = 80f * density
    private val cardGap: Float = 10f * density
    private val cardRadius: Float = 18f * density

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val facetPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cardRect = RectF()
    private val cellRect = RectF()
    private val facetPath = Path()

    private val surfaceColor: Int = ContextCompat.getColor(context, R.color.surface)
    private val strokeColor: Int = ContextCompat.getColor(context, R.color.stroke_soft)
    private val selectedColor: Int = ContextCompat.getColor(context, R.color.accent_cyan)

    private var pieces: List<MosaicPiece> = emptyList()
    private var selectedIndex: Int = 0

    var onPieceSelected: ((Int) -> Unit)? = null

    init {
        isClickable = true
        isFocusable = true
        strokePaint.style = Paint.Style.STROKE
        contentDescription = context.getString(R.string.label_pieces)
    }

    fun render(items: List<MosaicPiece>, selected: Int) {
        pieces = items
        selectedIndex = selected
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val index = indexAt(event.x)
            if (index >= 0 && index < pieces.size) {
                performClick()
                onPieceSelected?.invoke(index)
            }
            return true
        }
        return event.action == MotionEvent.ACTION_DOWN || super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun indexAt(x: Float): Int {
        if (pieces.isEmpty()) return -1
        val startX = startOffset()
        val slot = ((x - startX) / (cardSize + cardGap)).toInt()
        if (slot < 0 || slot >= pieces.size) return -1
        return slot
    }

    private fun startOffset(): Float {
        val count = pieces.size
        if (count == 0) return 0f
        val total = count * cardSize + (count - 1) * cardGap
        val free = width - total
        return if (free > 0f) free / 2f else cardGap
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (pieces.isEmpty()) return
        val startX = startOffset()
        val top = (height - cardSize) / 2f
        var index = 0
        while (index < pieces.size) {
            val left = startX + index * (cardSize + cardGap)
            cardRect.set(left, top, left + cardSize, top + cardSize)
            val isSelected = index == selectedIndex
            fillPaint.style = Paint.Style.FILL
            fillPaint.color = surfaceColor
            canvas.drawRoundRect(cardRect, cardRadius, cardRadius, fillPaint)
            strokePaint.color = if (isSelected) selectedColor else strokeColor
            strokePaint.strokeWidth = if (isSelected) 2f * density else 1f * density
            canvas.drawRoundRect(cardRect, cardRadius, cardRadius, strokePaint)
            drawPiece(canvas, pieces[index], isSelected)
            index += 1
        }
    }

    private fun drawPiece(canvas: Canvas, piece: MosaicPiece, selected: Boolean) {
        val shape = piece.shape
        if (shape.cells.isEmpty()) return
        val inset = 14f * density
        val usable = cardSize - 2f * inset
        val span = if (shape.rows > shape.cols) shape.rows else shape.cols
        val unit = usable / span
        val gridWidth = unit * shape.cols
        val gridHeight = unit * shape.rows
        val originX = cardRect.left + (cardSize - gridWidth) / 2f
        val originY = cardRect.top + (cardSize - gridHeight) / 2f
        val scale = if (selected) 1.06f else 1f
        shape.cells.forEach { packed ->
            val row = shape.rowOf(packed)
            val col = shape.colOf(packed)
            val cx = originX + col * unit + unit / 2f
            val cy = originY + row * unit + unit / 2f
            val half = unit * 0.46f * scale
            cellRect.set(cx - half, cy - half, cx + half, cy + half)
            fillPaint.color = piece.palette.base
            canvas.drawRoundRect(cellRect, 4f * density, 4f * density, fillPaint)
            facetPaint.style = Paint.Style.FILL
            facetPath.reset()
            facetPath.moveTo(cellRect.left, cellRect.top)
            facetPath.lineTo(cellRect.right, cellRect.top)
            facetPath.lineTo(cellRect.left, cellRect.bottom)
            facetPath.close()
            facetPaint.color = piece.palette.light
            canvas.drawPath(facetPath, facetPaint)
            facetPath.reset()
            facetPath.moveTo(cellRect.right, cellRect.top)
            facetPath.lineTo(cellRect.right, cellRect.bottom)
            facetPath.lineTo(cellRect.left, cellRect.bottom)
            facetPath.close()
            facetPaint.color = piece.palette.dark
            canvas.drawPath(facetPath, facetPaint)
            strokePaint.color = Color.argb(120, 255, 255, 255)
            strokePaint.strokeWidth = 1f * density
            canvas.drawLine(
                cellRect.left + unit * 0.14f,
                cellRect.top + unit * 0.12f,
                cellRect.right - unit * 0.14f,
                cellRect.top + unit * 0.12f,
                strokePaint
            )
        }
    }
}
