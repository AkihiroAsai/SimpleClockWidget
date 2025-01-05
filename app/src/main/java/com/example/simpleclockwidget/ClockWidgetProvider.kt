package com.example.simpleclockwidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.*

class ClockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId, null)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateWidget(context, appWidgetManager, appWidgetId, newOptions)
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, options: Bundle?) {
        val views = RemoteViews(context.packageName, R.layout.widget_clock)

        val prefs = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)
        val backgroundColor = prefs.getString("background_color", "#FFFFFF") ?: "#FFFFFF"
        val fontColor = prefs.getString("font_color", "#000000") ?: "#000000"

        views.setInt(R.id.widget_layout, "setBackgroundColor", android.graphics.Color.parseColor(backgroundColor))
        views.setTextColor(R.id.clock_time_text, android.graphics.Color.parseColor(fontColor))
        views.setTextColor(R.id.date_text, android.graphics.Color.parseColor(fontColor))

        // 日時を取得して表示
        val currentDate = SimpleDateFormat("M/d E", Locale.getDefault()).format(Date())
        views.setTextViewText(R.id.date_text, currentDate)
        // 現在時刻を取得
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        views.setTextViewText(R.id.clock_time_text, currentTime)

        // オプションからウィジェットサイズを取得
        val minWidth = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 0
        val minHeight = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) ?: 0

        // タップ時にアプリを開くインテントを設定
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.date_text, pendingIntent)
        views.setOnClickPendingIntent(R.id.clock_time_text, pendingIntent)

        updateTextSize(views,minWidth,minHeight)

        // 日時を保存
        val sharedPreferences = context.getSharedPreferences("ClockWidgetPrefs", Context.MODE_PRIVATE)
        // 日時と時刻を保存
        with(sharedPreferences.edit()) {
            putString("date_$appWidgetId", currentDate)
            putString("time_$appWidgetId", currentTime)
            apply()
        }

        // ウィジェットを更新
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun updateTextSize(views: RemoteViews, minWidth: Int, minHeight: Int) {
        if((minWidth > 0) && (minHeight > 0)) {
            // サイズに応じてフォントサイズを変更
            val dateFontSize = when {
                minWidth > 280 && minHeight > 120 -> 64f
                minWidth > 200 && minHeight > 100 -> 48f
                minWidth > 180 && minHeight > 80 -> 32f
                else -> 16f
            }
            val fontSize = when {
                minWidth > 280 && minHeight > 120 -> 104f
                minWidth > 200 && minHeight > 100 -> 80f
                minWidth > 180 && minHeight > 80 -> 64f
                else -> 48f
            }
            views.setTextViewTextSize(R.id.date_text, TypedValue.COMPLEX_UNIT_SP, dateFontSize)
            views.setTextViewTextSize(R.id.clock_time_text, TypedValue.COMPLEX_UNIT_SP, fontSize)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        // ウィジェットが更新された場合の処理
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS) ?: return

            for (appWidgetId in appWidgetIds) {
                val sharedPreferences = context.getSharedPreferences("ClockWidgetPrefs", Context.MODE_PRIVATE)

                // 保存された日時と時刻を取得
                val savedDate = sharedPreferences.getString("date_$appWidgetId", "N/A")
                val savedTime = sharedPreferences.getString("time_$appWidgetId", "00:00")

                // 保存されたカラー設定を取得
                val prefs = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)
                val backgroundColor = prefs.getString("background_color", "#FFFFFF") ?: "#FFFFFF"
                val fontColor = prefs.getString("font_color", "#000000") ?: "#000000"

                // 保存された値をウィジェットに設定
                val views = RemoteViews(context.packageName, R.layout.widget_clock)
                views.setTextViewText(R.id.date_text, savedDate)
                views.setTextViewText(R.id.clock_time_text, savedTime)

                views.setInt(R.id.widget_layout, "setBackgroundColor", android.graphics.Color.parseColor(backgroundColor))
                views.setTextColor(R.id.clock_time_text, android.graphics.Color.parseColor(fontColor))
                views.setTextColor(R.id.date_text, android.graphics.Color.parseColor(fontColor))

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
