package com.livae.android.loading.test;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.livae.android.loading.LoadingHelper;

public class ConcreteFragment extends Fragment implements LoadingHelper.LoadListener {

    private FakeAdapter mFakeAdapter;

    private LoadingHelper<FakeViewHolder> mLoadingHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_loading, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        ContentLoadingProgressBar contentLoadingProgressBar = (ContentLoadingProgressBar) view
                .findViewById(R.id.center_progressbar);
        mFakeAdapter = new FakeAdapter();
        mLoadingHelper = new LoadingHelper<FakeViewHolder>(
                getActivity(), recyclerView, mFakeAdapter, this, contentLoadingProgressBar,
                new LoadingHelper.ErrorViewsCreator() {

                    @Override
                    public View createTopErrorView(ViewGroup root) {
                        TextView textView = new TextView(root.getContext());
                        textView.setText("top error loading, try again");
                        return textView;

                    }

                    @Override
                    public View createBottomErrorView(ViewGroup root) {
                        TextView textView = new TextView(root.getContext());
                        textView.setText("bottom error loading, waiting for trying later");
                        return textView;
                    }

                    @Override
                    public boolean hasTopErrorView() {
                        return true;
                    }

                    @Override
                    public boolean hasBottomErrorView() {
                        return true;
                    }
                });
        mLoadingHelper.enableInitialProgressLoading(true);
        mLoadingHelper.enableEndlessLoading(true);
        mLoadingHelper.enablePullToRefreshUpdate(true);
        mLoadingHelper.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        mLoadingHelper.onResume();
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
                if (Math.random() < 0.5) {
                    mLoadingHelper.finishLoadingPrevious(true, 0);
                } else {
                    mFakeAdapter.preadd(3);
                    mLoadingHelper.finishLoadingPrevious(false, 3);
                }
                mLoadingHelper.enableEndlessLoading(true);
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
                if (Math.random() < 0.1) {
                    mLoadingHelper.enableEndlessLoading(false);
                    mLoadingHelper.finishLoadingNext(true, 0, true);
                } else {
                    mFakeAdapter.add(4);
                    mLoadingHelper.finishLoadingNext(false, 4, true);
                }
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
                mLoadingHelper.finishLoadingInitial(false, 27, true);
            }
        };
        asyncTask.execute();
    }

    @Override
    public void preloadInitial() {
        mLoadingHelper.finishPreloadInitial();
    }

    @Override
    public void clearAdapter() {
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
            viewHolder.textView.setText(Integer.toString(position)
                    + "  " + Integer.toString(position - mPreadd)
                    + "  -" + Integer.toString(mPreadd));
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
