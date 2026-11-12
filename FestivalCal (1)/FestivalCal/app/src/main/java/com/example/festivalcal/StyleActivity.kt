package com.example.festivalcal

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StyleActivity : Activity() {

    private class Theme(val name: String, val bg: Int, val time: Int, val date: Int, val fest: Int, val op: Int)

    private val themes = listOf(
        Theme("Dark", 0x101010, 0xFFFFFF, 0xBBBBBB, 0xFFB300, 90),
        Theme("Light", 0xF5F5F5, 0x111111, 0x555555, 0xD84315, 95),
        Theme("AMOLED", 0x000000, 0xFFFFFF, 0x9E9E9E, 0x00E676, 100),
        Theme("Glass", 0x000000, 0xFFFFFF, 0xE0E0E0, 0xFFD54F, 35),
        Theme("Ocean", 0x0D47A1, 0xFFFFFF, 0xBBDEFB, 0xFFEB3B, 92),
        Theme("Forest", 0x1B5E20, 0xFFFFFF, 0xC8E6C9, 0xFFCA28, 92),
        Theme("Rose", 0x880E4F, 0xFFFFFF, 0xF8BBD0, 0xFFEE58, 92),
        Theme("Purple", 0x311B92, 0xFFFFFF, 0xD1C4E9, 0x69F0AE, 92),
        Theme("Sunset", 0xE65100, 0xFFFFFF, 0xFFE0B2, 0xFFF59D, 92),
        Theme("Paper", 0xFFF8E1, 0x3E2723, 0x6D4C41, 0xBF360C, 95)
    )
    private val palette = intArrayOf(
        0x000000, 0x212121, 0x616161, 0xBDBDBD, 0xFFFFFF, 0xF44336, 0xFF9800, 0xFFB300,
        0xFFEB3B, 0x4CAF50, 0x00E676, 0x00BCD4, 0x2196F3, 0x3F51B5, 0x9C27B0, 0xE91E63, 0x795548
    )
    private val solid = -0x1000000
    private val amber = Color.parseColor("#FFB300")
    private val syncs = ArrayList<() -> Unit>()
    private lateinit var preview: FrameLayout

    private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()

    private fun shape(fill: Int, stroke: Int, w: Int = 1) = GradientDrawable().apply {
        cornerRadius = dp(20).toFloat(); setColor(fill); setStroke(dp(w), stroke)
    }

    private fun styleChip(t: TextView, sel: Boolean) {
        t.background = shape(if (sel) amber else Color.parseColor("#3C3C3C"), if (sel) amber else Color.GRAY)
        t.setTextColor(if (sel) Color.BLACK else Color.WHITE)
    }

    private fun label(t: String) = TextView(this).apply {
        text = t; textSize = 14f; setTextColor(Color.LTGRAY); typeface = Typeface.DEFAULT_BOLD
        setPadding(0, dp(18), 0, dp(6))
    }

    private fun LinearLayout.section(name: String, v: View) { addView(label(name)); addView(v) }

    private fun scroller(row: View): View = HorizontalScrollView(this).apply {
        isHorizontalScrollBarEnabled = false
        addView(row)
    }

    private fun changed() { syncs.forEach { it() }; refresh() }

    private fun refresh() {
        preview.removeAllViews()
        preview.addView(FestivalWidget.build(this).apply(this, preview))
        FestivalWidget.refreshAll(this)
    }

    private fun themeRow(): View {
        val row = LinearLayout(this)
        themes.forEach { t ->
            val tv = TextView(this)
            tv.text = t.name; tv.textSize = 14f; tv.typeface = Typeface.DEFAULT_BOLD
            tv.setTextColor(t.time or solid)
            tv.setPadding(dp(14), dp(10), dp(14), dp(10))
            tv.background = shape(t.bg or solid, Color.GRAY)
            tv.setOnClickListener {
                Style.set(this, "bg", t.bg or solid); Style.set(this, "tc", t.time or solid)
                Style.set(this, "dc", t.date or solid); Style.set(this, "fe", t.fest or solid)
                Style.set(this, "op", t.op)
                changed()
            }
            row.addView(tv, LinearLayout.LayoutParams(-2, -2).apply { rightMargin = dp(8) })
        }
        return scroller(row)
    }

    private fun chips(key: String, opts: List<String>): View {
        val row = LinearLayout(this)
        val items = opts.mapIndexed { i, name ->
            val tv = TextView(this)
            tv.text = name; tv.textSize = 14f; tv.typeface = Typeface.DEFAULT_BOLD
            tv.setPadding(dp(14), dp(8), dp(14), dp(8))
            tv.setOnClickListener { Style.set(this, key, i); changed() }
            row.addView(tv, LinearLayout.LayoutParams(-2, -2).apply { rightMargin = dp(8) })
            tv
        }
        syncs.add { items.forEachIndexed { i, tv -> styleChip(tv, i == Style.get(this, key)) } }
        return scroller(row)
    }

    private fun swatches(key: String): View {
        val row = LinearLayout(this)
        val dots = palette.map { c ->
            val d = View(this)
            d.setOnClickListener { Style.set(this, key, c or solid); changed() }
            row.addView(d, LinearLayout.LayoutParams(dp(34), dp(34)).apply { rightMargin = dp(8) })
            d
        }
        syncs.add {
            val cur = Style.get(this, key)
            dots.forEachIndexed { i, d ->
                val on = cur == (palette[i] or solid)
                d.background = GradientDrawable().apply {
                    this.shape = GradientDrawable.OVAL
                    setColor(palette[i] or solid)
                    setStroke(if (on) dp(3) else dp(1), if (on) Color.WHITE else Color.GRAY)
                }
            }
        }
        return scroller(row)
    }

    private fun seek(key: String, lo: Int, hi: Int, unit: String): View {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        val t = TextView(this)
        t.setTextColor(Color.WHITE); t.typeface = Typeface.DEFAULT_BOLD
        val sb = SeekBar(this)
        sb.max = hi - lo
        sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar, p: Int, user: Boolean) {
                if (!user) return
                Style.set(this@StyleActivity, key, p + lo)
                t.text = "${p + lo}$unit"
                refresh()
            }
            override fun onStartTrackingTouch(s: SeekBar) {}
            override fun onStopTrackingTouch(s: SeekBar) {}
        })
        syncs.add { val v = Style.get(this, key); sb.progress = v - lo; t.text = "$v$unit" }
        box.addView(t); box.addView(sb)
        return box
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.setPadding(dp(18), dp(18), dp(18), dp(28))

        val title = TextView(this)
        title.text = "WIDGET STYLE"; title.textSize = 22f; title.typeface = Typeface.DEFAULT_BOLD
        title.setTextColor(Color.WHITE); title.setPadding(0, 0, 0, dp(12))
        col.addView(title)

        preview = FrameLayout(this)
        col.addView(preview, LinearLayout.LayoutParams(dp(270), dp(130)).apply { gravity = Gravity.CENTER_HORIZONTAL })

        val now = Date()
        val dateNames = Style.DATE_FMT.map { SimpleDateFormat(it, Locale.getDefault()).format(now) }

        col.section("Themes", themeRow())
        col.section("Background color", swatches("bg"))
        col.section("Background opacity", seek("op", 0, 100, "%"))
        col.section("Time color", swatches("tc"))
        col.section("Date color", swatches("dc"))
        col.section("Festival color", swatches("fe"))
        col.section("Corners", chips("rad", listOf("Square", "Rounded", "Round")))
        col.section("Text size", seek("size", 60, 160, "%"))
        col.section("Alignment", chips("al", listOf("Left", "Center", "Right")))
        col.section("Time format", chips("tf", Style.TIME_NAMES))
        col.section("Date format", chips("df", dateNames))
        col.section("Show date", chips("sd", listOf("Hide", "Show")))
        col.section("Show festival", chips("sf", listOf("Hide", "Show")))

        val reset = Button(this)
        reset.text = "RESET TO DEFAULT"; reset.typeface = Typeface.DEFAULT_BOLD
        reset.setOnClickListener { Style.reset(this); changed() }
        col.addView(reset, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(24) })

        val sv = ScrollView(this)
        sv.setBackgroundColor(Color.parseColor("#2B2B2B"))
        sv.addView(col)
        setContentView(sv)
        syncs.forEach { it() }
        refresh()
    }
}
