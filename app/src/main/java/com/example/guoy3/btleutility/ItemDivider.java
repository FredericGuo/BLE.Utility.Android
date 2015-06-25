package com.example.guoy3.btleutility;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.view.View;

public class ItemDivider extends ItemDecoration {

	private Drawable mDrawable;

	public ItemDivider(Context context, int resId) {
		mDrawable = context.getResources().getDrawable(resId);
	}

	@Override
	public void onDrawOver(Canvas c, RecyclerView parent) {
		final int left = parent.getPaddingLeft();
		final int right = parent.getWidth() - parent.getPaddingRight();

		final int childCount = parent.getChildCount();
		for (int i = 0; i < childCount; i++) {
			final View child = parent.getChildAt(i);
			final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
					.getLayoutParams();

			final int top = child.getBottom() + params.bottomMargin;
			final int bottom = top + mDrawable.getIntrinsicHeight();
			mDrawable.setBounds(left, top, right, bottom);
			mDrawable.draw(c);
		}
	}

	@Override
	public void getItemOffsets(Rect outRect, int position, RecyclerView parent) {
		outRect.set(0, 0, 0, mDrawable.getIntrinsicWidth());
	}
}
