package com.jorgemf.android.loading;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;

public class RecyclerAdapter<k extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private boolean mShowTopLoading;

	private boolean mShowBottomLoading;

	private boolean mShowTopError;

	private boolean mShowBottomError;

	private View mTopLoadingView;

	private View mBottomLoadingView;

	private View mTopErrorView;

	private View mBottomErrorView;

	private RecyclerView.Adapter mAdapter;

	public RecyclerAdapter(RecyclerView.Adapter<k> adapter,
	                       View topLoadingView, View bottomLoadingView,
	                       View topErrorView, View bottomErrorView) {
		mAdapter = adapter;
		mTopLoadingView = topLoadingView;
		mBottomLoadingView = bottomLoadingView;
		mTopErrorView = topErrorView;
		mBottomErrorView = bottomErrorView;
		mShowTopLoading = false;
		mShowBottomLoading = false;
		mShowTopError = false;
		mShowBottomError = false;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
		RecyclerView.ViewHolder viewHolder;
		switch (type) {
			case 0:
				viewHolder = new ViewHolder(mTopLoadingView);
				break;
			case 1:
				viewHolder = new ViewHolder(mTopErrorView);
				break;
			case 2:
				viewHolder = new ViewHolder(mBottomLoadingView);
				break;
			case 3:
				viewHolder = new ViewHolder(mBottomErrorView);
				break;
			default:
				viewHolder = mAdapter.onCreateViewHolder(viewGroup, type - 4);
		}
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		int pos = position;
		if (mShowTopLoading || mShowTopError) {
			pos -= 1;
		}
		if (pos >= 0 && pos < mAdapter.getItemCount()) {
			//noinspection unchecked
			mAdapter.onBindViewHolder((k) viewHolder, pos);
		}
	}

	@Override
	public int getItemCount() {
		int countAdd = 0;
		if (mShowTopLoading) {
			countAdd += 1;
		}
		if (mShowTopError) {
			countAdd += 1;
		}
		if (mShowBottomLoading) {
			countAdd += 1;
		}
		if (mShowBottomError) {
			countAdd += 1;
		}
		return mAdapter.getItemCount() + countAdd;
	}

	public int getAdapterItemCount() {
		return mAdapter.getItemCount();
	}

	@Override
	public int getItemViewType(int position) {
		int pos = position;
		if (mShowTopLoading || mShowTopError) {
			pos -= 1;
		}
		if (pos >= 0 && pos < mAdapter.getItemCount()) {
			int itemType = mAdapter.getItemViewType(pos);
			if (itemType == Adapter.IGNORE_ITEM_VIEW_TYPE) {
				return itemType;
			} else if (itemType >= 0) {
				return itemType + 4;
			} else {
				return itemType;
			}
		} else {
			int itemCount = getItemCount();
			if (mShowTopLoading && position == 0) {
				return 0;
			} else if ((!mShowTopLoading && mShowTopError && position == 0)
					|| (mShowTopLoading && mShowTopError && position == 1)) {
				return 1;
			} else if (mShowBottomLoading && position == itemCount - 1) {
				return 2;
			} else if ((!mShowBottomLoading && mShowBottomError && position == itemCount - 1)
					|| (mShowBottomLoading && mShowBottomError && position == itemCount - 2)) {
				return 3;
			} else {
				throw new RuntimeException("should not happen");
			}
		}
	}

	public void showTopLoading(boolean show) {
		if (mShowTopLoading != show) {
			mShowTopLoading = show;
		}
	}

	public void showTopError(boolean show) {
		if (mShowTopError != show && mTopErrorView != null) {
			mShowTopError = show;
		}
	}

	public void showBottomLoading(boolean show) {
		if (mShowBottomLoading != show) {
			mShowBottomLoading = show;
		}
	}

	public void showBottomError(boolean show) {
		if (mShowBottomError != show && mBottomErrorView != null) {
			mShowBottomError = show;
		}
	}

	public boolean isShowTopLoading() {
		return mShowTopLoading;
	}

	public boolean isShowBottomLoading() {
		return mShowBottomLoading;
	}

	public boolean isShowTopError() {
		return mShowTopError;
	}

	public boolean isShowBottomError() {
		return mShowBottomError;
	}

	class ViewHolder extends RecyclerView.ViewHolder {

		public ViewHolder(View view) {
			super(view);
		}
	}
}