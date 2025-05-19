package com.hruby.ulydetailmodule.ui.namereneHodnotyUl

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.hruby.ulydetailmodule.R
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class CustomMarkerView(
    context: Context,
    layoutResource: Int
) : MarkerView(context, layoutResource) {

    private val tvContent: TextView = findViewById(R.id.tvContent)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e != null && chartView is LineChart) {
            val chart = chartView as LineChart
            val sb = StringBuilder()
            val timestamp = e.x.toLong()

            // Pokud je náhodou timestamp v milisekundách:
            val timestampSeconds = if (timestamp > 10_000_000_000L) timestamp / 1000 else timestamp

            val formattedDate = formatUnixTimestamp(timestampSeconds)

            Log.d("CustomMarkerView", "refreshContent - DataPoint: $e, Timestamp: $timestamp")

            chart.data?.dataSets?.forEach { dataSet ->
                Log.d("CustomMarkerView", "Processing dataset: ${dataSet.label}")

                if (dataSet != null) {
                    val entry = dataSet.getEntryForXValue(e.x, e.y)
                    if (entry != null) {
                        val label = dataSet.label
                        val value = entry.y
                        val jednotka = when {
                            label.contains("Teplota") -> "°C"
                            label.contains("Vlhkost") -> "%"
                            label.contains("Váha") -> "kg"
                            label.contains("Frekvence") -> "Hz"
                            else -> ""
                        }
                        sb.append("${"%.1f".format(value)} $jednotka\n")
                    } else {
                        Log.d("CustomMarkerView", "No entry found for x:${e.x} y:${e.y}")
                    }
                } else {
                    Log.d("CustomMarkerView", "Null dataset encountered!")
                }
            }
            sb.append(formattedDate)
            tvContent.text = sb.toString().trim()
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF((-width / 2).toFloat(), -height.toFloat())
    }

    private fun formatUnixTimestamp(seconds: Long): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
                .withZone(ZoneId.systemDefault())
            formatter.format(Instant.ofEpochSecond(seconds))
        } else {
            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
            sdf.format(Date(seconds * 1000))
        }
    }
}