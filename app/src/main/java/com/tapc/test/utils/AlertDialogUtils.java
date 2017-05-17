package com.tapc.test.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by Administrator on 2017/5/8.
 */

public class AlertDialogUtils {
    private static AlertDialogUtils sAlertDialogUtils;

    public interface Callback {
        void select(boolean isSuccessed);
    }

    public static AlertDialogUtils getInstance() {
        if (sAlertDialogUtils == null) {
            sAlertDialogUtils = new AlertDialogUtils();
        }
        return sAlertDialogUtils;
    }


    public void showTestResultDialog(Context context, String title, final Callback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(android.R.drawable.ic_menu_info_details).setTitle(title + " 是否成功？").setMessage("请点击测试的结果")
                .setCancelable(false).setPositiveButton("成功", new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        callback.select(true);
                        dialog.dismiss();
                    }
                }).setNegativeButton("失败", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                callback.select(false);
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.show();
    }

    public void showTestMsg(Context context, String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(android.R.drawable.ic_menu_info_details).setTitle(title).setMessage(msg);
        AlertDialog alert = builder.create();
        alert.show();
    }

    private ProgressDialog mProgressDialog;
    private String mTilte;

    public interface StopCallback {
        void stop();
    }

    public void initProgressDialog(Context context, final StopCallback callback) {
        if (mProgressDialog != null) {
            mProgressDialog.cancel();
            mProgressDialog = null;
        }
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setButton("停止测试", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                callback.stop();
            }
        });
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        hideProgressDialog();
    }

    public void setProgressDialogMsg(String title, String msg) {
        if (mProgressDialog != null) {
            mTilte = title;
            mProgressDialog.setTitle(title);
            mProgressDialog.setMessage(msg);
            mProgressDialog.show();
        }
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.setTitle("");
            mProgressDialog.setMessage("");
            mProgressDialog.hide();
        }
    }

    public void setProgressDialogVisibility(boolean visibility) {
        if (mProgressDialog != null) {
            if (visibility) {
                mProgressDialog.show();
            } else {
                mProgressDialog.hide();
            }
        }
    }

    public String getTitle() {
        return mTilte;
    }
}
