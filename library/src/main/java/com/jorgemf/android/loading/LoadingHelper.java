package com.jorgemf.android.loading;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Helper which handles pull to refresh and endless loading in a recycler view.
 *
 * @param <k> ViewHolder of the recycler view
 */
public class LoadingHelper<k extends RecyclerView.ViewHolder> implements View.OnTouchListener {

	private static final int INVALID_POINTER = -1;

	/**
	 * Maximum value of the progress bar.
	 */
	protected static final int PROGRESS_BAR_MAX = 1000;

	private final AtomicBoolean mIsLoadingNext = new AtomicBoolean(false);

	private final AtomicBoolean mIsLoadingPrevious = new AtomicBoolean(false);

	private final LoadListener mLoadListener;

	private final RecyclerView mRecyclerView;

	private LinearLayoutManager mLayoutManager;

	private final RecyclerAdapter mAdapter;

	private final ContentLoadingProgressBar mContentLoadingProgressBar;

	private boolean mEnableInitialProgressLoading;

	private boolean mEnabledPullToRefresUpdate;

	private boolean mEnableEndlessLoading;

	private int mEndlessLoadingPreloadAhead;

	private View mTopLoadingView;

	private ProgressBar mTopLoadingProgressBar;

	private float mPullToRefreshInitialY;

	private int mPullToRefreshDistance;

	private int mPullToRefreshAnimationDuration;

	private int mLoadingViewOriginalHeight;

	private ValueAnimator mPullToRefreshUpdateAnimation;

	private int mActivePointerId;

	private final DecelerateInterpolator mDecelerateInterpolator;

