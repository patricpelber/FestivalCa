package com.example.festivalcal

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.CalendarView
import android.widget.LinearLayout
import android.widget.TextClock
import android.widget.TextView
import java.util.Calendar

/** No XML, no libraries: the UI is built in code to stay tiny. */
class MainActivity : Activity() {

    private lateinit var today: TextView
    private lateinit var picked: TextView
    private lateinit var status: TextView
    private lateinit var btn: Button

    private fun tv(size: Float, color: Int) = TextView(this).apply {
        textSize = size; setTextColor(color); typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER_HORIZONTAL
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        Festivals.init(this)
        val p = (20 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#101010"))
            setPadding(p, p, p, p)
        }
        val clock = TextClock(this).apply {
            format12Hour = "hh:mm:ss a"; format24Hour = "HH:mm:ss"
            textSize = 44f; setTextColor(Color.WHITE); typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER_HORIZONTAL
        }
        val date = TextClock(this).apply {
            format12Hour = "EEEE, d MMMM yyyy"; format24Hour = "EEEE, d MMMM yyyy"
            textSize = 18f; setTextColor(Color.LTGRAY); typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER_HORIZONTAL
        }
        today = tv(22f, Color.parseColor("#FFB300")).apply { setPadding(0, p / 2, 0, p / 2) }
        picked = tv(18f, Color.parseColor("#4FC3F7")).apply { setPadding(0, p / 2, 0, 0) }
        status = tv(13f, Color.GRAY)
        btn = Button(this).apply {
            text = "CALIBRATE"; typeface = Typeface.DEFAULT_BOLD
            setOnClickListener { calibrate() }
        }
        val cal = CalendarView(this)
        cal.setOnDateChangeListener { _, y, m, d ->
            val c = Calendar.getInstance().apply { set(y, m, d) }
            picked.text = "%02d/%02d/%d: ".format(d, m + 1, y) + (Festivals.on(c) ?: "No festival")
        }
        root.addView(clock); root.addView(date); root.addView(today)
        val styleBtn = Button(this).apply {
            text = "WIDGET STYLE"; typeface = Typeface.DEFAULT_BOLD
            setOnClickListener { startActivity(Intent(this@MainActivity, StyleActivity::class.java)) }
        }
        root.addView(btn); root.addView(styleBtn); root.addView(status)
        root.addView(cal); root.addView(picked)
        setContentView(root)
    }

    private fun calibrate() {
        btn.isEnabled = false
        status.text = "Downloading festival dates..."
        Thread {
            val msg = try {
                "Calibrated: ${Calibrator.run(this)} festival days saved"
            } catch (e: Exception) {
                "Failed: ${e.message ?: "no internet?"}"
            }
            runOnUiThread {
                status.text = msg
                btn.isEnabled = true
                today.text = Festivals.todayText()
                FestivalWidget.refreshAll(this)
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        today.text = Festivals.todayText()
    }
}
