package com.ushastoe.changeSim

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.telephony.SubscriptionManager
import android.util.Log
import com.ushastoe.changeSim.R
import java.io.BufferedReader
import java.io.InputStreamReader


class MyTileService: TileService() {


    private fun clickOnTile() {
        inactive()
        switchSim()
        setTile()
        active()
    }

    @SuppressLint("MissingPermission")
    private fun getNameSim(sim: Int): String {
        val subscriptionManager = this.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        return subscriptionManager.getActiveSubscriptionInfo(sim).carrierName.toString()
    }

    override fun onClick() {
        super.onClick()
        clickOnTile()
    }

    private fun setTile() {
        val label = getCurrentSimName()
        val icons: Icon
        if (label == "Beeline") {
            icons = Icon.createWithResource(this, R.drawable.b)
        } else if (label == "MegaFon") {
            icons = Icon.createWithResource(this, R.drawable.m)
        } else {
            icons = Icon.createWithResource(this, R.drawable.ic_launcher_foreground)
        }
        qsTile.let { tile ->
            tile.label = label
            tile.icon = icons
            tile.updateTile()
        }
    }

    private fun logs(value: Any?) {
        if (value is String) {
            Log.d("level", value)
        } else {
            Log.d("level", value.toString())
        }
    }

    private fun switchSim() {
        logs("switchsim1")
        val result = getCurrentSim()
        if (result == 1) {
            runCommand("settings put global multi_sim_data_call 2")
        } else {
            runCommand("settings put global multi_sim_data_call 1")
        }
        Thread.sleep(100)
        runCommand("svc data disable")
        Thread.sleep(100)
        runCommand("svc data enable")
    }

    private fun getCurrentSim(): Int {
        val result = executeSuCommand("settings get global multi_sim_data_call")
        return result.toInt()
    }

    private fun getCurrentSimName(): String {
        val result = executeSuCommand("settings get global multi_sim_data_call").toInt()
        return getNameSim(result)
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
    private fun active() {
        qsTile?.let { tile ->
            tile.state = Tile.STATE_ACTIVE
            tile.updateTile()
        }
    }
    private fun inactive() {
        qsTile?.let { tile ->
            tile.state = Tile.STATE_INACTIVE
            tile.updateTile()
        }
    }
    private fun runCommand(cmd: String?) {
        Runtime.getRuntime().exec("su -c $cmd")
    }

}
