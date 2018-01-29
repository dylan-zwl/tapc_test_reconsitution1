// Generated code from Butter Knife. Do not modify!
package com.tapc.test.widget;

import android.view.View;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Finder;
import butterknife.internal.ViewBinder;
import java.lang.IllegalStateException;
import java.lang.Object;
import java.lang.Override;

public class MenuBar$$ViewBinder<T extends MenuBar> implements ViewBinder<T> {
  @Override
  public Unbinder bind(Finder finder, T target, Object source) {
    return new InnerUnbinder<>(target, finder, source);
  }

  protected static class InnerUnbinder<T extends MenuBar> implements Unbinder {
    protected T target;

    private View view2131296279;

    private View view2131296278;

    private View view2131296280;

    protected InnerUnbinder(final T target, Finder finder, Object source) {
      this.target = target;

      View view;
      view = finder.findRequiredView(source, 2131296279, "method 'backOnClick'");
      view2131296279 = view;
      view.setOnClickListener(new DebouncingOnClickListener() {
        @Override
        public void doClick(View p0) {
          target.backOnClick(p0);
        }
      });
      view = finder.findRequiredView(source, 2131296278, "method 'homeOnClick'");
      view2131296278 = view;
      view.setOnClickListener(new DebouncingOnClickListener() {
        @Override
        public void doClick(View p0) {
          target.homeOnClick(p0);
        }
      });
      view = finder.findRequiredView(source, 2131296280, "method 'closeOnClick'");
      view2131296280 = view;
      view.setOnClickListener(new DebouncingOnClickListener() {
        @Override
        public void doClick(View p0) {
          target.closeOnClick(p0);
        }
      });
    }

    @Override
    public void unbind() {
      if (this.target == null) throw new IllegalStateException("Bindings already cleared.");

      view2131296279.setOnClickListener(null);
      view2131296279 = null;
      view2131296278.setOnClickListener(null);
      view2131296278 = null;
      view2131296280.setOnClickListener(null);
      view2131296280 = null;

      this.target = null;
    }
  }
}
