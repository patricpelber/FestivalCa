package com.example.festivalcal

import android.content.Context
import java.util.Calendar

/** Tiny offline festival table. Edit/add lines freely. */
object Festivals {
    // Same date every year: "MM-DD"
    private val fixed = mapOf(
        "01-01" to "New Year's Day", "01-26" to "Republic Day", "02-14" to "Valentine's Day",
        "04-14" to "Ambedkar Jayanti", "05-01" to "Labour Day", "08-15" to "Independence Day",
        "09-05" to "Teachers' Day", "10-02" to "Gandhi Jayanti", "11-14" to "Children's Day",
        "12-25" to "Christmas"
    )

    // Lunar / moving festivals: "YYYY-MM-DD". Add next year's lines here.
    private val moving = mapOf(
        "2026-01-14" to "Makar Sankranti / Pongal", "2026-01-23" to "Vasant Panchami",
        "2026-02-15" to "Maha Shivratri", "2026-03-04" to "Holi", "2026-03-19" to "Ugadi / Gudi Padwa",
        "2026-03-21" to "Eid al-Fitr", "2026-03-26" to "Ram Navami", "2026-03-31" to "Mahavir Jayanti",
        "2026-04-03" to "Good Friday", "2026-04-05" to "Easter", "2026-05-01" to "Buddha Purnima",
        "2026-05-27" to "Eid al-Adha", "2026-06-26" to "Muharram", "2026-07-16" to "Rath Yatra",
        "2026-07-29" to "Guru Purnima", "2026-08-26" to "Onam", "2026-08-28" to "Raksha Bandhan",
        "2026-09-04" to "Janmashtami", "2026-09-14" to "Ganesh Chaturthi", "2026-10-11" to "Navratri begins",
        "2026-10-20" to "Dussehra", "2026-10-29" to "Karwa Chauth", "2026-11-06" to "Dhanteras",
        "2026-11-08" to "Diwali", "2026-11-09" to "Govardhan Puja", "2026-11-11" to "Bhai Dooj",
        "2026-11-15" to "Chhath Puja", "2026-11-24" to "Guru Nanak Jayanti"
    )

    private fun key(c: Calendar) = "%02d-%02d".format(c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
    private fun full(c: Calendar) = "%04d-".format(c.get(Calendar.YEAR)) + key(c)

    // ---- Downloaded data (saved by Calibrate), kept in a tiny SharedPreferences entry ----
    private var online: Map<String, String> = emptyMap()
    private var onlineYears: Set<Int> = emptySet()

    fun init(ctx: Context) {
        val raw = ctx.getSharedPreferences("fc", Context.MODE_PRIVATE).getString("data", "") ?: ""
        val m = HashMap<String, String>()
        raw.lineSequence().forEach {
            val i = it.indexOf('\t')
            if (i == 10) m[it.substring(0, 10)] = it.substring(11)
        }
        online = m
        onlineYears = m.keys.map { it.substring(0, 4).toInt() }.toSet()
    }

    fun save(ctx: Context, data: Map<String, String>) {
        val sb = StringBuilder()
        data.forEach { (d, n) -> sb.append(d).append('\t').append(n).append('\n') }
        ctx.getSharedPreferences("fc", Context.MODE_PRIVATE).edit().putString("data", sb.toString()).apply()
        init(ctx)
    }

    fun on(c: Calendar): String? {
        // If calibrated data covers this year, trust it; otherwise use the built-in table.
        if (c.get(Calendar.YEAR) in onlineYears) return online[full(c)]
        val l = listOfNotNull(fixed[key(c)], moving[full(c)])
        return if (l.isEmpty()) null else l.joinToString(" \u2022 ")
    }

    /** Next festival strictly after c within a year: (days away, name). */
    fun next(c: Calendar): Pair<Int, String>? {
        val d = c.clone() as Calendar
        for (i in 1..366) {
            d.add(Calendar.DAY_OF_YEAR, 1)
            on(d)?.let { return i to it }
        }
        return null
    }

    fun todayText(c: Calendar = Calendar.getInstance()): String =
        on(c) ?: next(c)?.let { (n, name) -> "Next: $name (" + (if (n == 1) "tomorrow" else "in $n days") + ")" }
        ?: "No festival today"
}
