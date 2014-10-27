package com.jorgemf.android.view;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jorgemf.android.loading.LoadingFragment;

public class ConcreteFragment extends LoadingFragment {

	private FakeAdapter mFakeAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		enableInitialProgressLoading(true);
		enableEndlessLoading(true);
		enablePullToRefreshUpdate(true);
	}

	@Override
	public RecyclerView.Adapter onCreateAdapter() {
		mFakeAdapter = new FakeAdapter();
		return mFakeAdapter;
	}

	@Override
	public void loadPrevious() {
		AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				super.onPostExecute(aVoid);
				mFakeAdapter.preadd(3);
				finishPullToRefreshUpdate(false, 3);
			}
		};
		asyncTask.execute();
	}

	@Override
	public void loadNext() {
		AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				super.onPostExecute(aVoid);
				mFakeAdapter.add(4);
				finishEndlessLoading(false, 4);
			}
		};
		asyncTask.execute();
	}

	@Override
	public void loadInitial() {
		AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				super.onPostExecute(aVoid);
				mFakeAdapter.add(27);
				finishInitialLoading(false, 27);
			}
		};
		asyncTask.execute();
	}

	@Override
	protected void clearAdapter() {
		mFakeAdapter.clear();
	}

	class FakeViewHolder extends RecyclerView.ViewHolder {

		public TextView textView;

		public FakeViewHolder(View itemView) {
			super(itemView);
			textView = (TextView) itemView;
			textView.setPadding(120, 40, 120, 40);
		}
	}

	class FakeAdapter extends RecyclerView.Adapter<FakeViewHolder> {

		private int mCount = 0;

		private int mPreadd = 0;

		@Override
		public FakeViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
			return new FakeViewHolder(new TextView(viewGroup.getContext()));
		}

		@Override
		public void onBindViewHolder(FakeViewHolder viewHolder, int position) {
			viewHolder.textView.setText(Integer.toString(position) + "  " + Integer.toString(position - mPreadd) + "  -" + Integer.toString(mPreadd));
		}

		public void preadd(int quantity) {
			mPreadd += quantity;
			mCount += quantity;
		}

		public void add(int quantity) {
			mCount += quantity;
		}

		@Override
		public int getItemCount() {
			return mCount;
		}

		public void clear() {
			mCount = 0;
			mPreadd = 0;
		}
	}


}
