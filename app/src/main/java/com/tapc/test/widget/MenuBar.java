package com.tapc.test.widget;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.tapc.test.R;
import com.tapc.test.entity.BoardType;
import com.tapc.test.entity.Config;
import com.tapc.test.model.KeyEventModel;
import com.tapc.test.service.MenuService;
import com.tapc.test.utils.IntentUtil;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MenuBar extends RelativeLayout {
    private Context mContext;

    public MenuBar(Context context) {
        super(context);
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.menubar, this, true);
        ButterKnife.bind(this);
    }


    @OnClick(R.id.back_btn)
    public void backOnClick(View v) {
        if (Config.IS_8935_PLATFORM) {
            if (Config.BOARD_TYPE == BoardType.G022 || Config.BOARD_TYPE == BoardType.G012) {
                IntentUtil.sendBroadcast(mContext, "reload", null);
                return;
            }
        }
        KeyEventModel.getInstance().getKeyboardEvent().backEvent();
    }

    @OnClick(R.id.home_btn)
    public void homeOnClick(View v) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);// 注意
        intent.addCategory(Intent.CATEGORY_HOME);
        mContext.startActivity(intent);
    }

    @OnClick(R.id.close_btn)
    public void closeOnClick(View v) {
        IntentUtil.stopService(mContext, MenuService.class);
    }
}
