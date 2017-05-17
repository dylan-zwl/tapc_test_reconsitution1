package com.tapc.test.activtiy;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.tapc.test.R;
import com.tapc.test.widget.TftColorView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TftTestActivity extends Activity {
    @BindView(R.id.tftcolor_view)
     TftColorView mTftColorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tft);
        ButterKnife.bind(this);
        mTftColorView.init(this);
    }

    @OnClick(R.id.tftcolor_view)
    protected void viewOnClick(View v) {
        if (mTftColorView.getIndex() >= TftColorView.CHECK_ITEM) {
            this.setResult(1);
            this.finish();
        }
        mTftColorView.nextTestColor();
    }
}
