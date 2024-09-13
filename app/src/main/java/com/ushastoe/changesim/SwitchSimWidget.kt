package com.ushastoe.changesim

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.telephony.SubscriptionManager
import android.view.View
import android.widget.RemoteViews
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Implementation of App Widget functionality.
 */
class SwitchSimWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    println("init")
    val intent = Intent(context, WidgetUpdateReceiver::class.java).apply {
        action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
    }

    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    switchSimS()

    val widgetText = getCurrentSimName(context)
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.switch_sim_widget)
    views.setTextViewText(R.id.appwidget_text, widgetText)
    views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent)

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

private fun getCurrentSim(): Int {
    val result = executeSuCommand("settings get global multi_sim_data_call")
    return result.toInt()
}

private fun getCurrentSimName(context: Context): String {
    val result = executeSuCommand("settings get global multi_sim_data_call").toInt()
    return getNameSim(result, context)
}

@SuppressLint("MissingPermission")
private fun getNameSim(sim: Int, context: Context): String {
    val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
    return subscriptionManager.getActiveSubscriptionInfo(sim).carrierName.toString()
}

private fun switchSim(): View.OnClickListener {
    return View.OnClickListener {
        val result = getCurrentSim()
        print(result)
        if (result == 2) {
            runCommand("settings put global multi_sim_data_call 3")
        } else {
            runCommand("settings put global multi_sim_data_call 2")
        }
        Thread.sleep(100)
        runCommand("svc data disable")
        Thread.sleep(100)
        runCommand("svc data enable")
    }
}

private fun runCommand(cmd: String?) {
    Runtime.getRuntime().exec("su -c $cmd")
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

private fun switchSimS() {
    println("switchSimS")
    val result = getCurrentSim()
    if (result == 2) {
        runCommand("settings put global multi_sim_data_call 3")
    } else {
        runCommand("settings put global multi_sim_data_call 2")
    }
    Thread.sleep(100)
    runCommand("svc data disable")
    Thread.sleep(100)
    runCommand("svc data enable")
}