package com.fortunegem.mosaic.core.ui

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.fortunegem.mosaic.R
import com.fortunegem.mosaic.core.ext.ViewExt
import com.fortunegem.mosaic.databinding.ViewStatCardBinding

class StatCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewStatCardBinding =
        ViewStatCardBinding.inflate(LayoutInflater.from(context), this)

    private var accentColor: Int = ContextCompat.getColor(context, R.color.primary_violet)
    private var compact: Boolean = false
    private var boundValue: CharSequence = ""
    private var boundLabel: CharSequence = ""

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        val typed = context.obtainStyledAttributes(attrs, R.styleable.StatCardView)
        val value = typed.getString(R.styleable.StatCardView_statValue)
        val label = typed.getString(R.styleable.StatCardView_statLabel)
        accentColor = typed.getColor(R.styleable.StatCardView_statAccent, accentColor)
        compact = typed.getBoolean(R.styleable.StatCardView_statCompact, false)
        typed.recycle()
        applyCompact()
        binding.statValue.text = value ?: ""
        binding.statLabel.text = label ?: ""
        applyAccent()
    }

    private fun applyCompact() {
        val density = resources.displayMetrics.density
        if (compact) {
            background = null
            elevation = 0f
            val pad = (6f * density).toInt()
            setPadding(pad, pad, pad, pad)
            binding.statValue.textSize = 15f
            binding.statLabel.textSize = 9f
        } else {
            background = ContextCompat.getDrawable(context, R.drawable.bg_card_surface)
            elevation = 2f * density
            val padH = (10f * density).toInt()
            val padV = (8f * density).toInt()
            setPadding(padH, padV, padH, padV)
            binding.statValue.textSize = 18f
            binding.statLabel.textSize = 10f
        }
    }

    private fun applyAccent() {
        binding.statValue.setTextColor(accentColor)
        val dot = GradientDrawable()
        dot.shape = GradientDrawable.OVAL
        dot.setColor(accentColor)
        binding.statDot.background = dot
    }

    fun bind(value: CharSequence, label: CharSequence) {
        if (boundValue == value && boundLabel == label) return
        boundValue = value
        boundLabel = label
        binding.statValue.text = value
        binding.statLabel.text = label
        ViewExt.describeState(this, label.toString() + " " + value)
    }

    fun setAccent(color: Int) {
        accentColor = color
        applyAccent()
    }

    fun setCompactMode(enabled: Boolean) {
        compact = enabled
        applyCompact()
        applyAccent()
    }
}
