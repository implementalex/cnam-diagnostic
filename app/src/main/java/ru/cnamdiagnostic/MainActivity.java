package ru.cnamdiagnostic;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private TextView logView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(28, 28, 28, 28);

        TextView title = new TextView(this);
        title.setText("CNAM Диагностика");
        title.setTextSize(23f);
        root.addView(title);

        TextView description = new TextView(this);
        description.setText(
                "Тестирует данные, которые Android Telecom передаёт " +
                "приложению при звонке. Ничего не блокирует."
        );
        description.setTextSize(15f);
        root.addView(description);

        Button roleButton = new Button(this);
        roleButton.setText("1. Назначить фильтром звонков");
        roleButton.setOnClickListener(v -> requestScreeningRole());
        root.addView(roleButton);

        Button refreshButton = new Button(this);
        refreshButton.setText("2. Обновить журнал");
        refreshButton.setOnClickListener(v -> refreshLog());
        root.addView(refreshButton);

        Button copyButton = new Button(this);
        copyButton.setText("Скопировать журнал");
        copyButton.setOnClickListener(v -> {
            ClipboardManager cm =
                    (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

            if (cm != null) {
                cm.setPrimaryClip(
                        ClipData.newPlainText("CNAM log", getLog())
                );

                Toast.makeText(
                        MainActivity.this,
                        "Скопировано",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
        root.addView(copyButton);

        Button clearButton = new Button(this);
        clearButton.setText("Очистить журнал");
        clearButton.setOnClickListener(v -> {
            getSharedPreferences("log", MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();

            refreshLog();
        });
        root.addView(clearButton);

        logView = new TextView(this);
        logView.setTextSize(13f);
        logView.setTextIsSelectable(true);
        root.addView(logView);

        setContentView(root);

        refreshLog();
    }

    private void requestScreeningRole() {
        RoleManager roleManager = getSystemService(RoleManager.class);

        if (roleManager != null &&
                roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {

            startActivityForResult(
                    roleManager.createRequestRoleIntent(
                            RoleManager.ROLE_CALL_SCREENING
                    ),
                    100
            );

        } else {
            Toast.makeText(
                    this,
                    "ROLE_CALL_SCREENING недоступна",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private String getLog() {
        return getSharedPreferences("log", MODE_PRIVATE)
                .getString("text", "Звонков пока нет.");
    }

    private void refreshLog() {
        if (logView != null) {
            logView.setText(getLog());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshLog();
    }
}
