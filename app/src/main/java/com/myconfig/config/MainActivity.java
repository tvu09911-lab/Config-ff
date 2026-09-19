package com.myconfig.config;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;
import rikka.shizuku.Shizuku;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

public class MainActivity extends Activity {

    private static final int REQUEST_CODE = 1001;
    private TextView resultText;

    private static final String OPTIMIZE_ON =
            "settings put global window_animation_scale 0.5; " +
            "settings put global transition_animation_scale 0.5; " +
            "settings put global animator_duration_scale 0.5; " +
            "settings put global cached_apps_freezer enabled; " +
            "settings put global app_standby_enabled 1; " +
            "settings put global settings_enable_monitor_phantom_procs true; " +
            "settings put system pointer_speed 6; " +
            "settings put system pointer_acceleration 0; " +
            "settings put system touch_slop 2; " +
            "settings put system pointer_gesture_duration 30; " +
            "settings put system touch_sensitivity 1.1";

    private static final String OPTIMIZE_OFF =
            "settings put global window_animation_scale 1; " +
            "settings put global transition_animation_scale 1; " +
            "settings put global animator_duration_scale 1; " +
            "settings put global cached_apps_freezer disabled; " +
            "settings put global app_standby_enabled 0; " +
            "settings put system pointer_speed 0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        resultText = findViewById(R.id.resultText);
        Switch optimizeSwitch = findViewById(R.id.optimizeSwitch);

        Shizuku.addRequestPermissionResultListener((requestCode, grantResult) -> {
            if (grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                resultText.setText("Đã được cấp quyền! Bật lại switch để chạy lệnh.");
            } else {
                resultText.setText("Bị từ chối quyền.");
            }
        });

        optimizeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                runCommand(OPTIMIZE_ON, "Đã bật tối ưu hiệu năng.");
            } else {
                runCommand(OPTIMIZE_OFF, "Đã tắt, trả về mặc định.");
            }
        });
    }

    private Process newShizukuProcess(String[] cmd) throws Exception {
        Method method = Shizuku.class.getDeclaredMethod(
                "newProcess", String[].class, String[].class, String.class);
        method.setAccessible(true);
        return (Process) method.invoke(null, cmd, null, null);
    }

    private void runCommand(String command, String successLabel) {
        if (!Shizuku.pingBinder()) {
            resultText.setText("Shizuku chưa chạy. Mở app Shizuku trước.");
            return;
        }
        if (Shizuku.checkSelfPermission() != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Shizuku.requestPermission(REQUEST_CODE);
            return;
        }
        try {
            Process process = newShizukuProcess(new String[]{"sh", "-c", command});
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
            resultText.setText(successLabel + "\n\n" + output.toString());
        } catch (Exception e) {
            resultText.setText("Lỗi: " + e.getMessage());
        }
    }
}
