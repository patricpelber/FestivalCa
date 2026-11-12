package com.example.festivalcal

import android.content.Context

/** Widget style settings, stored as small ints in SharedPreferences. */
object Style {
    private val D = mapOf(
        "bg" to 0xFF101010.toInt(), "tc" to 0xFFFFFFFF.toInt(),
        "dc" to 0xFFBBBBBB.toInt(), "fe" to 0xFFFFB300.toInt(),
        "op" to 90,    // background opacity %
        "rad" to 1,    // corners: 0 square, 1 rounded, 2 round
        "size" to 100, // text size %
        "al" to 0,     // alignment: 0 left, 1 center, 2 right
        "tf" to 0,     // time format index
        "df" to 0,     // date format index
        "sd" to 1,     // show date
        "sf" to 1      // show festival
    )
    val TIME_FMT = listOf("hh:mm a", "h:mm", "HH:mm")
    val TIME_NAMES = listOf("12h AM/PM", "12h", "24h")
    val DATE_FMT = listOf("EEEE, d MMMM", "EEE, d MMM", "dd/MM/yyyy", "d MMMM yyyy")

    private fun p(c: Context) = c.getSharedPreferences("fc", Context.MODE_PRIVATE)
    fun get(c: Context, k: String): Int = p(c).getInt("s_$k", D[k]!!)
    fun set(c: Context, k: String, v: Int) { p(c).edit().putInt("s_$k", v).apply() }
    fun reset(c: Context) {
        val e = p(c).edit()
        D.keys.forEach { e.remove("s_$it") }
        e.apply()
    }
}
