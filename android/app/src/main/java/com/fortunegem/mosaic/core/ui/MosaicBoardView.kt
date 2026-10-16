package com.fortunegem.mosaic.core.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.fortunegem.mosaic.R
import com.fortunegem.mosaic.core.config.GameConfig
import com.fortunegem.mosaic.domain.model.BoardState
import com.fortunegem.mosaic.domain.model.GemPalette

class MosaicBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val density: Float = resources.displayMetrics.density
    private val framePadding: Float = 8f * density
    private val frameStroke: Float = 2f * density
    private val boardFrame: Float = framePadding + frameStroke
    private val maxBoard: Float = 380f * density
    private val cellRadius: Float = 6f * density

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val dashPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val facetPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sweepPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sparkPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val cellRect = RectF()
    private val facetPath = Path()

    private var cellSize: Float = 0f
    private var boardExtent: Int = 0

    private var board: BoardState = BoardState.empty(GameConfig.BOARD_SIZE, emptySet())
    private var ghostCells: List<Int> = emptyList()
    private var ghostValid: Boolean = true
    private var flashCells: List<Int> = emptyList()

    private var sweepProgress: Float = -1f
    private var sweepAnimator: ValueAnimator? = null

    private val emptyFill: Int = ContextCompat.getColor(context, R.color.surface)
    private val emptyStroke: Int = ContextCompat.getColor(context, R.color.stroke_board)
    private val targetFill: Int = ContextCompat.getColor(context, R.color.surface_alt)
    private val targetStroke: Int = ContextCompat.getColor(context, R.color.primary_violet)
    private val hintColor: Int = ContextCompat.getColor(context, R.color.facet_hint)
    private val validColor: Int = ContextCompat.getColor(context, R.color.accent_cyan)
    private val invalidColor: Int = ContextCompat.getColor(context, R.color.accent_rose)
    private val frameColor: Int = ContextCompat.getColor(context, R.color.stroke_soft)
    private val sparkColor: Int = ContextCompat.getColor(context, R.color.accent_amber)

    var onCellTapped: ((Int) -> Unit)? = null

    init {
        isClickable = true
        isFocusable = true
        dashPaint.style = Paint.Style.STROKE
        dashPaint.strokeWidth = 1.5f * density
        dashPaint.pathEffect = DashPathEffect(floatArrayOf(5f * density, 4f * density), 0f)
        strokePaint.style = Paint.Style.STROKE
        contentDescription = context.getString(R.string.brand_subtitle)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthAvailable = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        val heightAvailable = MeasureSpec.getSize(heightMeasureSpec).toFloat()
        var limit = widthAvailable
        if (heightAvailable > 0f && heightAvailable < limit) limit = heightAvailable
        if (limit > maxBoard) limit = maxBoard
        if (limit < 120f * density) limit = 120f * density
        val inner = limit - 2f * boardFrame
        val columns = GameConfig.BOARD_SIZE
        val computed = Math.floor((inner / columns).toDouble()).toFloat()
        cellSize = if (computed < 1f) 1f else computed
        boardExtent = (cellSize * columns + 2f * boardFrame).toInt()
        setMeasuredDimension(boardExtent, boardExtent)
    }

    fun render(state: BoardState) {
        board = state
        invalidate()
    }

    fun showGhost(cells: List<Int>, valid: Boolean) {
        ghostCells = cells
        ghostValid = valid
        invalidate()
    }

    fun clearGhost() {
        ghostCells = emptyList()
        invalidate()
    }

    fun flash(cells: List<Int>) {
        flashCells = cells
        invalidate()
    }

    fun clearFlash() {
        flashCells = emptyList()
        invalidate()
    }

    fun playSweep() {
        stopAnimations()
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = GameConfig.SWEEP_MS
        animator.addUpdateListener { value ->
            try {
                sweepProgress = value.animatedValue as Float
                invalidate()
            } catch (e: Exception) {
                sweepProgress = -1f
            }
        }
        sweepAnimator = animator
        animator.start()
    }

    fun stopAnimations() {
        try {
            sweepAnimator?.cancel()
        } catch (e: Exception) {
            sweepProgress = -1f
        }
        sweepAnimator = null
    }

    override fun onDetachedFromWindow() {
        stopAnimations()
        super.onDetachedFromWindow()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val cell = cellAt(event.x, event.y)
            if (cell >= 0) {
                performClick()
                onCellTapped?.invoke(cell)
            }
            return true
        }
        return event.action == MotionEvent.ACTION_DOWN || super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun cellAt(x: Float, y: Float): Int {
        if (cellSize <= 0f) return -1
        val col = ((x - boardFrame) / cellSize).toInt()
        val row = ((y - boardFrame) / cellSize).toInt()
        val size = GameConfig.BOARD_SIZE
        if (col < 0 || col >= size || row < 0 || row >= size) return -1
        return row * size + col
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (cellSize <= 0f) return
        drawFrame(canvas)
        val size = GameConfig.BOARD_SIZE
        var index = 0
        while (index < size * size) {
            val row = index / size
            val col = index % size
            val left = boardFrame + col * cellSize
            val top = boardFrame + row * cellSize
            cellRect.set(
                left + 1.5f,
                top + 1.5f,
                left + cellSize - 1.5f,
                top + cellSize - 1.5f
            )
            val palette = board.filled[index]
            when {
                palette != null -> drawFacet(canvas, palette)
                board.silhouette.contains(index) -> drawTarget(canvas)
                else -> drawEmpty(canvas)
            }
            index += 1
        }
        drawGhost(canvas)
        drawFlash(canvas)
        drawSweep(canvas)
    }

    private fun drawFrame(canvas: Canvas) {
        fillPaint.style = Paint.Style.FILL
        fillPaint.color = emptyFill
        val outer = RectF(0f, 0f, boardExtent.toFloat(), boardExtent.toFloat())
        canvas.drawRoundRect(outer, cellRadius * 2f, cellRadius * 2f, fillPaint)
        strokePaint.color = frameColor
        strokePaint.strokeWidth = frameStroke
        val inset = frameStroke / 2f
        outer.inset(inset, inset)
        canvas.drawRoundRect(outer, cellRadius * 2f, cellRadius * 2f, strokePaint)
    }

    private fun drawEmpty(canvas: Canvas) {
        fillPaint.style = Paint.Style.FILL
        fillPaint.color = emptyFill
        canvas.drawRoundRect(cellRect, cellRadius, cellRadius, fillPaint)
        strokePaint.color = emptyStroke
        strokePaint.strokeWidth = 1f * density
        canvas.drawRoundRect(cellRect, cellRadius, cellRadius, strokePaint)
    }

    private fun drawTarget(canvas: Canvas) {
        fillPaint.style = Paint.Style.FILL
        fillPaint.color = targetFill
        canvas.drawRoundRect(cellRect, cellRadius, cellRadius, fillPaint)
        dashPaint.color = targetStroke
        canvas.drawRoundRect(cellRect, cellRadius, cellRadius, dashPaint)
        facetPath.reset()
        facetPath.moveTo(cellRect.centerX(), cellRect.top + cellSize * 0.22f)
        facetPath.lineTo(cellRect.right - cellSize * 0.24f, cellRect.bottom - cellSize * 0.26f)
        facetPath.lineTo(cellRect.left + cellSize * 0.24f, cellRect.bottom - cellSize * 0.26f)
        facetPath.close()
        facetPaint.style = Paint.Style.FILL
        facetPaint.color = hintColor
        canvas.drawPath(facetPath, facetPaint)
    }

    private fun drawFacet(canvas: Canvas, palette: GemPalette) {
        fillPaint.style = Paint.Style.FILL
        fillPaint.color = palette.base
        canvas.drawRoundRect(cellRect, cellRadius, cellRadius, fillPaint)
        facetPaint.style = Paint.Style.FILL
        facetPath.reset()
        facetPath.moveTo(cellRect.left, cellRect.top)
        facetPath.lineTo(cellRect.right, cellRect.top)
        facetPath.lineTo(cellRect.left, cellRect.bottom)
        facetPath.close()
        facetPaint.color = palette.light
        canvas.drawPath(facetPath, facetPaint)
        facetPath.reset()
        facetPath.moveTo(cellRect.right, cellRect.top)
        facetPath.lineTo(cellRect.right, cellRect.bottom)
        facetPath.lineTo(cellRect.left, cellRect.bottom)
        facetPath.close()
        facetPaint.color = palette.dark
        canvas.drawPath(facetPath, facetPaint)
        strokePaint.color = Color.argb(140, 255, 255, 255)
        strokePaint.strokeWidth = 1f * density
        canvas.drawLine(
            cellRect.left + cellSize * 0.16f,
            cellRect.top + cellSize * 0.14f,
            cellRect.right - cellSize * 0.16f,
            cellRect.top + cellSize * 0.14f,
            strokePaint
        )
    }

    private fun drawGhost(canvas: Canvas) {
        if (ghostCells.isEmpty()) return
        val color = if (ghostValid) validColor else invalidColor
        strokePaint.color = color
        strokePaint.strokeWidth = 2f * density
        fillPaint.style = Paint.Style.FILL
        fillPaint.color = Color.argb(70, Color.red(color), Color.green(color), Color.blue(color))
        val size = GameConfig.BOARD_SIZE
        ghostCells.forEach { cell ->
            if (cell >= 0 && cell < size * size) {
                val left = boardFrame + (cell % size) * cellSize
                val top = boardFrame + (cell / size) * cellSize
                cellRect.set(
                    left + 2f,
                    top + 2f,
                    left + cellSize - 2f,
                    top + cellSize - 2f
                )
                canvas.drawRoundRect(cellRect, cellRadius, cellRadius, fillPaint)
                canvas.drawRoundRect(cellRect, cellRadius, cellRadius, strokePaint)
            }
        }
    }

    private fun drawFlash(canvas: Canvas) {
        if (flashCells.isEmpty()) return
        fillPaint.style = Paint.Style.FILL
        fillPaint.color = Color.argb(90, 224, 96, 126)
        val size = GameConfig.BOARD_SIZE
        flashCells.forEach { cell ->
            if (cell >= 0 && cell < size * size) {
                val left = boardFrame + (cell % size) * cellSize
                val top = boardFrame + (cell / size) * cellSize
                cellRect.set(left, top, left + cellSize, top + cellSize)
                canvas.drawRoundRect(cellRect, cellRadius, cellRadius, fillPaint)
            }
        }
    }

    private fun drawSweep(canvas: Canvas) {
        if (sweepProgress < 0f) return
        val extent = boardExtent.toFloat()
        val bandWidth = extent * 0.28f
        val travel = extent * 2f + bandWidth
        val offset = sweepProgress * travel - bandWidth
        sweepPaint.style = Paint.Style.FILL
        sweepPaint.color = Color.argb(56, 255, 255, 255)
        facetPath.reset()
        facetPath.moveTo(offset, extent)
        facetPath.lineTo(offset + bandWidth, extent)
        facetPath.lineTo(offset + bandWidth + extent, 0f)
        facetPath.lineTo(offset + extent, 0f)
        facetPath.close()
        canvas.drawPath(facetPath, sweepPaint)
        sparkPaint.style = Paint.Style.FILL
        sparkPaint.color = sparkColor
        var spark = 0
        while (spark < 12) {
            val fraction = (spark + 1) / 13f
            val radius = (1f - sweepProgress) * 4f * density
            if (radius > 0f) {
                canvas.drawCircle(
                    extent * fraction,
                    extent * (0.2f + 0.6f * ((spark * 7) % 11) / 11f),
                    radius,
                    sparkPaint
                )
            }
            spark += 1
        }
    }
}
