package ru.cnamdiagnostic;

import android.os.Bundle;
import android.telecom.Call;
import android.telecom.CallScreeningService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CnamScreeningService extends CallScreeningService {

    @Override
    public void onScreenCall(Call.Details details) {

        String direction;

        switch (details.getCallDirection()) {
            case Call.Details.DIRECTION_INCOMING:
                direction = "INCOMING";
                break;

            case Call.Details.DIRECTION_OUTGOING:
                direction = "OUTGOING";
                break;

            default:
                direction = "UNKNOWN";
                break;
        }

        String number = "<null>";

        if (details.getHandle() != null) {
            String value = details.getHandle().getSchemeSpecificPart();

            if (value != null) {
                number = value;
            }
        }

        String callerName = details.getCallerDisplayName();

        if (callerName == null) {
            callerName = "<null>";
        }

        String now = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.US
        ).format(new Date());

        StringBuilder record = new StringBuilder();

        record.append("[")
                .append(now)
                .append("]\n");

        record.append("direction=")
                .append(direction)
                .append("\n");

        record.append("NUMBER=")
                .append(number)
                .append("\n");

        record.append("CALLER_DISPLAY_NAME=")
                .append(callerName)
                .append("\n");

        record.append("CALLER_DISPLAY_NAME_PRESENTATION=")
                .append(details.getCallerDisplayNamePresentation())
                .append("\n");

        record.append("HANDLE_PRESENTATION=")
                .append(details.getHandlePresentation())
                .append("\n");

        record.append("NUMBER_VERIFICATION_STATUS=")
                .append(details.getCallerNumberVerificationStatus())
                .append("\n");

        record.append("PROPERTIES=")
                .append(Call.Details.propertiesToString(
                        details.getCallProperties()
                ))
                .append("\n");

        record.append("CAPABILITIES=")
                .append(Call.Details.capabilitiesToString(
                        details.getCallCapabilities()
                ))
                .append("\n");

        record.append("EXTRAS=")
                .append(bundleToString(details.getExtras()))
                .append("\n");

        record.append("INTENT_EXTRAS=")
                .append(bundleToString(details.getIntentExtras()))
                .append("\n");

        record.append("------------------------------\n");

        String recordText = record.toString();

        android.content.SharedPreferences prefs =
                getSharedPreferences("log", MODE_PRIVATE);

        String old = prefs.getString("text", "");

        if (old == null) {
            old = "";
        }

        String combined = recordText + old;

        if (combined.length() > 30000) {
            combined = combined.substring(0, 30000);
        }

        prefs.edit()
                .putString("text", combined)
                .apply();

        respondToCall(
                details,
                new CallResponse.Builder()
                        .setDisallowCall(false)
                        .setRejectCall(false)
                        .setSilenceCall(false)
                        .setSkipCallLog(false)
                        .setSkipNotification(false)
                        .build()
        );
    }

    private String bundleToString(Bundle bundle) {

        if (bundle == null) {
            return "<null>";
        }

        if (bundle.isEmpty()) {
            return "{}";
        }

        try {
            List<String> keys =
                    new ArrayList<>(bundle.keySet());

            Collections.sort(keys);

            StringBuilder result = new StringBuilder("{");

            for (int i = 0; i < keys.size(); i++) {

                if (i > 0) {
                    result.append(", ");
                }

                String key = keys.get(i);
                Object value = bundle.get(key);

                result.append(key)
                        .append("=")
                        .append(value != null ? value : "<null>");
            }

            result.append("}");

            return result.toString();

        } catch (Exception e) {
            return "<error:" +
                    e.getClass().getSimpleName() +
                    ">";
        }
    }
}
