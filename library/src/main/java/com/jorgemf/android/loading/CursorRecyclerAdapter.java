package com.jorgemf.android.loading;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;

/**
 * Helper class to use a cursor in the recycler adapter.
 *
 * @param <k> ViewHolder used for the recycler adapter.
 */
public abstract class CursorRecyclerAdapter<k extends RecyclerView.ViewHolder> extends
		RecyclerView.Adapter<k> {

	private Cursor mCursor;

	@Override
	public final void onBindViewHolder(k holder, int position) {
		if (mCursor != null && !mCursor.isClosed()) {
			mCursor.moveToPosition(position);
		}
		onBindViewHolder(holder, mCursor, position);
	}

	@Override
	public int getItemCount() {
		if (mCursor != null && !mCursor.isClosed()) {
			return mCursor.getCount();
		} else {
			return 0;
		}
	}

	/**
	 * @return The current cursor of the adapter.
	 */
	public Cursor getCursor() {
		return mCursor;
	}

	/**
	 * Changes the cursor, it also closes the current one.
	 *
	 * @param cursor New cursor to use.
	 */
	public void changeCursor(Cursor cursor) {
		Cursor old = swapCursor(cursor);
		if (old != null) {
			old.close();
		}
	}

	/**
	 * Changes the cursor and returns the current one being used.
	 *
	 * @param newCursor New cursor to use
	 * @return The old cursor or null if it wasn't anyone.
	 */
	public Cursor swapCursor(Cursor newCursor) {
		if (newCursor == mCursor) {
			return null;
		}
		Cursor oldCursor = mCursor;
		mCursor = newCursor;
		if (mCursor != null) {
			findIndexes(mCursor);
		}
		return oldCursor;
	}

	/**
	 * Helper method called each time the cursor is changed, useful to get the indexes of the columns.
	 */
	public abstract void findIndexes(Cursor cursor);

	/**
	 * Wrapper method of the w#onBindViewHolder
	 *
	 * @param holder   ViewHolder
	 * @param cursor   Cursor in the current position of the element
	 * @param position Position of the element
	 * @see #onBindViewHolder(android.support.v7.widget.RecyclerView.ViewHolder, android.database.Cursor, int)
	 */
	public abstract void onBindViewHolder(k holder, Cursor cursor, int position);
}
