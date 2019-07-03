package ru.xkip.noad.twitter;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String pInfo = "";
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        Resources res = getResources();
        String status = res.getString(R.string.app_name2)
                + " v"
                + pInfo
                + " "
                + (XChecker.isEnabled() ? res.getString(R.string.module_active) : res
                .getString(R.string.module_inactive));
        TextView tvStatus = ((TextView) findViewById(R.id.moduleStatus));
        tvStatus.setText(status);
        tvStatus.setTextColor((XChecker.isEnabled() ? Color.GREEN : Color.RED));

        Log.i("NOAD", "setOnClickListener2");

        findViewById(R.id.btnOK).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

}
