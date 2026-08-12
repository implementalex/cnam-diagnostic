package ru.cnamdiagnostic

import android.os.Bundle
import android.telecom.Call
import android.telecom.CallScreeningService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CnamScreeningService : CallScreeningService() {
    override fun onScreenCall(details: Call.Details) {
        val direction = when (details.callDirection) {
            Call.Details.DIRECTION_INCOMING -> "INCOMING"
            Call.Details.DIRECTION_OUTGOING -> "OUTGOING"
            else -> "UNKNOWN"
        }
        val number = details.handle?.schemeSpecificPart ?: "<null>"
        val callerName = details.callerDisplayName ?: "<null>"
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())

        val record = "[" + now + "]\n" +
            "direction=" + direction + "\n" +
            "NUMBER=" + number + "\n" +
            "CALLER_DISPLAY_NAME=" + callerName + "\n" +
            "CALLER_DISPLAY_NAME_PRESENTATION=" + details.callerDisplayNamePresentation + "\n" +
            "HANDLE_PRESENTATION=" + details.handlePresentation + "\n" +
            "NUMBER_VERIFICATION_STATUS=" + details.callerNumberVerificationStatus + "\n" +
            "PROPERTIES=" + Call.Details.propertiesToString(details.callProperties) + "\n" +
            "CAPABILITIES=" + Call.Details.capabilitiesToString(details.callCapabilities) + "\n" +
            "EXTRAS=" + bundleToString(details.extras) + "\n" +
            "INTENT_EXTRAS=" + bundleToString(details.intentExtras) + "\n" +
            "------------------------------\n"

        val prefs = getSharedPreferences("log", 0)
        val old = prefs.getString("text", "") ?: ""
        prefs.edit().putString("text", (record + old).take(30000)).apply()

        respondToCall(details, CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .setSilenceCall(false)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build())
    }

    private fun bundleToString(bundle: Bundle?): String {
        if (bundle == null) return "<null>"
        if (bundle.isEmpty) return "{}"
        return try {
            bundle.keySet().sorted().joinToString(prefix = "{", postfix = "}") { key ->
                "$key=${bundle.get(key)?.toString() ?: "<null>"}"
            }
        } catch (e: Exception) {
            "<error:${e.javaClass.simpleName}>"
        }
    }
}
