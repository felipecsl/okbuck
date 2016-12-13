package com.uber.okbuck.example.common;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class CustomFrameLayout extends FrameLayout {
  public CustomFrameLayout(Context context) {
    super(context);
  }

  public CustomFrameLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public CustomFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }
}
