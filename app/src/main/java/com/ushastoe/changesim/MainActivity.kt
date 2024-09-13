package com.ushastoe.changesim

import android.Manifest
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.button).setOnClickListener(switchSim())
        checkPermission(this)
        findViewById<Button>(R.id.buttoncr).setOnClickListener(requestWidget(this))

    }


    private fun getCurrentSim(): Int {
        val result = executeSuCommand("settings get global multi_sim_data_call")
        return result.toInt()
    }

    private fun switchSim(): View.OnClickListener {
        return View.OnClickListener {
            val result = getCurrentSim()
            if (result == 1) {
                runCommand("settings put global multi_sim_data_call 3")
            } else {
                runCommand("settings put global multi_sim_data_call 2")
            }
            runCommand("svc data disable")
            runCommand("svc data enable")
        }
    }

    private fun executeSuCommand(command: String): String {
        val processBuilder = ProcessBuilder("su", "-c", command)
        processBuilder.redirectErrorStream(true)

        val process = processBuilder.start()
        val result = StringBuilder()

        BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            var line: String? = reader.readLine()
            while (line != null) {
                result.append(line).append("\n")
                line = reader.readLine()
            }
        }
        process.waitFor()
        return result.toString().trimEnd()
    }


    private fun runCommand(cmd: String?) {
        Runtime.getRuntime().exec("su -c $cmd")
    }

    private fun checkPermission(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_NUMBERS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            logs(123)
            runCommand("pm grant ${context.packageName} android.permission.READ_PHONE_NUMBERS")
        }
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            logs(1231)
            logs(context.packageName)
            runCommand("pm grant ${context.packageName} android.permission.READ_PHONE_STATE")
        }
    }

    private fun logs(value: Any?) {
        if (value is String) {
            Log.d("level", value)
        } else {
            Log.d("level", value.toString())
        }
    }

    private fun requestWidget(context: Context): View.OnClickListener? {
        return View.OnClickListener {
            val appWidgetManager = AppWidgetManager.getInstance(context)

            val myProvider = ComponentName(context, WidgetUpdateReceiver::class.java)


            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                val successCallback = PendingIntent.getBroadcast(
                    context,
                    0,
                    Intent(),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                appWidgetManager.requestPinAppWidget(myProvider, null, successCallback)
            }
        }
    }
}