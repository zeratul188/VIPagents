package com.vip.raid;

import android.app.Activity;
import android.widget.Toast;

public class BackPressedForFinish {
    private final long TIME_INTERVAL = 2000;
    private long backKeyPressedTime = 0;

    private Toast toast;
    private Activity activity;

    public BackPressedForFinish(Activity activity) {
        this.activity = activity;
    }

    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + TIME_INTERVAL) {
            backKeyPressedTime = System.currentTimeMillis();
            showMessage("'뒤로' 버튼을 한번 더 누르시면 종료됩니다.");
        } else {
            toast.cancel();
            activity.finish();
        }
    }

    private void showMessage(String message) {
        toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
        toast.show();
    }
}
