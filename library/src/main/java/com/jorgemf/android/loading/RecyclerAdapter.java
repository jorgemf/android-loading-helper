package com.jorgemf.android.loading;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ProgressBar;

import com.jorgemf.android.loading.R;

/**
 * Recycler adapter wrapper which adds to a recycler adapter views for loading items.
 *
 * @param <k> ViewHolder of the adapter
 */
public class RecyclerAdapter<k extends RecyclerView.ViewHolder> extends RecyclerView
		.Adapter<RecyclerView.ViewHolder> {

	private static final int TYPE_TOP_LOADING = 0;

	private static final int TYPE_TOP_ERROR = 1;

	private static final int TYPE_BOTTOM_LOADING = 2;

	private static final int TYPE_BOTTOM_ERROR = 3;

	private boolean mShowTopLoading;

	private boolean mShowBottomLoading;

	private boolean mShowTopError;

	private boolean mShowBottomError;

	private final Context mContext;

	private LoadingHelper mLoadingHelper;

	private final RecyclerView.Adapter mAdapter;

	private final LoadingHelper.ErrorViewsCreator mErrorViewsCreator;

	/**
	 * Default constructor, it requires the adapter which will wrap and the loading fragment in
	 * order to bind the top loading view.
	 *
	 * @param loadingHelper     Loading helper used to bind the top loading view
	 * @param adapter           Adapter with the data
	 * @param errorViewsCreator Class which create the error views
	 * @param context           Context for the adapter
	 */
	public RecyclerAdapter(@NonNull LoadingHelper loadingHelper,
	                       @NonNull RecyclerView.Adapter<k> adapter,
	                       @NonNull LoadingHelper.ErrorViewsCreator errorViewsCreator,
	                       @NonNull Context context) {
		mLoadingHelper = loadingHelper;
		mAdapter = adapter;
		mErrorViewsCreator = errorViewsCreator;
		mContext = context;
		mShowTopLoading = false;
		mShowBottomLoading = false;
		mShowTopError = false;
		mShowBottomError = false;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
		RecyclerView.ViewHolder viewHolder;
		switch (type) {
			case TYPE_TOP_LOADING:
				View topLoadingView = LayoutInflater.from(mContext)
						.inflate(R.layout.view_loading, viewGroup, false);
				ViewHolder topViewHolder = new ViewHolder(topLoadingView);
				topViewHolder.mTopLoading = (ProgressBar)
						topLoadingView.findViewById(R.id.loading_progress_bar);
				topViewHolder.mTopLoading.setMax(LoadingHelper.PROGRESS_BAR_MAX);
				topViewHolder.mTopLoading.setProgressDrawable(
						new CircularLoadingDrawable(mContext));
				viewHolder = topViewHolder;
				break;
			case TYPE_TOP_ERROR:
				viewHolder = new ViewHolder(mErrorViewsCreator.createTopErrorView(viewGroup));
				break;
			case TYPE_BOTTOM_LOADING:
				View bottomLoadingView = LayoutInflater.from(mContext)
						.inflate(R.layout.view_loading, viewGroup, false);
				viewHolder = new ViewHolder(bottomLoadingView);
				break;
			case TYPE_BOTTOM_ERROR:
				viewHolder = new ViewHolder(mErrorViewsCreator.createBottomErrorView(viewGroup));
				break;
			default:
				viewHolder = mAdapter.onCreateViewHolder(viewGroup, type - 4);
		}
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		switch (viewHolder.getItemViewType()) {
			case TYPE_TOP_LOADING:
				//noinspection unchecked
				mLoadingHelper.bindTopLoadingView(viewHolder.itemView,
						((ViewHolder) viewHolder).mTopLoading);
				break;
			case TYPE_TOP_ERROR:
				break;
			case TYPE_BOTTOM_LOADING:
				break;
			case TYPE_BOTTOM_ERROR:
				break;
			default:
				if (mShowTopLoading) {
					position -= 1;
				}
				if (mShowTopError) {
					position -= 1;
				}
				//noinspection unchecked
				mAdapter.onBindViewHolder((k) viewHolder, position);
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

	/**
	 * @return The number of items of the adapter with the data. It does not include the headar and
	 * footer views for loading items.
	 */
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
				return TYPE_TOP_LOADING;
			} else if ((!mShowTopLoading && mShowTopError && position == 0)
					|| (mShowTopLoading && mShowTopError && position == 1)) {
				return TYPE_TOP_ERROR;
			} else if (mShowBottomLoading && position == itemCount - 1) {
				return TYPE_BOTTOM_LOADING;
			} else if ((!mShowBottomLoading && mShowBottomError && position == itemCount - 1)
					|| (mShowBottomLoading && mShowBottomError && position == itemCount - 2)) {
				return TYPE_BOTTOM_ERROR;
			} else {
				throw new RuntimeException("should not happen");
			}
		}
	}

	/**
	 * Whether to show or not the top loading view.
	 *
	 * @param show
	 */
	public void showTopLoading(boolean show) {
		if (mShowTopLoading != show) {
			mShowTopLoading = show;
		}
	}

	/**
	 * Whether to show or not the top error view. Only used if there is an error view.
	 *
	 * @param show
	 */
	public void showTopError(boolean show) {
		if (mShowTopError != show && mErrorViewsCreator.hasTopErrorView()) {
			mShowTopError = show;
		}
	}

	/**
	 * Whether to show or not the bottom loading view.
	 *
	 * @param show
	 */
	public void showBottomLoading(boolean show) {
		if (mShowBottomLoading != show) {
			mShowBottomLoading = show;
		}
	}

	/**
	 * Whether to show or not the bottom error view. Only used if there is an error view.
	 *
	 * @param show
	 */
	public void showBottomError(boolean show) {
		if (mShowBottomError != show && mErrorViewsCreator.hasBottomErrorView()) {
			mShowBottomError = show;
		}
	}

	/**
	 * @return true if the top loading view is being displayed
	 */
	public boolean isShowTopLoading() {
		return mShowTopLoading;
	}

	/**
	 * @return true if the bottom loading view is being displayed
	 */
	public boolean isShowBottomLoading() {
		return mShowBottomLoading;
	}

	/**
	 * @return true if the top error view is being displayed
	 */
	public boolean isShowTopError() {
		return mShowTopError;
	}

	/**
	 * @return true if the bottom error view is being displayed
	 */
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