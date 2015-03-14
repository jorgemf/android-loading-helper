package com.livae.android.loading;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

/**
 * Recycler adapter wrapper which adds to a recycler adapter views for loading items.
 *
 * @param <k> ViewHolder of the adapter
 */
public class RecyclerAdapter<k extends RecyclerView.ViewHolder> extends RecyclerView
		.Adapter<RecyclerView.ViewHolder> {

	private static final int TYPE_TOP_HEADER = -2;
	private static final int TYPE_TOP_LOADING = -3;
	private static final int TYPE_TOP_ERROR = -4;
	private static final int TYPE_BOTTOM_LOADING = -5;
	private static final int TYPE_BOTTOM_ERROR = -6;
	private static final int TYPE_BOTTOM_FOOTER = -7;

	private final Context mContext;
	private final RecyclerView.Adapter mAdapter;
	private final LoadingHelper.ErrorViewsCreator mErrorViewsCreator;

	private boolean mShowTopLoading;
	private boolean mShowBottomLoading;
	private boolean mShowTopError;
	private boolean mShowBottomError;

	private LoadingHelper mLoadingHelper;

	private View mHeaderView;
	private View mFooterView;

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
		mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			@Override
			public void onChanged() {
				RecyclerAdapter.this.notifyDataSetChanged();
			}

			@Override
			public void onItemRangeChanged(int positionStart, int itemCount) {
				RecyclerAdapter.this.notifyItemRangeChanged(positionStart, itemCount);
			}

			@Override
			public void onItemRangeInserted(int positionStart, int itemCount) {
				RecyclerAdapter.this.notifyItemRangeInserted(positionStart, itemCount);
			}

			@Override
			public void onItemRangeRemoved(int positionStart, int itemCount) {
				RecyclerAdapter.this.notifyItemRangeRemoved(positionStart, itemCount);
			}

			@Override
			public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
				for (int i = 0; i < itemCount; i++) {
					RecyclerAdapter.this.notifyItemMoved(fromPosition + 1, toPosition + 1);
				}
			}
		});
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
		RecyclerView.ViewHolder viewHolder;
		switch (type) {
			case TYPE_TOP_HEADER:
				viewHolder = new ViewHolder(mHeaderView);
				break;
			case TYPE_TOP_LOADING:
				View topLoadingView = LayoutInflater.from(mContext)
						.inflate(R.layout.loading_helper_view_loading, viewGroup, false);
				ViewHolder topViewHolder = new ViewHolder(topLoadingView);
				topViewHolder.mTopLoading = (ProgressBar)
						topLoadingView.findViewById(R.id.loading_helper_loading_progress_bar);
				topViewHolder.mTopLoading.setMax(LoadingHelper.PROGRESS_BAR_MAX);
				topViewHolder.mTopLoading.setProgressDrawable(
						new CircularLoadingDrawable(mContext,
								mLoadingHelper.getColorCircularLoading(),
								mLoadingHelper.getColorCircularLoadingActive()));
				viewHolder = topViewHolder;
				break;
			case TYPE_TOP_ERROR:
				viewHolder = new ViewHolder(mErrorViewsCreator.createTopErrorView(viewGroup));
				break;
			case TYPE_BOTTOM_LOADING:
				View bottomLoadingView = LayoutInflater.from(mContext)
						.inflate(R.layout.loading_helper_view_loading, viewGroup, false);
				viewHolder = new ViewHolder(bottomLoadingView);
				break;
			case TYPE_BOTTOM_ERROR:
				viewHolder = new ViewHolder(mErrorViewsCreator.createBottomErrorView(viewGroup));
				break;
			case TYPE_BOTTOM_FOOTER:
				viewHolder = new ViewHolder(mFooterView);
				break;
			default:
				viewHolder = mAdapter.onCreateViewHolder(viewGroup, type);
		}
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		switch (viewHolder.getItemViewType()) {
			case TYPE_TOP_HEADER:
				break;
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
			case TYPE_BOTTOM_FOOTER:
				break;
			default:
				//noinspection unchecked
				mAdapter.onBindViewHolder(viewHolder, position - getHeaderCount());
		}
	}

	private int getHeaderCount() {
		int countAdd = 0;
		if (mHeaderView != null) {
			countAdd += 1;
		}
		if (mShowTopLoading) {
			countAdd += 1;
		}
		if (mShowTopError) {
			countAdd += 1;
		}
		return countAdd;
	}

	@Override
	public int getItemCount() {
		int countAdd = 0;
		if (mHeaderView != null) {
			countAdd += 1;
		}
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
		if (mFooterView != null) {
			countAdd += 1;
		}
		return mAdapter.getItemCount() + countAdd;
	}

	private int getHeaderPosition() {
		int pos = -1;
		if (mHeaderView != null) {
			pos += 1;
		}
		return pos;
	}

	private int getTopLoadingPosition() {
		int pos = getHeaderPosition();
		if (mShowTopLoading) {
			pos += 1;
		}
		return pos;
	}

	private int getTopErrorPosition() {
		int pos = getTopLoadingPosition();
		if (mShowTopError) {
			pos += 1;
		}
		return pos;
	}

	private int getBottomLoadingPosition() {
		int pos = getHeaderCount() + mAdapter.getItemCount();
		if (mShowBottomLoading) {
			pos += 1;
		}
		return pos;
	}

	private int getBottomErrorPosition() {
		int pos = getBottomLoadingPosition();
		if (mShowBottomError) {
			pos += 1;
		}
		return pos;
	}

	private int getFooterPosition() {
		int pos = getBottomErrorPosition();
		if (mFooterView != null) {
			pos += 1;
		}
		return pos;
	}

	/**
	 * @return The number of items of the adapter with the data. It does not include the header and
	 * footer views for loading items, errors and custom views.
	 */
	public int getAdapterItemCount() {
		return mAdapter.getItemCount();
	}

	@Override
	public int getItemViewType(int position) {
		int pos = position - getHeaderCount();
		if (pos >= 0 && pos < mAdapter.getItemCount()) {
			return mAdapter.getItemViewType(pos);
		} else {
			int itemCount = getItemCount();
			if (mHeaderView != null && position == 0) {
				return TYPE_TOP_HEADER;
			} else if (mShowTopLoading
					&& ((mHeaderView == null && position == 0) ||
					(mHeaderView != null && position == 1))) {
				return TYPE_TOP_LOADING;
			} else if (mShowTopError
					&& ((mHeaderView == null && !mShowTopLoading && position == 0) ||
					(mHeaderView == null && mShowTopLoading && position == 1) ||
					(mHeaderView != null && !mShowTopLoading && position == 1) ||
					(mHeaderView != null && mShowTopLoading && position == 2))) {
				return TYPE_TOP_ERROR;
			} else if (mFooterView != null && position == itemCount - 1) {
				return TYPE_BOTTOM_FOOTER;
			} else if (mShowBottomError
					&& ((mFooterView == null && position == itemCount - 1) ||
					(mFooterView != null && position == itemCount - 2))) {
				return TYPE_BOTTOM_ERROR;
			} else if (mShowBottomLoading
					&& ((mFooterView == null && !mShowBottomError && position == itemCount - 1) ||
					(mFooterView == null && mShowBottomError && position == itemCount - 2) ||
					(mFooterView != null && !mShowBottomError && position == itemCount - 2) ||
					(mFooterView != null && mShowBottomError && position == itemCount - 3))) {
				return TYPE_BOTTOM_LOADING;
			} else {
				throw new RuntimeException("should not happen");
			}
		}
	}

	/**
	 * Whether to show or not the top loading view.
	 *
	 * @param show whether to show the view or not
	 */
	public void showTopLoading(boolean show) {
		if (mShowTopLoading != show) {
			if (show) {
				mShowTopLoading = true;
				int position = getTopLoadingPosition();
				notifyItemInserted(position);
			} else {
				int position = getTopLoadingPosition();
				mShowTopLoading = false;
				notifyItemRemoved(position);
			}
		}
	}

	/**
	 * Whether to show or not the top error view. Only used if there is an error view.
	 *
	 * @param show whether to show the view or not
	 */
	public void showTopError(boolean show) {
		if (mShowTopError != show && mErrorViewsCreator.hasTopErrorView()) {
			if (show) {
				mShowTopError = true;
				int position = getTopErrorPosition();
				notifyItemInserted(position);
			} else {
				int position = getTopErrorPosition();
				mShowTopError = false;
				notifyItemRemoved(position);
			}
		}
	}

	/**
	 * Whether to show or not the bottom loading view.
	 *
	 * @param show whether to show the view or not
	 */
	public void showBottomLoading(boolean show) {
		if (mShowBottomLoading != show) {
			if (show) {
				mShowBottomLoading = true;
				int position = getBottomLoadingPosition();
				notifyItemInserted(position);
			} else {
				int position = getBottomLoadingPosition();
				mShowBottomLoading = false;
				notifyItemRemoved(position);
			}
		}
	}

	/**
	 * Whether to show or not the bottom error view. Only used if there is an error view.
	 *
	 * @param show whether to show the view or not
	 */
	public void showBottomError(boolean show) {
		if (mShowBottomError != show && mErrorViewsCreator.hasBottomErrorView()) {
			if (show) {
				mShowBottomError = true;
				int position = getBottomErrorPosition();
				notifyItemInserted(position);
			} else {
				int position = getBottomErrorPosition();
				mShowBottomError = false;
				notifyItemRemoved(position);
			}
		}
	}

	/**
	 * Sets the header view, before the loading and the errors
	 *
	 * @param headerView The view for the header
	 */
	public void setHeaderView(View headerView) {
		if (mHeaderView != headerView) {
			if (mHeaderView == null) {
				mHeaderView = headerView;
				int position = getHeaderPosition();
				notifyItemInserted(position);
			} else {
				if (headerView != null) {
					mHeaderView = headerView;
					int position = getHeaderPosition();
					notifyItemChanged(position);
				} else {
					int position = getHeaderPosition();
					mHeaderView = null;
					notifyItemRemoved(position);
				}
			}
		}
	}

	/**
	 * Sets the footer view, after the loading and the errors
	 *
	 * @param footerView The view for the footer
	 */
	public void setFooterView(View footerView) {
		if (mFooterView != footerView) {
			if (mFooterView == null) {
				mFooterView = footerView;
				int position = getFooterPosition();
				notifyItemInserted(position);
			} else {
				if (footerView != null) {
					mFooterView = footerView;
					int position = getFooterPosition();
					notifyItemChanged(position);
				} else {
					int position = getFooterPosition();
					mFooterView = null;
					notifyItemRemoved(position);
				}
			}
		}
	}

	/**
	 * @return true if the header view is being displayed
	 */
	public boolean isShowHeader() {
		return mHeaderView != null;
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

	/**
	 * @return true if the footer view is being displayed
	 */
	public boolean isShowFooter() {
		return mFooterView != null;
	}

	/**
	 * Notify that the item at <code>position</code> has changed.
	 *
	 * @see android.support.v7.widget.RecyclerView.Adapter#notifyItemChanged(int)
	 */
	public void notifyDataItemChanged(int position) {
		super.notifyItemChanged(getHeaderCount() + position);
	}

	/**
	 * Notify that the <code>itemCount</code> items starting at position <code>positionStart</code>
	 * have changed.
	 *
	 * @see android.support.v7.widget.RecyclerView.Adapter#notifyItemRangeChanged(int, int)
	 */
	public void notifyDataItemRangeChanged(int positionStart, int itemCount) {
		super.notifyItemRangeChanged(getHeaderCount() + positionStart, itemCount);
	}

	/**
	 * Notify that the item reflected at <code>position</code> has been newly inserted. The item
	 * previously at <code>position</code> is now at position <code>position + 1</code>.
	 *
	 * @see android.support.v7.widget.RecyclerView.Adapter#notifyItemInserted(int)
	 */
	public void notifyDataItemInserted(int position) {
		super.notifyItemInserted(getHeaderCount() + position);
	}

	/**
	 * Notify any registered observers that the item reflected at <code>fromPosition</code> has been
	 * moved to <code>toPosition</code>.
	 *
	 * @see android.support.v7.widget.RecyclerView.Adapter#notifyItemMoved(int, int)
	 */
	public void notifyDataItemMoved(int fromPosition, int toPosition) {
		super.notifyItemMoved(getHeaderCount() + fromPosition, getHeaderCount() + toPosition);
	}

	/**
	 * Notify that the currently reflected <code>itemCount</code> items starting at
	 * <code>positionStart</code> have been newly inserted. The items previously located at
	 * <code>positionStart</code> and beyond can now be found starting at position
	 * <code>positionStart + itemCount</code>.
	 *
	 * @see android.support.v7.widget.RecyclerView.Adapter#notifyItemRangeInserted(int, int)
	 */
	public void notifyDataItemRangeInserted(int positionStart, int itemCount) {
		super.notifyItemRangeInserted(getHeaderCount() + positionStart, itemCount);
	}

	/**
	 * Notify that the item previously located at <code>position</code> has been removed from the
	 * data set. The items previously located at and after <code>position</code> may now be found at
	 * <code>oldPosition - 1</code>.
	 *
	 * @see android.support.v7.widget.RecyclerView.Adapter#notifyItemRemoved(int)
	 */
	public void notifyDataItemRemoved(int position) {
		super.notifyItemRemoved(getHeaderCount() + position);
	}

	/**
	 * Notify that the <code>itemCount</code> items previously located at <code>positionStart</code>
	 * have been removed from the data set. The items previously located at and after
	 * <code>positionStart + itemCount</code> may now be found at <code>oldPosition -
	 * itemCount</code>.
	 *
	 * @see android.support.v7.widget.RecyclerView.Adapter#notifyItemRangeRemoved(int, int)
	 */
	public void notifyDataItemRangeRemoved(int positionStart, int itemCount) {
		super.notifyItemRangeRemoved(getHeaderCount() + positionStart, itemCount);
	}

	class ViewHolder extends RecyclerView.ViewHolder {

		protected ProgressBar mTopLoading;

		public ViewHolder(View view) {
			super(view);
		}
	}


}