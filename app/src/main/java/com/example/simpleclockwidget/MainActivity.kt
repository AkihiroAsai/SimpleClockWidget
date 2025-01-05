package com.example.simpleclockwidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var preTextView: TextView
    private lateinit var aftTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var selBackTextView: TextView
    private lateinit var selFontTextView: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("M/d (E)", Locale.getDefault())

    private lateinit var backgroundColorSpinner: Spinner
    private lateinit var fontColorSpinner: Spinner

    // カラーオプション
    private val colors = mapOf(
        "白" to "#FFFFFF",
        "黒" to "#000000",
        "青" to "#0000FF",
        "赤" to "#FF0000",
        "緑" to "#00FF00"
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UIの参照を取得
        backgroundColorSpinner = findViewById(R.id.background_color_spinner)
        fontColorSpinner = findViewById(R.id.font_color_spinner)
        dateTextView = findViewById(R.id.date_text)
        timeTextView = findViewById(R.id.time_text)
        preTextView = findViewById(R.id.pre_text)
        aftTextView = findViewById(R.id.aft_text)
        selBackTextView = findViewById(R.id.sel_back_text)
        selFontTextView = findViewById(R.id.sel_font_text)

        // スピナーの設定
        val colorNames = colors.keys.toList()
//        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, colorNames)
        val adapter = ArrayAdapter(this, R.layout.spinner_item, colorNames)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        backgroundColorSpinner.adapter = adapter
        fontColorSpinner.adapter = adapter

        // イベントリスナーを設定
        backgroundColorSpinner.onItemSelectedListener = createColorChangeListener { color ->
            dateTextView.setBackgroundColor(android.graphics.Color.parseColor(color))
            timeTextView.setBackgroundColor(android.graphics.Color.parseColor(color))
            preTextView.setBackgroundColor(android.graphics.Color.parseColor(color))
            aftTextView.setBackgroundColor(android.graphics.Color.parseColor(color))
            saveSetting("background_color", color)
        }

        fontColorSpinner.onItemSelectedListener = createColorChangeListener { color ->
            dateTextView.setTextColor(android.graphics.Color.parseColor(color))
            timeTextView.setTextColor(android.graphics.Color.parseColor(color))
            preTextView.setTextColor(android.graphics.Color.parseColor(color))
            aftTextView.setTextColor(android.graphics.Color.parseColor(color))
            saveSetting("font_color", color)
        }

        // 保存された設定を反映
        applySavedSettings()
        updateWidget()

        // TextView の参照を取得
        dateTextView = findViewById(R.id.date_text)
        timeTextView = findViewById(R.id.time_text)
        preTextView = findViewById(R.id.pre_text)
        aftTextView = findViewById(R.id.aft_text)

        // 日時の更新を開始
        startClock()

    }

    private fun startClock() {
        val updateTask = object : Runnable {
            override fun run() {
                val now = Date()
                // 日付と時刻を更新
                dateTextView.text = dateFormat.format(now)
                timeTextView.text = timeFormat.format(now)
                // 1秒後に再度実行
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(updateTask)
    }

    // 設定を保存
    private fun saveSetting(key: String, value: String) {
        val prefs = getSharedPreferences("widget_settings", Context.MODE_PRIVATE)
        prefs.edit().putString(key, value).apply()
        updateWidget()
    }

    // 保存された設定を取得して反映
    private fun applySavedSettings() {
        val prefs = getSharedPreferences("widget_settings", Context.MODE_PRIVATE)
        val backgroundColor = prefs.getString("background_color", "#FFFFFF") ?: "#FFFFFF"
        val fontColor = prefs.getString("font_color", "#000000") ?: "#000000"

        dateTextView.setBackgroundColor(android.graphics.Color.parseColor(backgroundColor))
        dateTextView.setTextColor(android.graphics.Color.parseColor(fontColor))
        timeTextView.setBackgroundColor(android.graphics.Color.parseColor(backgroundColor))
        timeTextView.setTextColor(android.graphics.Color.parseColor(fontColor))

        // スピナーの選択状態を設定
        backgroundColorSpinner.setSelection(colors.values.indexOf(backgroundColor))
        fontColorSpinner.setSelection(colors.values.indexOf(fontColor))
    }

    // ウィジェットを更新
    private fun updateWidget() {
        val intent = Intent(this, ClockWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        // すべてのウィジェット ID を指定
        val ids = AppWidgetManager.getInstance(this).getAppWidgetIds(
            ComponentName(this, ClockWidgetProvider::class.java)
        )
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }

    // 色変更リスナー
    private fun createColorChangeListener(onColorSelected: (String) -> Unit): AdapterView.OnItemSelectedListener {
        return object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedColor = colors.values.toList()[position]
                onColorSelected(selectedColor)
                updateWidget()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Handler のループを停止
        handler.removeCallbacksAndMessages(null)
    }
}
