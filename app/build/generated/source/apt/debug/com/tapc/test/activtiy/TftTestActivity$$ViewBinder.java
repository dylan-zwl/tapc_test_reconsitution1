// Generated code from Butter Knife. Do not modify!
package com.tapc.test.activtiy;

import android.view.View;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Finder;
import butterknife.internal.ViewBinder;
import java.lang.IllegalStateException;
import java.lang.Object;
import java.lang.Override;

public class TftTestActivity$$ViewBinder<T extends TftTestActivity> implements ViewBinder<T> {
  @Override
  public Unbinder bind(Finder finder, T target, Object source) {
    return new InnerUnbinder<>(target, finder, source);
  }

  protected static class InnerUnbinder<T extends TftTestActivity> implements Unbinder {
    protected T target;

    private View view2131296272;

    protected InnerUnbinder(final T target, Finder finder, Object source) {
      this.target = target;

      View view;
      view = finder.findRequiredView(source, 2131296272, "field 'mTftColorView' and method 'viewOnClick'");
      target.mTftColorView = finder.castView(view, 2131296272, "field 'mTftColorView'");
      view2131296272 = view;
      view.setOnClickListener(new DebouncingOnClickListener() {
        @Override
        public void doClick(View p0) {
          target.viewOnClick(p0);
        }
      });
    }

    @Override
    public void unbind() {
      T target = this.target;
      if (target == null) throw new IllegalStateException("Bindings already cleared.");

      target.mTftColorView = null;

      view2131296272.setOnClickListener(null);
      view2131296272 = null;

      this.target = null;
    }
  }
}
