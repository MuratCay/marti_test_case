package com.muratcay.presentation

import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.BindingAdapter
import java.text.NumberFormat
import java.util.Locale

object DataBindingUtil {
    @JvmStatic
    @BindingAdapter("formattedLongWithDots")
    fun AppCompatTextView.setFormattedLongWithDots(value: Long?) {
        val formattedValue = value?.let {
            val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
            numberFormat.format(it)
        } ?: ""
        text = formattedValue
    }
}