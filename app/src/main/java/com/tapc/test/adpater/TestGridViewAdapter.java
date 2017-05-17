package com.tapc.test.adpater;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tapc.test.R;
import com.tapc.test.entity.TestItemType;
import com.tapc.test.entity.TestResult;
import com.tapc.test.model.base.ITest;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TestGridViewAdapter extends BaseAdapter {

    private List<ITest> mListAppInfo = null;
    private Context mContext;

    public TestGridViewAdapter(Context context, List<ITest> mListAppInfo) {
        this.mListAppInfo = mListAppInfo;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mListAppInfo.size();
    }

    @Override
    public Object getItem(int position) {
        return mListAppInfo.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setListItem(List<ITest> list) {
        mListAppInfo = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.test_grid_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ITest item = (ITest) getItem(position);
        TestItemType title = item.getTestItemType();
        if (title != null) {
            viewHolder.testItemName.setText(title.getTitle());
        }

        TestResult result = item.getTestResult();
        int id = 0;
        switch (result) {
            case NOT_TEST:
                id = R.drawable.not_test;
                break;
            case SUCCESS:
                id = R.drawable.success;
                break;
            case FAIL:
                id = R.drawable.fault;
                break;
        }
        Glide.with(mContext).load(id).diskCacheStrategy(DiskCacheStrategy.RESULT).into(viewHolder.testItemSatus);

        return convertView;
    }

    class ViewHolder {
        @BindView(R.id.test_item_icon)
        ImageView testItemIcon;
        @BindView(R.id.test_item_name)
        TextView testItemName;
        @BindView(R.id.test_item_status)
        ImageView testItemSatus;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
