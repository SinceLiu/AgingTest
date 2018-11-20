package com.dream.agingtest;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class LcdView extends View {
	static int screen_X;
	static int screen_Y;
	
	private Paint mPaint;
	private ArrayList<BitmapItem> biList;
	int biListIndex;
	int biListSize;
	boolean isLastColor = false;

	public LcdView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mPaint = new Paint();
		inputBiList();
		biListSize = biList.size();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (biListIndex >= biList.size()) {
			biListIndex = 0;
		}
		if (biListIndex == biList.size() - 1) {
			isLastColor = true;
		}
		Bitmap bmp = getBitmap(biList.get(biListIndex));
		canvas.drawBitmap(bmp, 0, 0, mPaint);
		bmp.recycle();
	}
	
	private Bitmap getBitmap(BitmapItem bi){
		Bitmap bmp = Bitmap.createBitmap(screen_X, screen_Y, Bitmap.Config.RGB_565);
		Canvas cvs = new Canvas(bmp);
		
		int count = bi.color.length;
		for (int i = 0; i < count; i++) {
			mPaint.setColor(bi.color[i]);
			if (bi.isHorizontal) {
				int top = i * (screen_Y / count);
				int bottom = (i + 1) * (screen_Y / count);
				Rect r = new Rect(0, top, screen_X, bottom);
				cvs.drawRect(r, mPaint);
			}else{
				int left = i * (screen_X / count);
				int right = (i + 1) * (screen_X / count);
				Rect r = new Rect(left, 0, right, screen_Y);
				cvs.drawRect(r, mPaint);
			}
		}
		
		return bmp;
	}
	
	private void inputBiList(){
		biList = new ArrayList<BitmapItem>();
		biList.add(new BitmapItem(new int[]{Color.WHITE}, true));
		biList.add(new BitmapItem(new int[]{Color.RED}, true));
		biList.add(new BitmapItem(new int[]{Color.GREEN}, true));
		biList.add(new BitmapItem(new int[]{Color.BLUE}, true));
//		biList.add(new BitmapItem(new int[]{Color.GRAY, Color.RED, Color.GREEN, Color.BLUE}, true));
		biList.add(new BitmapItem(new int[]{Color.BLACK}, true));
	}
	
	private class BitmapItem {
		private int[] color;
		private boolean isHorizontal;

		private BitmapItem(int[] color, boolean isHorizontal) {
			super();
			this.color = color;
			this.isHorizontal = isHorizontal;
		}

	}
}
