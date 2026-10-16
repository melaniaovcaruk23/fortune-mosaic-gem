package com.fortunegem.mosaic.core.ui

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.fortunegem.mosaic.R
import com.fortunegem.mosaic.databinding.ViewSectionHeaderBinding

class SectionHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ViewSectionHeaderBinding =
        ViewSectionHeaderBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        val typed = context.obtainStyledAttributes(attrs, R.styleable.SectionHeaderView)
        val title = typed.getString(R.styleable.SectionHeaderView_headerTitle)
        val caption = typed.getString(R.styleable.SectionHeaderView_headerCaption)
        val accent = typed.getColor(
            R.styleable.SectionHeaderView_headerAccent,
            ContextCompat.getColor(context, R.color.accent_cyan)
        )
        typed.recycle()
        binding.headerTitle.text = title ?: ""
        binding.headerCaption.text = caption ?: ""
        applyAccent(accent)
    }

    private fun applyAccent(color: Int) {
        val bar = GradientDrawable()
        bar.shape = GradientDrawable.RECTANGLE
        bar.cornerRadius = 2f * resources.displayMetrics.density
        bar.setColor(color)
        binding.headerBar.background = bar
    }

    fun bind(title: CharSequence, caption: CharSequence) {
        binding.headerTitle.text = title
        binding.headerCaption.text = caption
    }

    fun setAccent(color: Int) {
        applyAccent(color)
    }
}
