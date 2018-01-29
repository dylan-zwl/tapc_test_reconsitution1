// Generated code from Butter Knife. Do not modify!
package com.tapc.test.activtiy;

import android.view.View;
import android.widget.SeekBar;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Finder;
import butterknife.internal.ViewBinder;
import java.lang.IllegalStateException;
import java.lang.Object;
import java.lang.Override;

public class BrightnessTestActivity$$ViewBinder<T extends BrightnessTestActivity> implements ViewBinder<T> {
  @Override
  public Unbinder bind(Finder finder, T target, Object source) {
    return new InnerUnbinder<>(target, finder, source);
  }

  protected static class InnerUnbinder<T extends BrightnessTestActivity> implements Unbinder {
    protected T target;

    private View view2131296258;

    private View view2131296257;

    protected InnerUnbinder(final T target, Finder finder, Object source) {
      this.target = target;

      View view;
      target.mBrightnessBar = finder.findRequiredViewAsType(source, 2131296256, "field 'mBrightnessBar'", SeekBar.class);
      view = finder.findRequiredView(source, 2131296258, "method 'testFinish'");
      view2131296258 = view;
      view.setOnClickListener(new DebouncingOnClickListener() {
        @Override
        public void doClick(View p0) {
          target.testFinish(p0);
        }
      });
      view = finder.findRequiredView(source, 2131296257, "method 'testAgain'");
      view2131296257 = view;
      view.setOnClickListener(new DebouncingOnClickListener() {
        @Override
        public void doClick(View p0) {
          target.testAgain(p0);
        }
      });
    }

    @Override
    public void unbind() {
      T target = this.target;
      if (target == null) throw new IllegalStateException("Bindings already cleared.");

      target.mBrightnessBar = null;

      view2131296258.setOnClickListener(null);
      view2131296258 = null;
      view2131296257.setOnClickListener(null);
      view2131296257 = null;

      this.target = null;
    }
  }
}
