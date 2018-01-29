// Generated code from Butter Knife. Do not modify!
package com.tapc.test.activtiy;

import android.view.View;
import android.widget.GridView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Finder;
import butterknife.internal.ViewBinder;
import java.lang.IllegalStateException;
import java.lang.Object;
import java.lang.Override;

public class MainActivity$$ViewBinder<T extends MainActivity> implements ViewBinder<T> {
  @Override
  public Unbinder bind(Finder finder, T target, Object source) {
    return new InnerUnbinder<>(target, finder, source);
  }

  protected static class InnerUnbinder<T extends MainActivity> implements Unbinder {
    protected T target;

    private View view2131296269;

    private View view2131296266;

    private View view2131296261;

    private View view2131296267;

    private View view2131296268;

    private View view2131296262;

    private View view2131296263;

    private View view2131296264;

    private View view2131296270;

    private View view2131296271;

    private View view2131296265;

    protected InnerUnbinder(final T target, Finder finder, Object source) {
      this.target = target;

      View view;
      target.mTestGridview = finder.findRequiredViewAsType(source, 2131296259, "field 'mTestGridview'", GridView.class);
      view = finder.findRequiredView(source, 2131296269, "field 'mTestResultTxt' and method 'resultOnClick'");
      target.mTestResultTxt = finder.castView(view, 2131296269, "field 'mTestResultTxt'");
      view2131296269 = view;
      view.setOnClickListener(new DebouncingOnClickListener() {
        @Override
        public void doClick(View p0) {
          target.resultOnClick();
        }
      });
      view = finder.findRequiredView(source, 2131296266, "field 'mErpBtn' and method 'erplOnclick'");
      target.mErpBtn = finder.castView(view, 2131296266, "field 'mErpBtn'");
      view2131296266 = view;
      view.setOnClickListener(new DebouncingOnClickListener() {
        @Override
        public void doClick(View p0) {
          target.erplOnclick(p0);
        }
      });
      view = finder.findRequiredView(source, 2131296261, "field 'mTitleText' and method 'titleOnclick'");
      target.mTitleText = finder.castView(view, 2131296261, "field 'mTitleText'");
      view2131296261 = view;
      view.setOnClickListener(new DebouncingOnClickListener() {
        @Override
        public void doClick(View p0) {
          target.titleOnclick(p0);
        }
      });
      view = finder.findRequiredView(source, 2131296267, "field 'mTestVaCopy' and method 'vaCopyOnclick'");
      target.mTestVaCopy = finder.castView(view, 2131296267, "field 'mTestVaCopy'");
      view2131296267 = view;
      view.setOnClickListener(new DebouncingOnClickListener() {
        @Override
        public void doClick(View p0) {
          target.vaCopyOnclick(p0);
        }
      });
      view = finder.findRequiredView(source, 2131296268, "field 'mPartitions' and method 'showPartitions'");
      target.mPartitions = finder.castView(view, 2131296268, "field 'mPartitions'");
      view2131296268 = view;
      view.setOnClickListener(new DebouncingOnClickListener() {
        @Override
        public void doClick(View p0) {
          target.showPartitions(p0);
        }
      });
      view = finder.findRequiredView(source, 2131296262, "method 'startTestClick'");
      view2131296262 = view;
      view.setOnClickListener(new DebouncingOnClickListener() {
        @Override
        public void doClick(View p0) {
          target.startTestClick(p0);
        }
      });
      view = finder.findRequiredView(source, 2131296263, "method 'startTestFailedClick'");
      view2131296263 = view;
      view.setOnClickListener(new DebouncingOnClickListener() {
        @Override
        public void doClick(View p0) {
          target.startTestFailedClick(p0);
        }
      });
      view = finder.findRequiredView(source, 2131296264, "method 'mcubslOnclick'");
      view2131296264 = view;
      view.setOnClickListener(new DebouncingOnClickListener() {
        @Override
        public void doClick(View p0) {
          target.mcubslOnclick(p0);
        }
      });
      view = finder.findRequiredView(source, 2131296270, "method 'close'");
      view2131296270 = view;
      view.setOnClickListener(new DebouncingOnClickListener() {
        @Override
        public void doClick(View p0) {
          target.close();
        }
      });
      view = finder.findRequiredView(source, 2131296271, "method 'uninstall'");
      view2131296271 = view;
      view.setOnClickListener(new DebouncingOnClickListener() {
        @Override
        public void doClick(View p0) {
          target.uninstall();
        }
      });
      view = finder.findRequiredView(source, 2131296265, "method 'systemSettingOnclick'");
      view2131296265 = view;
      view.setOnClickListener(new DebouncingOnClickListener() {
        @Override
        public void doClick(View p0) {
          target.systemSettingOnclick(p0);
        }
      });
    }

    @Override
    public void unbind() {
      T target = this.target;
      if (target == null) throw new IllegalStateException("Bindings already cleared.");

      target.mTestGridview = null;
      target.mTestResultTxt = null;
      target.mErpBtn = null;
      target.mTitleText = null;
      target.mTestVaCopy = null;
      target.mPartitions = null;

      view2131296269.setOnClickListener(null);
      view2131296269 = null;
      view2131296266.setOnClickListener(null);
      view2131296266 = null;
      view2131296261.setOnClickListener(null);
      view2131296261 = null;
      view2131296267.setOnClickListener(null);
      view2131296267 = null;
      view2131296268.setOnClickListener(null);
      view2131296268 = null;
      view2131296262.setOnClickListener(null);
      view2131296262 = null;
      view2131296263.setOnClickListener(null);
      view2131296263 = null;
      view2131296264.setOnClickListener(null);
      view2131296264 = null;
      view2131296270.setOnClickListener(null);
      view2131296270 = null;
      view2131296271.setOnClickListener(null);
      view2131296271 = null;
      view2131296265.setOnClickListener(null);
      view2131296265 = null;

      this.target = null;
    }
  }
}
