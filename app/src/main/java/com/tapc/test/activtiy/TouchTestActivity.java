package com.tapc.test.activtiy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import com.tapc.test.R;
import com.tapc.test.entity.BoardType;
import com.tapc.test.entity.Config;
import com.tapc.test.widget.TouchView;
import com.tapc.test.widget.TouchView2;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TouchTestActivity extends Activity {
    @BindView(R.id.touch_test_finish)
    Button touchTestFinish;
    @BindView(R.id.touch_view)
    TouchView touView;
    @BindView(R.id.touch_view2)
    TouchView2 touView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_touch);
        ButterKnife.bind(this);
        Settings.System.putInt(getContentResolver(), "show_touches", 1);
        Settings.System.putInt(getContentResolver(), "pointer_location", 1);

        if (Config.BOARD_TYPE == BoardType.G022 || Config.BOARD_TYPE == BoardType.G012) {
//            touView.setVisibility(View.GONE);
//            touView2.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.touch_test_finish)
    protected void testFinish(View v) {
        Settings.System.putInt(getContentResolver(), "show_touches", 0);
        Settings.System.putInt(getContentResolver(), "pointer_location", 0);
        Intent intent = new Intent();
        this.setResult(1, intent);
        this.finish();
    }
}
