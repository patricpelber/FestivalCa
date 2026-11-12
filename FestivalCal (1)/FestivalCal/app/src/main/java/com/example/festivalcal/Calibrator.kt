package com.example.festivalcal

import android.content.Context
import java.net.HttpURLConnection
import java.net.URL
import java.util.TreeMap

/** Downloads a public iCal holiday/festival feed and stores date -> name. Call off the main thread. */
object Calibrator {
    // Change this URL to use another region's public iCal feed.
    const val FEED = "https://calendar.google.com/calendar/ical/en.indian%23holiday%40group.v.calendar.google.com/public/basic.ics"

    /** Returns number of festival days saved; throws on failure. */
    fun run(ctx: Context): Int {
        val c = URL(FEED).openConnection() as HttpURLConnection
        c.connectTimeout = 10000; c.readTimeout = 15000
        val text = try {
            if (c.responseCode != 200) throw Exception("Server replied ${c.responseCode}")
            c.inputStream.bufferedReader().readText()
        } finally { c.disconnect() }

        val byDate = TreeMap<String, MutableList<String>>()
        var date: String? = null
        var name: String? = null
        text.replace("\r\n ", "").replace("\n ", "").lineSequence().forEach { l ->
            when {
                l.startsWith("BEGIN:VEVENT") -> { date = null; name = null }
                l.startsWith("DTSTART") -> {
                    val v = l.substringAfterLast(':').trim()
                    if (v.length >= 8) date = "${v.substring(0, 4)}-${v.substring(4, 6)}-${v.substring(6, 8)}"
                }
                l.startsWith("SUMMARY") ->
                    name = l.substringAfter(':').replace("\\,", ",").replace("\\;", ";").replace("\\n", " ").trim()
                l.startsWith("END:VEVENT") -> {
                    val d = date; val n = name
                    if (d != null && !n.isNullOrEmpty()) {
                        val list = byDate.getOrPut(d) { mutableListOf() }
                        if (n !in list) list.add(n)
                    }
                }
            }
        }
        if (byDate.isEmpty()) throw Exception("No festivals found in feed")
        Festivals.save(ctx, byDate.mapValues { it.value.joinToString(" \u2022 ") })
        return byDate.size
    }
}
