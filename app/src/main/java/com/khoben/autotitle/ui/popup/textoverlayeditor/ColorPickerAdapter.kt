package com.khoben.autotitle.ui.popup.textoverlayeditor

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.khoben.autotitle.R


class ColorPickerAdapter(
    private val context: Context,
    private val colorPickerColors: List<Int>
) : RecyclerView.Adapter<ColorPickerAdapter.ViewHolder>() {

    private val inflater = LayoutInflater.from(context)

    var onColorPickerClickListener: OnColorPickerClickListener? = null

    constructor(context: Context) : this(context, getDefaultColors(context))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.color_picker_item_list, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        buildColorPickerView(holder.colorPickerView, colorPickerColors[position])
    }

    override fun getItemCount() = colorPickerColors.size

    private fun buildColorPickerView(view: View, colorCode: Int) {
        view.visibility = View.VISIBLE
        val biggerCircle = ShapeDrawable(OvalShape())
        biggerCircle.intrinsicHeight = 20
        biggerCircle.intrinsicWidth = 20
        biggerCircle.bounds = Rect(0, 0, 20, 20)
        biggerCircle.paint.color = colorCode
        val smallerCircle = ShapeDrawable(OvalShape())
        smallerCircle.intrinsicHeight = 5
        smallerCircle.intrinsicWidth = 5
        smallerCircle.bounds = Rect(0, 0, 5, 5)
        smallerCircle.paint.color = ContextCompat.getColor(context, R.color.white)
        smallerCircle.setPadding(10, 10, 10, 10)
        val drawables = arrayOf<Drawable>(smallerCircle, biggerCircle)
        val layerDrawable = LayerDrawable(drawables)
        view.background = layerDrawable
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var colorPickerView: View = itemView.findViewById(R.id.color_picker_view)

        init {
            itemView.setOnClickListener {
                onColorPickerClickListener?.onColorPickerClickListener(
                    colorPickerColors[adapterPosition]
                )
            }
        }
    }

    interface OnColorPickerClickListener {
        fun onColorPickerClickListener(colorCode: Int)
    }

    companion object {
        fun getDefaultColors(context: Context): List<Int> {
            val colorPickerColors: ArrayList<Int> = ArrayList()
            colorPickerColors.add(ContextCompat.getColor(context, R.color.blue_color_picker))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.brown_color_picker))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.green_color_picker))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.orange_color_picker))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.red_color_picker))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.black))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.red_orange_color_picker))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.sky_blue_color_picker))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.violet_color_picker))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.white))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.yellow_color_picker))
            colorPickerColors.add(
                ContextCompat.getColor(
                    context,
                    R.color.yellow_green_color_picker
                )
            )
            return colorPickerColors
        }
    }
}