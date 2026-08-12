package ru.cnamdiagnostic

import android.app.Activity
import android.app.role.RoleManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.*

class MainActivity : Activity() {
    private lateinit var logView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(28, 28, 28, 28)

        root.addView(TextView(this).apply {
            text = "CNAM Диагностика"
            textSize = 23f
        })
        root.addView(TextView(this).apply {
            text = "Тестирует данные, которые Android Telecom передаёт приложению при звонке. Ничего не блокирует."
            textSize = 15f
        })
        root.addView(Button(this).apply {
            text = "1. Назначить фильтром звонков"
            setOnClickListener { requestScreeningRole() }
        })
        root.addView(Button(this).apply {
            text = "2. Обновить журнал"
            setOnClickListener { refreshLog() }
        })
        root.addView(Button(this).apply {
            text = "Скопировать журнал"
            setOnClickListener {
                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("CNAM log", getLog()))
                Toast.makeText(this@MainActivity, "Скопировано", Toast.LENGTH_SHORT).show()
            }
        })
        root.addView(Button(this).apply {
            text = "Очистить журнал"
            setOnClickListener {
                getSharedPreferences("log", 0).edit().clear().apply()
                refreshLog()
            }
        })
        logView = TextView(this)
        logView.textSize = 13f
        logView.setTextIsSelectable(true)
        root.addView(logView)
        setContentView(root)
        refreshLog()
    }

    private fun requestScreeningRole() {
        val rm = getSystemService(RoleManager::class.java)
        if (rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
            startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING), 100)
        } else {
            Toast.makeText(this, "ROLE_CALL_SCREENING недоступна", Toast.LENGTH_LONG).show()
        }
    }

    private fun getLog(): String =
        getSharedPreferences("log", 0).getString("text", "Звонков пока нет.") ?: "Звонков пока нет."

    private fun refreshLog() { logView.text = getLog() }
    override fun onResume() { super.onResume(); refreshLog() }
}
