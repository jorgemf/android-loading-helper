package com.jorgemf.android.loading;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ProgressBar;

import com.jorgemf.android.view.R;

public class RecyclerAdapter<k extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private boolean mShowTopLoading;

	private boolean mShowBottomLoading;

	private boolean mShowTopError;

	private boolean mShowBottomError;

	private LoadingFragment mLoadingFragment;

	private RecyclerView.Adapter mAdapter;

	public RecyclerAdapter(RecyclerView.Adapter<k> adapter, LoadingFragment loadingFragment) {
		mAdapter = adapter;
		mLoadingFragment = loadingFragment;
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
				View topLoadingView = LayoutInflater.from(mLoadingFragment.getActivity())
						.inflate(R.layout.view_loading, viewGroup, false);
				ViewHolder topViewHolder = new ViewHolder(topLoadingView);
				topViewHolder.mTopLoading = (ProgressBar) topLoadingView.findViewById(R.id.loading_progress_bar);
				topViewHolder.mTopLoading.setMax(LoadingFragment.PROGRESS_BAR_MAX);
				topViewHolder.mTopLoading.setProgressDrawable(new CircularLoadingDrawable(mLoadingFragment.getActivity()));
				viewHolder = topViewHolder;
				break;
			case 1:
				viewHolder = new ViewHolder(mLoadingFragment.createTopErrorView());
				break;
			case 2:
				View bottomLoadingView = LayoutInflater.from(mLoadingFragment.getActivity())
						.inflate(R.layout.view_loading, viewGroup, false);
				viewHolder = new ViewHolder(bottomLoadingView);
				break;
			case 3:
				viewHolder = new ViewHolder(mLoadingFragment.createBottomErrorView());
				break;
			default:
				viewHolder = mAdapter.onCreateViewHolder(viewGroup, type - 4);
		}
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		int pos = position;
		if (mShowTopLoading) {
			pos -= 1;
		}
		if (mShowTopError) {
			pos -= 1;
		}
		if (viewHolder.getItemViewType() == 0) {
			mLoadingFragment.bindTopLoadingView(viewHolder.itemView, ((ViewHolder) viewHolder).mTopLoading);
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
		if (mShowTopLoading) {
			pos -= 1;
		}
		if (mShowTopError) {
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
		if (mShowTopError != show && mLoadingFragment.hasTopErrorView()) {
			mShowTopError = show;
		}
	}

	public void showBottomLoading(boolean show) {
		if (mShowBottomLoading != show) {
			mShowBottomLoading = show;
		}
	}

	public void showBottomError(boolean show) {
		if (mShowBottomError != show && mLoadingFragment.hasBottomErrorView()) {
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

		protected ProgressBar mTopLoading;

		public ViewHolder(View view) {
			super(view);
		}
	}
}