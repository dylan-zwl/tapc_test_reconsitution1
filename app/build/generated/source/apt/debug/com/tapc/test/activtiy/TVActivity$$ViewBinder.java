// Generated code from Butter Knife. Do not modify!
package com.tapc.test.activtiy;

import android.view.SurfaceView;
import android.view.View;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Finder;
import butterknife.internal.ViewBinder;
import java.lang.IllegalStateException;
import java.lang.Object;
import java.lang.Override;

public class TVActivity$$ViewBinder<T extends TVActivity> implements ViewBinder<T> {
  @Override
  public Unbinder bind(Finder finder, T target, Object source) {
    return new InnerUnbinder<>(target, finder, source);
  }

  protected static class InnerUnbinder<T extends TVActivity> implements Unbinder {
    protected T target;

    private View view2131296277;

    protected InnerUnbinder(final T target, Finder finder, Object source) {
      this.target = target;

      View view;
      target.mSurfaceView = finder.findRequiredViewAsType(source, 2131296276, "field 'mSurfaceView'", SurfaceView.class);
      view = finder.findRequiredView(source, 2131296277, "method 'testFinish'");
      view2131296277 = view;
      view.setOnClickListener(new DebouncingOnClickListener() {
        @Override
        public void doClick(View p0) {
          target.testFinish(p0);
        }
      });
    }

    @Override
    public void unbind() {
      T target = this.target;
      if (target == null) throw new IllegalStateException("Bindings already cleared.");

      target.mSurfaceView = null;

      view2131296277.setOnClickListener(null);
      view2131296277 = null;

      this.target = null;
    }
  }
}
