// Generated code from Butter Knife. Do not modify!
package com.tapc.test.activtiy;

import android.view.View;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Finder;
import butterknife.internal.ViewBinder;
import com.tapc.test.widget.TouchView;
import com.tapc.test.widget.TouchView2;
import java.lang.IllegalStateException;
import java.lang.Object;
import java.lang.Override;

public class TouchTestActivity$$ViewBinder<T extends TouchTestActivity> implements ViewBinder<T> {
  @Override
  public Unbinder bind(Finder finder, T target, Object source) {
    return new InnerUnbinder<>(target, finder, source);
  }

  protected static class InnerUnbinder<T extends TouchTestActivity> implements Unbinder {
    protected T target;

    private View view2131296275;

    protected InnerUnbinder(final T target, Finder finder, Object source) {
      this.target = target;

      View view;
      view = finder.findRequiredView(source, 2131296275, "field 'touchTestFinish' and method 'testFinish'");
      target.touchTestFinish = finder.castView(view, 2131296275, "field 'touchTestFinish'");
      view2131296275 = view;
      view.setOnClickListener(new DebouncingOnClickListener() {
        @Override
        public void doClick(View p0) {
          target.testFinish(p0);
        }
      });
      target.touView = finder.findRequiredViewAsType(source, 2131296273, "field 'touView'", TouchView.class);
      target.touView2 = finder.findRequiredViewAsType(source, 2131296274, "field 'touView2'", TouchView2.class);
    }

    @Override
    public void unbind() {
      T target = this.target;
      if (target == null) throw new IllegalStateException("Bindings already cleared.");

      target.touchTestFinish = null;
      target.touView = null;
      target.touView2 = null;

      view2131296275.setOnClickListener(null);
      view2131296275 = null;

      this.target = null;
    }
  }
}
