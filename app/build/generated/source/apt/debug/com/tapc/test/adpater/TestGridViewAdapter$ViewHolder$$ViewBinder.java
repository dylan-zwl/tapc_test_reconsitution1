// Generated code from Butter Knife. Do not modify!
package com.tapc.test.adpater;

import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Finder;
import butterknife.internal.ViewBinder;
import java.lang.IllegalStateException;
import java.lang.Object;
import java.lang.Override;

public class TestGridViewAdapter$ViewHolder$$ViewBinder<T extends TestGridViewAdapter.ViewHolder> implements ViewBinder<T> {
  @Override
  public Unbinder bind(Finder finder, T target, Object source) {
    return new InnerUnbinder<>(target, finder, source);
  }

  protected static class InnerUnbinder<T extends TestGridViewAdapter.ViewHolder> implements Unbinder {
    protected T target;

    protected InnerUnbinder(T target, Finder finder, Object source) {
      this.target = target;

      target.testItemIcon = finder.findRequiredViewAsType(source, 2131296284, "field 'testItemIcon'", ImageView.class);
      target.testItemName = finder.findRequiredViewAsType(source, 2131296285, "field 'testItemName'", TextView.class);
      target.testItemSatus = finder.findRequiredViewAsType(source, 2131296286, "field 'testItemSatus'", ImageView.class);
    }

    @Override
    public void unbind() {
      T target = this.target;
      if (target == null) throw new IllegalStateException("Bindings already cleared.");

      target.testItemIcon = null;
      target.testItemName = null;
      target.testItemSatus = null;

      this.target = null;
    }
  }
}
