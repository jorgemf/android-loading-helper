package com.jorgemf.android.loading;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.widget.ListAdapter;

public abstract class CursorRecyclerAdapter<k extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<k> implements ListAdapter {

	private Cursor mCursor;

	@Override
	public void onBindViewHolder(k holder, int position) {
		if (mCursor != null && !mCursor.isClosed()) {
			mCursor.moveToPosition(position);
			onBindViewHolder(holder, mCursor, position);
		}
	}

	@Override
	public int getItemCount() {
		if (mCursor != null && !mCursor.isClosed()) {
			return mCursor.getCount();
		} else {
			return 0;
		}
	}

	public Cursor getCursor() {
		return mCursor;
	}

	public void changeCursor(Cursor cursor) {
		Cursor old = swapCursor(cursor);
		if (old != null) {
			old.close();
		}
	}

	public Cursor swapCursor(Cursor newCursor) {
		if (newCursor == mCursor) {
			return null;
		}
		Cursor oldCursor = mCursor;
		mCursor = newCursor;
		findIndexes();
		return oldCursor;
	}

	public abstract void findIndexes();

	public abstract void onBindViewHolder(k holder, Cursor cursor, int position);
}