	/**
	 * Default constructor
	 *
	 * @param activity          Activity
	 * @param recyclerView      Recycler view
	 * @param adapter           Adapter with the data. It will be set into the recycler view inside
	 *                          a wrapper adapter.
	 * @param loadListener      Load listener which received the load actions and load the next
	 *                          items
	 * @param initialLoading    Initial loading progress bar
	 * @param errorViewsCreator Errors view creator, it can be set to null an no error views
	 *                             will be
	 *                          displayed
	 */
	public LoadingHelper(@NonNull Activity activity, @NonNull RecyclerView recyclerView,
	                     @NonNull RecyclerView.Adapter<k> adapter,
	                     @NonNull LoadListener loadListener,
	                     ContentLoadingProgressBar initialLoading,
	                     ErrorViewsCreator errorViewsCreator) {
		mRecyclerView = recyclerView;
		mLoadListener = loadListener;
		mContentLoadingProgressBar = initialLoading;
		mEnableInitialProgressLoading = false;
		mEnabledPullToRefresUpdate = false;
		mEnableEndlessLoading = false;
		mEndlessLoadingPreloadAhead = 0;
		mDecelerateInterpolator = new DecelerateInterpolator(2f);
		mActivePointerId = INVALID_POINTER;
		mPullToRefreshInitialY = -1;

		setLayoutManager(new LinearLayoutManager(activity));

		if (errorViewsCreator == null) {
			errorViewsCreator = new ErrorViewsCreator() {

				@Override
				public View createTopErrorView(ViewGroup root) {
					return null;
				}

				@Override
				public View createBottomErrorView(ViewGroup root) {
					return null;
				}

				@Override
				public boolean hasTopErrorView() {
					return false;
				}

				@Override
				public boolean hasBottomErrorView() {
					return false;
				}
			};
		}
		// specify the adapter
		mAdapter = new RecyclerAdapter<k>(this, adapter, errorViewsCreator, activity);
		mRecyclerView.setAdapter(mAdapter);

		if (mContentLoadingProgressBar != null) {
			mContentLoadingProgressBar.setVisibility(View.GONE);
		}

		mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
			}

			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				checkLoadNext();
			}
		});
		mRecyclerView.setOnTouchListener(this);
		Resources resources = activity.getResources();
		mPullToRefreshDistance = resources.getDimensionPixelSize(R.dimen
				.loading_helper_pull_refresh_distance);
		mPullToRefreshAnimationDuration = resources.getInteger(
				android.R.integer.config_mediumAnimTime);
	}

	/**
	 * Sets the layout manager of the recycler view.
	 *
	 * @param layoutManager The new layout manager.
	 * @see android.support.v7.widget.LinearLayoutManager
	 */
	public void setLayoutManager(@NonNull LinearLayoutManager layoutManager) {
		mLayoutManager = layoutManager;
		mRecyclerView.setLayoutManager(mLayoutManager);
	}

	/**
	 * This method should be called on resume of the activity.
	 */
	public void onResume() {
		mActivePointerId = INVALID_POINTER;
		mPullToRefreshInitialY = -1;
	}

	/**
	 * Whether the initial progress loading will be performed or not. Future calls after the
	 * initial
	 * loading wont do anything unless you restart the fragment.
	 *
	 * @param enable whether the initial progress loading is enabled or not
	 * @see LoadListener#loadInitial()
	 */
	public void enableInitialProgressLoading(boolean enable) {
		mEnableInitialProgressLoading = enable;
	}

	/**
	 * Whether the pull to refresh is enabled for the fragment or not.
	 *
	 * @param enable whether the pull to refresh loading is enabled or not
	 * @see LoadListener#loadPrevious()
	 */
	public void enablePullToRefreshUpdate(boolean enable) {
		mEnabledPullToRefresUpdate = enable;
	}

	/**
	 * Whether the endless loading is enabled for the fragment or not.
	 *
	 * @param enable whether endless loading is enabled or not
	 * @see LoadListener#loadNext()
	 */
	public void enableEndlessLoading(boolean enable) {
		mEnableEndlessLoading = enable;
	}


	/**
	 * Sets the number of elements before reaching the end of the recycler view to call the loading
	 * method.
	 *
	 * @param numberOfElements Number of elements
	 * @see LoadListener#loadNext()
	 */
	public void endlessLoadingPreloadAhead(int numberOfElements) {
		if (numberOfElements < 0) {
			numberOfElements = 0;
		}
		mEndlessLoadingPreloadAhead = numberOfElements;
	}

	/**
	 * This method must be called after preloading the initial items.
	 *
	 * @see com.jorgemf.android.loading.LoadingHelper.LoadListener#preloadInitial()
	 */
	public synchronized void finishPreloadInitial() {
		mIsLoadingNext.set(true);
		mLoadListener.loadInitial();
	}

	/**
	 * This method must be called after loading previous items.
	 *
	 * @param showTopErrorView whether to show or not the top error view
	 * @param dataInserted     Number of elements inserted before the first element
	 * @see com.jorgemf.android.loading.LoadingHelper.LoadListener#loadPrevious()
	 */
	public synchronized void finishLoadingPrevious(boolean showTopErrorView,
	                                               int dataInserted) {
		if (mIsLoadingPrevious.getAndSet(false)) {
			mAdapter.showTopLoading(false);
			mAdapter.notifyItemRemoved(0);
			if (showTopErrorView) {
				mAdapter.showTopError(true);
				mAdapter.notifyItemInserted(0);
			} else if (dataInserted > 0) {
				mAdapter.notifyItemRangeInserted(0, dataInserted);
			}
		}
	}

	/**
	 * This method must be called after loading the next items.
	 *
	 * @param showBottomErrorView whether to show or not the bottom error view
	 * @param dataInserted        Number of elements inserted after the last element
	 * @see com.jorgemf.android.loading.LoadingHelper.LoadListener#loadNext()
	 */
	public synchronized void finishLoadingNext(boolean showBottomErrorView,
	                                           int dataInserted) {
		if (mIsLoadingNext.getAndSet(false)) {
			mAdapter.showBottomLoading(false);
			int itemCount = mAdapter.getItemCount();
			mAdapter.notifyItemRemoved(itemCount - dataInserted);
			if (showBottomErrorView) {
				mAdapter.showBottomError(true);
				mAdapter.notifyItemInserted(itemCount);
			} else if (dataInserted > 0) {
				mAdapter.notifyItemRangeInserted(itemCount - dataInserted, dataInserted);
				checkLoadNext();
			}
		}
	}

	/**
	 * This method must be called after loading the initial items.
	 *
	 * @param showTopErrorView whether to show or not the top error view
	 * @param dataInserted     Number of elements inserted
	 * @see com.jorgemf.android.loading.LoadingHelper.LoadListener#loadInitial()
	 */
	public synchronized void finishLoadingInitial(boolean showTopErrorView, int dataInserted) {
		if (mEnableInitialProgressLoading) {
			mContentLoadingProgressBar.hide();
		}
		if (mIsLoadingNext.getAndSet(false)) {
			mIsLoadingNext.set(false);
			if (showTopErrorView) {
				mAdapter.showTopError(true);
				mAdapter.notifyItemInserted(0);
			} else if (dataInserted > 0) {
				mAdapter.notifyItemRangeInserted(0, dataInserted);
				checkLoadNext();
			}
		}
	}

	/**
	 * When an error is displayed at the top this method tries again to load the previous items
	 * again.
	 */
	public void retryLoadPrevious() {
		if (mAdapter.isShowTopLoading() && mEnabledPullToRefresUpdate &&
				!mIsLoadingPrevious.get()) {
			initPullToRefresh();
		}
	}


	/**
	 * When an error is displayed at the bottom this method tries again to load the next items
	 * again.
	 */
	public void retryLoadNext() {
		if (mAdapter.isShowBottomError() && mEnableEndlessLoading &&
				!mIsLoadingNext.get()) {
			checkLoadNext();
		}
	}

	/**
	 * Starts the loading process.
	 */
	public void start() {
		reset();
	}

	/**
	 * Resets the loading view. All the items are removed from the adapter with the method
	 * #clearAdapter
	 */
	public synchronized void reset() {
		if (mEnableInitialProgressLoading) {
			mContentLoadingProgressBar.show();
		}
		int itemCount;
		if ((itemCount = mAdapter.getItemCount()) > 0) {
			mAdapter.showTopLoading(false);
			mAdapter.showTopError(false);
			mAdapter.showBottomLoading(false);
			mAdapter.showBottomError(false);
			mLoadListener.clearAdapter();
			mAdapter.notifyItemRangeRemoved(0, itemCount);
		}
		mIsLoadingPrevious.set(false);
		mIsLoadingNext.set(true);
		mLoadListener.preloadInitial();
	}

	private void checkLoadNext() {
		if (mEnableEndlessLoading) {
			mRecyclerView.requestLayout();
			mRecyclerView.post(new Runnable() {
				@Override
				public void run() {
					int lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition();
					if (lastVisibleItemPosition + mEndlessLoadingPreloadAhead
							>= mAdapter.getItemCount() - 1 && !mIsLoadingNext.get()) {
						mIsLoadingNext.set(true);
						if (mAdapter.isShowBottomError()) {
							mAdapter.showBottomError(false);
							mAdapter.notifyItemRemoved(mAdapter.getItemCount() - 1);
						}
						if (!mAdapter.isShowBottomLoading()) {
							mAdapter.showBottomLoading(true);
							mAdapter.notifyItemInserted(mAdapter.getItemCount() - 1);
						}
						mLoadListener.loadNext();
					}
				}
			});
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (mEnabledPullToRefresUpdate && !mIsLoadingPrevious.get()) {
			if (mAdapter.getAdapterItemCount() > 0
					&& mLayoutManager.findFirstVisibleItemPosition() == 0
					&& mLayoutManager.findViewByPosition(0).getTop() == 0) {

				int pointer = MotionEventCompat.getPointerId(event, 0);
				if (mActivePointerId == INVALID_POINTER || mActivePointerId == pointer) {

					switch (event.getAction()) {
						case MotionEvent.ACTION_MOVE:
							if (mPullToRefreshInitialY != -1) {
								float diff = event.getY() - mPullToRefreshInitialY;
								if (diff > 0) {
									setPullToRefresh(diff);
									return true;
								} else {
									mPullToRefreshInitialY = -1;
									cancelPullToRefresh();
								}
							} else {
								mPullToRefreshInitialY = event.getY();
								mActivePointerId = pointer;
								initPullToRefresh();
							}
							break;
						case MotionEvent.ACTION_CANCEL:
						case MotionEvent.ACTION_UP:
							if (mPullToRefreshInitialY != -1) {
								float diff = event.getY() - mPullToRefreshInitialY;
								mPullToRefreshInitialY = -1;
								if (diff > mPullToRefreshDistance) {
									startPullToRefresh();
								} else {
									cancelPullToRefresh();
								}
							}
							mActivePointerId = INVALID_POINTER;
							break;
					}
				}
			}
		}
		return false;
	}

	private void initPullToRefresh() {
		if (!mIsLoadingPrevious.getAndSet(true)) {
			if (mAdapter.isShowTopError()) {
				mAdapter.showTopError(false);
				mAdapter.notifyItemRemoved(0);
			}
			if (!mAdapter.isShowTopLoading()) {
				mAdapter.showTopLoading(true);
				mAdapter.notifyItemInserted(0);
			}
			if (mTopLoadingView != null) {
				mTopLoadingView.getLayoutParams().height = 1;
				mTopLoadingProgressBar.setIndeterminate(false);
				mTopLoadingView.requestLayout();
			}
			mRecyclerView.scrollBy(-1, 0);
		}
	}

	private void setPullToRefresh(float displacement) {
		float ratioPull = displacement / mPullToRefreshDistance;
		if (ratioPull > 1) {
			ratioPull = 1;
		}
		if (displacement > mLoadingViewOriginalHeight) {
			displacement = mLoadingViewOriginalHeight +
					(displacement - mLoadingViewOriginalHeight) / 2;
		}
		float ratioDisplacement = displacement / mLoadingViewOriginalHeight;
		if (ratioDisplacement > 1) {
			ratioDisplacement = 1;
		}
		int variation = mTopLoadingView.getLayoutParams().height - (int) displacement;
		mTopLoadingView.getLayoutParams().height = (int) displacement;
		mRecyclerView.scrollBy(variation, 0);
		mTopLoadingView.requestLayout();
		mTopLoadingProgressBar.setProgress((int) (PROGRESS_BAR_MAX * ratioPull));
		mTopLoadingView.setAlpha(ratioDisplacement);
		mTopLoadingProgressBar.setScaleX(ratioPull);
		mTopLoadingProgressBar.setScaleY(ratioPull);
	}

	private void cancelPullToRefresh() {
		if (mPullToRefreshUpdateAnimation != null && mPullToRefreshUpdateAnimation.isRunning()) {
			mPullToRefreshUpdateAnimation.cancel();
		}
		float alpha = mTopLoadingView.getAlpha();
		mPullToRefreshUpdateAnimation = ValueAnimator.ofFloat(alpha, 0);
		mPullToRefreshUpdateAnimation.setDuration(mPullToRefreshAnimationDuration);
		mPullToRefreshUpdateAnimation.setInterpolator(mDecelerateInterpolator);
		mPullToRefreshUpdateAnimation.addUpdateListener(
				new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator valueAnimator) {
						float val = (Float) valueAnimator.getAnimatedValue();
						mTopLoadingView.setAlpha(val);
					}
				});
		mPullToRefreshUpdateAnimation.start();
		mAdapter.showTopLoading(false);
		mAdapter.notifyItemRemoved(0);
	}

	private void startPullToRefresh() {
		mIsLoadingPrevious.set(true);
		mTopLoadingProgressBar.setIndeterminate(true);
		if (mPullToRefreshUpdateAnimation != null && mPullToRefreshUpdateAnimation.isRunning()) {
			mPullToRefreshUpdateAnimation.cancel();
		}
		int height = mTopLoadingView.getHeight();
		mPullToRefreshUpdateAnimation = ValueAnimator.ofInt(height, mLoadingViewOriginalHeight);
		mPullToRefreshUpdateAnimation.setDuration(mPullToRefreshAnimationDuration);
		mPullToRefreshUpdateAnimation.setInterpolator(mDecelerateInterpolator);
		mPullToRefreshUpdateAnimation.addUpdateListener(
				new ValueAnimator.AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator valueAnimator) {
						int val = (Integer) valueAnimator.getAnimatedValue();
						ViewGroup.LayoutParams layoutParams = mTopLoadingView.getLayoutParams();
						layoutParams.height = val;
						mTopLoadingView.setLayoutParams(layoutParams);
					}
				});
		mPullToRefreshUpdateAnimation.setDuration(mPullToRefreshAnimationDuration);
		mPullToRefreshUpdateAnimation.start();
		mLoadListener.loadPrevious();
	}

	/**
	 * @return returns true if it is loading the previous or next items.
	 */
	public boolean isLoading(){
		return mIsLoadingNext.get() || mIsLoadingPrevious.get();
	}

	/**
	 * Binds the top loading view. This is required in order to deal with the pull to refresh
	 * function.
	 *
	 * @param itemView   the view with the progress bar
	 * @param topLoading the top progress bar
	 */
	protected void bindTopLoadingView(View itemView, ProgressBar topLoading) {
		mTopLoadingView = itemView;
		mTopLoadingProgressBar = topLoading;
//		mTopCircularLoadingDrawable = (CircularLoadingDrawable)topLoading.getProgressDrawable();
		mTopLoadingView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		mLoadingViewOriginalHeight = mTopLoadingView.getMeasuredHeight();
		mTopLoadingView.getLayoutParams().height = 1;
		mTopLoadingProgressBar.setIndeterminate(false);
		mTopLoadingView.requestLayout();
		mRecyclerView.scrollBy(-1, 0);
	}

	/**
	 * Class to handle the creation of the error views. By default it does not create any error
	 * view.
	 */
	public interface ErrorViewsCreator {

		/**
		 * @param root parent view of the new view created
		 * @return Create a new error view for the top
		 */
		public View createTopErrorView(ViewGroup root);

		/**
		 * @param root parent view of the new view created
		 * @return Create a new error view for the bottom
		 */
		public View createBottomErrorView(ViewGroup root);

		/**
		 * @return true if it can create top error views
		 */
		public boolean hasTopErrorView();


		/**
		 * @return true if it can create bottom error views
		 */
		public boolean hasBottomErrorView();
	}

	/**
	 * Interface called when the user performed an action to load more elements.
	 */
	public interface LoadListener {

		/**
		 * Method called to make the initial preload. After finish the initial preloading you must
		 * call the method #finishPreloadInitial() If you do not want to use it call inside the
		 * method #finishPreloadInitial.
		 *
		 * @see #finishPreloadInitial()
		 */
		public void preloadInitial();

		/**
		 * Method called in the reset to clear the elements in the adapter.
		 *
		 * @see #reset()
		 */
		public void clearAdapter();

		/**
		 * Method called when the pull to refresh action has been performed.  After finish the
		 * loading you must call the method #finishLoadingPrevious(boolean, int)
		 *
		 * @see #finishLoadingPrevious(boolean, int)
		 */
		public void loadPrevious();


		/**
		 * Method called when the user is reaching the end of the elements and it needs to load
		 * more. After finish the loading you must call the method #finishLoadingNext(boolean, int)
		 *
		 * @see #finishLoadingNext(boolean, int)
		 */
		public void loadNext();


		/**
		 * Method called to load the first items. After finish the loading you must call the method
		 * #finishLoadingInitial(boolean, int)
		 *
		 * @see #finishLoadingInitial(boolean, int)
		 */
		public void loadInitial();
	}
}
