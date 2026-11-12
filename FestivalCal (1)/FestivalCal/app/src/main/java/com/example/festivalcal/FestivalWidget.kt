package com.example.festivalcal

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.RemoteViews

class FestivalWidget : AppWidgetProvider() {

    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, ids: IntArray) {
        ids.forEach { mgr.updateAppWidget(it, build(ctx)) }
    }

    override fun onReceive(ctx: Context, intent: Intent) {
        super.onReceive(ctx, intent)
        val a = intent.action
        if (a == Intent.ACTION_DATE_CHANGED || a == Intent.ACTION_TIME_CHANGED || a == Intent.ACTION_TIMEZONE_CHANGED) {
            refreshAll(ctx)
        }
    }

    companion object {
        fun refreshAll(ctx: Context) {
            val mgr = AppWidgetManager.getInstance(ctx)
            val ids = mgr.getAppWidgetIds(ComponentName(ctx, FestivalWidget::class.java))
            if (ids.isNotEmpty()) mgr.updateAppWidget(ids, build(ctx))
        }

        /** Also used by the style screen for the live preview. */
        fun build(ctx: Context): RemoteViews {
            Festivals.init(ctx)
            fun g(k: String) = Style.get(ctx, k)
            val v = RemoteViews(ctx.packageName, R.layout.widget)

            // background: white shape tinted + faded
            v.setImageViewResource(R.id.bg, when (g("rad")) { 0 -> R.drawable.bg_r0; 2 -> R.drawable.bg_r2; else -> R.drawable.bg_r1 })
            v.setInt(R.id.bg, "setColorFilter", g("bg"))
            v.setInt(R.id.bg, "setImageAlpha", g("op") * 255 / 100)

            // text size + color
            val s = g("size") / 100f
            v.setTextColor(R.id.time, g("tc"))
            v.setTextViewTextSize(R.id.time, TypedValue.COMPLEX_UNIT_SP, 34f * s)
            v.setTextColor(R.id.date, g("dc"))
            v.setTextViewTextSize(R.id.date, TypedValue.COMPLEX_UNIT_SP, 15f * s)
            v.setTextColor(R.id.fest, g("fe"))
            v.setTextViewTextSize(R.id.fest, TypedValue.COMPLEX_UNIT_SP, 18f * s)

            // formats
            val tf = Style.TIME_FMT[g("tf")]
            v.setCharSequence(R.id.time, "setFormat12Hour", tf)
            v.setCharSequence(R.id.time, "setFormat24Hour", tf)
            val df = Style.DATE_FMT[g("df")]
            v.setCharSequence(R.id.date, "setFormat12Hour", df)
            v.setCharSequence(R.id.date, "setFormat24Hour", df)

            // show / hide
            v.setViewVisibility(R.id.date, if (g("sd") == 1) View.VISIBLE else View.GONE)
            v.setViewVisibility(R.id.fest, if (g("sf") == 1) View.VISIBLE else View.GONE)
            v.setTextViewText(R.id.fest, Festivals.todayText())

            // alignment
            val h = when (g("al")) { 1 -> Gravity.CENTER_HORIZONTAL; 2 -> Gravity.END; else -> Gravity.START }
            v.setInt(R.id.content, "setGravity", Gravity.CENTER_VERTICAL or h)

            val pi = PendingIntent.getActivity(
                ctx, 0, Intent(ctx, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            v.setOnClickPendingIntent(R.id.root, pi)
            return v
        }
    }
}
