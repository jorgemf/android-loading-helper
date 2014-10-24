package com.jorgemf.android.loading;

import android.animation.ValueAnimator;
import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;

import com.jorgemf.android.view.R;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class LoadingFragment<k extends RecyclerView.ViewHolder> extends Fragment implements View.OnTouchListener {

	private static final int INVALID_POINTER = -1;

	private static final int PROGRESS_BAR_MAX = 1000;

	private RecyclerView mRecyclerView;

	private LinearLayoutManager mLayoutManager;

	private RecyclerAdapter mAdapter;

	private ContentLoadingProgressBar mContentLoadingProgressBar;

	private boolean mEnableInitialProgressLoading = false;

	private boolean mEnabledPullToRefresUpdate = false;

	private boolean mEnableEndlessLoading = false;

	private int mEndlessLoadingPreloadAhead = 0;

	private View mTopLoadingView;

	private View mBottomLoadingView;

	private ProgressBar mTopLoadingProgressBar;

	private AtomicBoolean mIsLoadingNext = new AtomicBoolean(false);

	private AtomicBoolean mIsLoadingPrevious = new AtomicBoolean(false);

	private float mPullToRefreshInitialY;

	private int mPullToRefreshDistance;

	private int mPullToRefreshAnimationDuration;

	private int mLoadingViewOriginalHeight;

	private ValueAnimator mPullToRefreshUpdateAnimation;

	private int mActivePointerId = INVALID_POINTER;

	private final DecelerateInterpolator mDecelerateInterpolator = new DecelerateInterpolator(2f);

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mTopLoadingView = inflater.inflate(R.layout.view_loading, container, false);
		mBottomLoadingView = inflater.inflate(R.layout.view_loading, container, false);
		return inflater.inflate(R.layout.fragment_loading, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
		mContentLoadingProgressBar = (ContentLoadingProgressBar) view.findViewById(R.id.center_progressbar);
		mTopLoadingProgressBar = (ProgressBar) mTopLoadingView.findViewById(R.id.loading_progress_bar);
		mTopLoadingProgressBar.setMax(PROGRESS_BAR_MAX);

		mTopLoadingView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		mLoadingViewOriginalHeight = mTopLoadingView.getMeasuredHeight();

		// use this setting to improve performance if you know that changes
		// in content do not change the layout size of the RecyclerView
		mRecyclerView.setHasFixedSize(true);

		// use a linear layout manager
		mLayoutManager = onCreateLayoutManager();
		mRecyclerView.setLayoutManager(mLayoutManager);

		// specify the adapter
		mAdapter = new RecyclerAdapter<k>(onCreateAdapter(),
				mTopLoadingView, mBottomLoadingView,
				getTopErrorView(), getBottomErrorView());
		mRecyclerView.setAdapter(mAdapter);

		if (mEnableInitialProgressLoading) {
			mContentLoadingProgressBar.setVisibility(View.VISIBLE);
		} else {
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
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Resources resources = getResources();
		mPullToRefreshDistance = resources.getDimensionPixelSize(R.dimen.pull_refresh_distance);
		mPullToRefreshAnimationDuration = resources.getInteger(android.R.integer.config_mediumAnimTime);
		reset();
	}

	@Override
	public void onResume() {
		super.onResume();
		mPullToRefreshInitialY = -1;
	}

	protected void enableInitialProgressLoading(boolean enable) {
		mEnableInitialProgressLoading = enable;
	}

	protected void enablePullToRefreshUpdate(boolean enable) {
		mEnabledPullToRefresUpdate = enable;
	}

	protected void enableEndlessLoading(boolean enable) {
		mEnableEndlessLoading = enable;
	}

	protected void endlessLoadingPreloadAhead(int numberOfElements) {
		if (numberOfElements < 0) {
			numberOfElements = 0;
		}
		mEndlessLoadingPreloadAhead = numberOfElements;
	}

	public void preloadInitial() {
		finishPreloadInitial();
	}

	protected LinearLayoutManager onCreateLayoutManager() {
		return new LinearLayoutManager(getActivity());
	}

	public abstract RecyclerView.Adapter<k> onCreateAdapter();

	protected abstract void clearAdapter();

	public abstract void loadPrevious();

	public abstract void loadNext();

	public abstract void loadInitial();

	protected abstract View getTopErrorView();

	protected abstract View getBottomErrorView();

	protected synchronized void finishPreloadInitial() {
		mIsLoadingNext.set(true);
		loadInitial();
	}

	protected synchronized void finishPullToRefreshUpdate(boolean showTopErrorView, int dataInserted) {
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

	protected synchronized void finishEndlessLoading(boolean showBottomErrorView, int dataInserted) {
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

	protected synchronized void finishInitialLoading(boolean showTopErrorView, int dataInserted) {
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

	protected synchronized void reset() {
		if (mEnableInitialProgressLoading) {
			mContentLoadingProgressBar.show();
		}
		int itemCount;
		if (mAdapter != null && (itemCount = mAdapter.getItemCount()) > 0) {
			mAdapter.showTopLoading(false);
			mAdapter.showTopError(false);
			mAdapter.showBottomLoading(false);
			mAdapter.showBottomError(false);
			clearAdapter();
			mAdapter.notifyItemRangeRemoved(0, itemCount);
		}
		mIsLoadingPrevious.set(false);
		mIsLoadingNext.set(true);
		preloadInitial();
	}

	private void checkLoadNext() {
		if (mEnableEndlessLoading) {
			mRecyclerView.requestLayout();
			mRecyclerView.post(new Runnable() {
				@Override
				public void run() {
					int lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition();
					if (lastVisibleItemPosition + mEndlessLoadingPreloadAhead >= mAdapter.getItemCount() - 1 && !mIsLoadingNext.get()) {
						mIsLoadingNext.set(true);
						mAdapter.showBottomLoading(true);
						if (mAdapter.isShowBottomError()) {
							mAdapter.showBottomError(false);
							int pos = mAdapter.getItemCount() - 1;
							mAdapter.notifyItemRemoved(pos);
							mAdapter.notifyItemInserted(pos);
						} else {
							mAdapter.notifyItemInserted(mAdapter.getItemCount() - 1);
						}
						loadNext();
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
								System.out.println("move diff: " + diff);
								if (diff > 0) {
									setPullToRefresh(diff);
									return true;
								} else {
									System.out.println("cancelPullToRefresh diff");
									mPullToRefreshInitialY = -1;
									cancelPullToRefresh();
									return false;
								}
							} else {
								mPullToRefreshInitialY = event.getY();
								mActivePointerId = pointer;
								System.out.println("move START: " + mPullToRefreshInitialY);
								initPullToRefresh();
								return false;
							}
//							break;
						case MotionEvent.ACTION_CANCEL:
						case MotionEvent.ACTION_UP:
							System.out.println("move STOP: ");
							if (mPullToRefreshInitialY != -1) {
								float diff = event.getY() - mPullToRefreshInitialY;
								mPullToRefreshInitialY = -1;
								if (diff > mPullToRefreshDistance) {
									System.out.println("startPullToRefresh");
									startPullToRefresh();
								} else {
									System.out.println("cancelPullToRefresh");
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
		mAdapter.showTopLoading(true);
		mTopLoadingView.getLayoutParams().height = 1;
		mRecyclerView.scrollBy(-1, 0);
		mTopLoadingView.requestLayout();
		if (mAdapter.isShowTopError()) {
			mAdapter.showTopError(false);
			mAdapter.notifyItemRemoved(0);
		}
		mTopLoadingProgressBar.setIndeterminate(false);
		mAdapter.notifyItemInserted(0);
	}

	private void setPullToRefresh(float displacement) {
		if (displacement > mLoadingViewOriginalHeight) {
			displacement = mLoadingViewOriginalHeight + (displacement - mLoadingViewOriginalHeight) / 2;
		}
		float ratioPull = displacement / mPullToRefreshDistance;
		if (ratioPull > 1) {
			ratioPull = 1;
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
		mTopLoadingProgressBar.setAlpha(ratioDisplacement);
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
		mPullToRefreshUpdateAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				float val = (Float) valueAnimator.getAnimatedValue();
				mTopLoadingProgressBar.setAlpha(val);
			}
		});
		mPullToRefreshUpdateAnimation.setDuration(mPullToRefreshAnimationDuration);
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
		mPullToRefreshUpdateAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
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
		loadPrevious();
	}

}
