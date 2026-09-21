package com.example.mkat_nur.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.SystemClock
import android.util.Log
import android.widget.RemoteViews
import com.example.mkat_nur.MainActivity
import com.example.mkat_nur.R
import com.example.mkat_nur.model.PrayerData
import com.example.mkat_nur.util.PrayerManager
import java.util.*

class DialWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        schedulePeriodicUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val actions = listOf(
            "REFRESH_WIDGET",
            Intent.ACTION_TIME_TICK,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED
        )
        if (intent.action in actions) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, DialWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }

            if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
                schedulePeriodicUpdate(context)
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        schedulePeriodicUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelPeriodicUpdate(context)
    }

    private fun schedulePeriodicUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DialWidgetProvider::class.java).apply {
            action = "REFRESH_WIDGET"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 998, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val now = Calendar.getInstance()
        now.set(Calendar.SECOND, 0)
        now.set(Calendar.MILLISECOND, 0)
        val nextMinute = now.timeInMillis + 60000

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC,
                nextMinute,
                pendingIntent
            )
        } catch (_: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC,
                nextMinute,
                pendingIntent
            )
        }
    }

    private fun cancelPeriodicUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, DialWidgetProvider::class.java).apply {
            action = "REFRESH_WIDGET"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 998, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_dial)
            val prayerManager = PrayerManager(context)
            val data = prayerManager.getTodayPrayerData()

            if (data != null) {
                // Generate Top Dial Bitmap
                val bitmap = renderDialBitmap(context, data, 800, 360)
                if (bitmap != null) {
                    views.setImageViewBitmap(R.id.widget_dial_canvas, bitmap)
                }

                // Fill Bottom Row Times
                val timings = data.timings
                val imsak = timings.fajr.substringBefore(" ")
                val gunes = timings.sunrise.substringBefore(" ")
                val ogle = timings.dhuhr.substringBefore(" ")
                val ikindi = timings.asr.substringBefore(" ")
                val aksam = timings.maghrib.substringBefore(" ")
                val yatsi = timings.isha.substringBefore(" ")

                views.setTextViewText(R.id.tv_imsak, imsak)
                views.setTextViewText(R.id.tv_gunes, gunes)
                views.setTextViewText(R.id.tv_ogle, ogle)
                views.setTextViewText(R.id.tv_ikindi, ikindi)
                views.setTextViewText(R.id.tv_aksam, aksam)
                views.setTextViewText(R.id.tv_yatsi, yatsi)

                // Highlight Active Prayer
                val activePrayer = prayerManager.getCurrentVakit(data)
                val activeColor = 0xFF00E5FF.toInt() // Vibrant Cyan
                val defaultColor = 0xFFFFFFFF.toInt()
                val labelDefault = 0xFFAAAAAA.toInt()

                val prayerKeys = listOf("imsak", "gunes", "ogle", "ikindi", "aksam", "yatsi")
                val tvIds = listOf(R.id.tv_imsak, R.id.tv_gunes, R.id.tv_ogle, R.id.tv_ikindi, R.id.tv_aksam, R.id.tv_yatsi)
                val labelIds = listOf(R.id.label_imsak, R.id.label_gunes, R.id.label_ogle, R.id.label_ikindi, R.id.label_aksam, R.id.label_yatsi)

                for (i in prayerKeys.indices) {
                    val isCurr = prayerKeys[i] == activePrayer
                    val c = if (isCurr) activeColor else defaultColor
                    val lc = if (isCurr) activeColor else labelDefault
                    views.setTextColor(tvIds[i], c)
                    views.setTextColor(labelIds[i], lc)
                }

                // Center Dome Title & Live Chronometer Countdown
                val isKerahat = prayerManager.isKerahat(data)
                val nextVakitInfo = prayerManager.getNextVakitInfo(data)

                if (isKerahat) {
                    views.setTextViewText(R.id.widget_dial_title, "Kerahat Vakti")
                    views.setTextColor(R.id.widget_dial_title, 0xFFFFD700.toInt())
                } else if (nextVakitInfo != null) {
                    val (nextPName, _) = nextVakitInfo
                    val isRamadan = data.date.hijri.month.en.contains("Ramadan", true)
                    val suffixTitle = when (nextPName) {
                        "İmsak" -> "İmsak'a"
                        "Güneş" -> "Güneş'e"
                        "Öğle" -> "Öğle'ye"
                        "İkindi" -> "İkindi'ye"
                        "Akşam" -> if (isRamadan) "İftar'a" else "Akşam'a"
                        "Yatsı" -> "Yatsı'ya"
                        else -> "$nextPName'e"
                    }
                    views.setTextViewText(R.id.widget_dial_title, suffixTitle)
                    views.setTextColor(R.id.widget_dial_title, 0xFFCCCCCC.toInt())
                }

                if (nextVakitInfo != null) {
                    val (_, nextVakitTime) = nextVakitInfo
                    val nowMillis = System.currentTimeMillis()
                    
                    var targetTime = nextVakitTime
                    if (targetTime <= nowMillis) {
                        targetTime += 86400000L // Vakit geçmişse 24 saat ekle (yarınki vakit)
                    }

                    val remainingMillis = (targetTime - nowMillis).coerceAtLeast(0)
                    val baseTime = SystemClock.elapsedRealtime() + remainingMillis

                    views.setChronometer(R.id.widget_dial_chronometer, baseTime, null, true)
                    views.setChronometerCountDown(R.id.widget_dial_chronometer, true)

                    // Vakit doldugu an widget'ı tam 00:00 anında otomatik yenile
                    try {
                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        val refreshIntent = Intent(context, DialWidgetProvider::class.java).apply {
                            action = "REFRESH_WIDGET"
                        }
                        val pendingIntent = PendingIntent.getBroadcast(
                            context,
                            998,
                            refreshIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        try {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetTime + 500L, pendingIntent)
                        } catch (_: SecurityException) {
                            alarmManager.set(AlarmManager.RTC_WAKEUP, targetTime + 500L, pendingIntent)
                        }
                    } catch (_: Exception) {}
                }
            }

            // Click Intent
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 998, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_dial_container, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun renderDialBitmap(context: Context, data: PrayerData, width: Int, height: Int): Bitmap? {
            return try {
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)

                val centerX = width / 2f
                val centerY = height.toFloat() // Baseline at bottom of top canvas!
                val arcOuterR = width * 0.38f // 304px
                val arcInnerR = width * 0.20f // 160px
                val domeR = width * 0.18f // 144px
                val topFrameR = arcOuterR + 32f // 336px (Topmost Y = 360 - 336 = 24px, zero clipping!)

                // Parse prayer times to minutes
                fun timeToMin(timeStr: String): Int {
                    return try {
                        val clean = timeStr.substringBefore(" ").trim()
                        val parts = clean.split(":")
                        parts[0].toInt() * 60 + parts[1].toInt()
                    } catch (_: Exception) { 0 }
                }

                val imsakM = timeToMin(data.timings.fajr)
                val sabahM = timeToMin(data.timings.sabah)
                val gunesM = timeToMin(data.timings.sunrise)
                val ogleM = timeToMin(data.timings.dhuhr)
                val ikindiM = timeToMin(data.timings.asr)
                val aksamM = timeToMin(data.timings.maghrib)
                val yatsiM = timeToMin(data.timings.isha)

                // Proportional Angle Converter for 05:00 -> 23:00 (300m -> 1380m) mapped from 180° to 0°
                fun minToAngle(mins: Int): Float {
                    val clamped = mins.coerceIn(300, 1380)
                    val frac = (clamped - 300f) / 1080f
                    return 180f - (frac * 180f)
                }

                // 1. Draw Solid Dark Non-Transparent Background ONLY Under Dial & Hours Scale
                val fullDialRect = RectF(centerX - (topFrameR + 6f), centerY - (topFrameR + 6f), centerX + (topFrameR + 6f), centerY + (topFrameR + 6f))
                val solidBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFF0A1D20.toInt() // Solid Dark Teal
                    style = Paint.Style.FILL
                }
                canvas.drawArc(fullDialRect, 180f, 180f, true, solidBgPaint)

                val rectOuter = RectF(centerX - arcOuterR, centerY - arcOuterR, centerX + arcOuterR, centerY + arcOuterR)

                // Paint Definitions
                val bgArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFF0D292D.toInt()
                    style = Paint.Style.FILL
                }
                val tealPaint1 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFF144D53.toInt()
                    style = Paint.Style.FILL
                }
                val tealPaint2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFF185A60.toInt()
                    style = Paint.Style.FILL
                }
                val kerahatPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFF8D021F.toInt() // Rich Crimson Burgundy Red
                    style = Paint.Style.FILL
                }

                // 2. Draw Base 180° Background Semi-Circle
                canvas.drawArc(rectOuter, 180f, 180f, true, bgArcPaint)

                // Helper to draw segment slice
                fun drawSlice(startM: Int, endM: Int, paint: Paint) {
                    if (endM <= startM) return
                    val startA = minToAngle(endM)
                    val sweepA = ((endM - startM).coerceAtLeast(0) / 1080f * 180f)
                    canvas.drawArc(rectOuter, startA, sweepA, true, paint)
                }

                // 3. Draw Prayer & Kerahat Segments
                drawSlice(imsakM, sabahM, tealPaint1)
                drawSlice(sabahM, gunesM, tealPaint2)
                drawSlice(gunesM, (gunesM + 45).coerceAtMost(1380), kerahatPaint) // Güneş Kerahat (Bordo)
                drawSlice(gunesM + 45, ogleM - 45, tealPaint1)
                drawSlice(ogleM - 45, ogleM, kerahatPaint) // Öğle Kerahat (Bordo)
                drawSlice(ogleM, ikindiM, tealPaint2)
                drawSlice(ikindiM, aksamM - 45, tealPaint1)
                drawSlice(aksamM - 45, aksamM, kerahatPaint) // Akşam Kerahat (Bordo)
                drawSlice(aksamM, yatsiM, tealPaint2)
                drawSlice(yatsiM, 1380, tealPaint1)

                // Cut out Inner Circle
                val rectInner = RectF(centerX - arcInnerR, centerY - arcInnerR, centerX + arcInnerR, centerY + arcInnerR)
                val erasePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFF0A1D20.toInt()
                    style = Paint.Style.FILL
                }
                canvas.drawArc(rectInner, 180f, 180f, true, erasePaint)

                // 4. Draw Entry & Exit Segment Divider Lines (No line after Yatsı)
                val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xAAFFFFFF.toInt()
                    strokeWidth = 2.5f
                    style = Paint.Style.STROKE
                }

                fun drawDivider(mins: Int) {
                    val rad = Math.toRadians(minToAngle(mins).toDouble())
                    val x1 = centerX + (arcInnerR * Math.cos(rad)).toFloat()
                    val y1 = centerY - (arcInnerR * Math.sin(rad)).toFloat()
                    val x2 = centerX + (arcOuterR * Math.cos(rad)).toFloat()
                    val y2 = centerY - (arcOuterR * Math.sin(rad)).toFloat()
                    canvas.drawLine(x1, y1, x2, y2, linePaint)
                }

                listOf(imsakM, sabahM, gunesM, gunesM + 45, ogleM - 45, ogleM, ikindiM, aksamM - 45, aksamM, yatsiM).forEach {
                    drawDivider(it)
                }

                // 5. Draw Prayer & Kerahat Labels Vertically Inside Slices
                val textLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFFFFFFF.toInt()
                    textSize = 17f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                val kerahatLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFFFD700.toInt()
                    textSize = 15f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }

                fun drawArcLabel(text: String, startM: Int, endM: Int, isKerahatText: Boolean = false) {
                    if (endM <= startM) return
                    val midMins = (startM + endM) / 2
                    val midAngle = minToAngle(midMins)
                    val rad = Math.toRadians(midAngle.toDouble())
                    val r = (arcInnerR + arcOuterR) / 2f
                    val x = centerX + (r * Math.cos(rad)).toFloat()
                    val y = centerY - (r * Math.sin(rad)).toFloat()

                    val paint = if (isKerahatText) kerahatLabelPaint else textLabelPaint
                    canvas.save()
                    canvas.rotate(-midAngle, x, y)
                    canvas.drawText(text, x, y + 4f, paint)
                    canvas.restore()
                }

                drawArcLabel("Sabah", imsakM, gunesM)
                drawArcLabel("Kerahat", gunesM, gunesM + 45, true)
                drawArcLabel("Kuşluk", gunesM + 45, ogleM - 45)
                drawArcLabel("Kerahat", ogleM - 45, ogleM, true)
                drawArcLabel("Öğle", ogleM, ikindiM)
                drawArcLabel("İkindi", ikindiM, aksamM - 45)
                drawArcLabel("Kerahat", aksamM - 45, aksamM, true)
                drawArcLabel("Akşam", aksamM, yatsiM)
                drawArcLabel("Yatsı", yatsiM, 1380)

                // 6. Draw Top Arc Frame Ring
                val rectTopFrame = RectF(centerX - topFrameR, centerY - topFrameR, centerX + topFrameR, centerY + topFrameR)
                val frameArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xAA00E5FF.toInt()
                    strokeWidth = 3f
                    style = Paint.Style.STROKE
                }
                canvas.drawArc(rectTopFrame, 180f, 180f, false, frameArcPaint)

                // 7. Draw Outer Hour Scale (05 to 23)
                val hourScalePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFE0E0E0.toInt()
                    textSize = 18f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xCCFFFFFF.toInt()
                    strokeWidth = 3.5f
                }

                val scaleHours = listOf(5, 7, 9, 11, 13, 15, 17, 19, 21, 23)
                for (h in scaleHours) {
                    val hMins = h * 60
                    val rad = Math.toRadians(minToAngle(hMins).toDouble())
                    val x1 = centerX + (arcOuterR * Math.cos(rad)).toFloat()
                    val y1 = centerY - (arcOuterR * Math.sin(rad)).toFloat()
                    val x2 = centerX + ((arcOuterR + 10f) * Math.cos(rad)).toFloat()
                    val y2 = centerY - ((arcOuterR + 10f) * Math.sin(rad)).toFloat()
                    canvas.drawLine(x1, y1, x2, y2, tickPaint)

                    val xText = centerX + ((arcOuterR + 24f) * Math.cos(rad)).toFloat()
                    val yText = centerY - ((arcOuterR + 24f) * Math.sin(rad)).toFloat() + 5f
                    val hStr = String.format(Locale.getDefault(), "%02d", h)
                    canvas.drawText(hStr, xText, yText, hourScalePaint)
                }

                // 8. Check Current Time & Kerahat State
                val prayerManager = PrayerManager(context)
                val isKerahat = prayerManager.isKerahat(data)

                val nowCal = Calendar.getInstance()
                val nowMins = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)

                // 9. Draw Current Time Needle
                val nowAngleRad = Math.toRadians(minToAngle(nowMins).toDouble())
                val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0xFFFF1744.toInt() // Bright Red Needle
                    strokeWidth = 6f
                    strokeCap = Paint.Cap.ROUND
                }
                val needleX = centerX + ((arcOuterR + 12f) * Math.cos(nowAngleRad)).toFloat()
                val needleY = centerY - ((arcOuterR + 12f) * Math.sin(nowAngleRad)).toFloat()
                val needleStartX = centerX + ((domeR + 4f) * Math.cos(nowAngleRad)).toFloat()
                val needleStartY = centerY - ((domeR + 4f) * Math.sin(nowAngleRad)).toFloat()
                canvas.drawLine(needleStartX, needleStartY, needleX, needleY, needlePaint)

                // 10. Draw Center Inner Dome with Multi-layer Radiant Glowing Neon Arc
                val domeRect = RectF(centerX - domeR, centerY - domeR, centerX + domeR, centerY + domeR)
                val domePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (isKerahat) 0xFF8D021F.toInt() else 0xFF0D2529.toInt()
                    style = Paint.Style.FILL
                }
                canvas.drawArc(domeRect, 180f, 180f, true, domePaint)

                // Fill small bridge gap right under dome hub to connect to bottom bar in matching dome color!
                val gapRect = RectF(centerX - domeR, centerY - 2f, centerX + domeR, centerY + 8f)
                canvas.drawRect(gapRect, domePaint)

                val glowBorder1 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (isKerahat) 0x55FF1744.toInt() else 0x3300E5FF.toInt()
                    strokeWidth = 12f
                    style = Paint.Style.STROKE
                }
                val glowBorder2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (isKerahat) 0xAAFF1744.toInt() else 0x7700E5FF.toInt()
                    strokeWidth = 7f
                    style = Paint.Style.STROKE
                }
                val glowBorder3 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = if (isKerahat) 0xFFFF1744.toInt() else 0xFF00E5FF.toInt()
                    strokeWidth = 3.5f
                    style = Paint.Style.STROKE
                }
                canvas.drawArc(domeRect, 180f, 180f, false, glowBorder1)
                canvas.drawArc(domeRect, 180f, 180f, false, glowBorder2)
                canvas.drawArc(domeRect, 180f, 180f, false, glowBorder3)

                bitmap
            } catch (e: Exception) {
                Log.e("DialWidget", "Render error: ${e.message}", e)
                null
            }
        }
    }
}
