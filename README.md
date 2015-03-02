# Android Loading Helper

This is a set of classes which help to perform pull to refresh and endless scrolling in a recycler view.

![Demo GIF](http://raw.github.com/jorgemf/android-loading-helper/master/misc/loadinghelper.gif)

There is a sample application under example.

In order to use them add the project to your build.gradle

```Gradle
dependencies {
    compile 'com.livae:android-loadingHelper:1.2.3'
}
```

And then in your fragment:

```Java
public class MyFragment extends Fragment implements LoadingHelper.LoadListener {

	private MyAdapter mAdapter;

	private LoadingHelper<MyViewHolder> mLoadingHelper;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_loading, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
		ContentLoadingProgressBar contentLoadingProgressBar =
		        (ContentLoadingProgressBar) view.findViewById(R.id.center_progressbar);
		mAdapter = new MyAdapter();
		mLoadingHelper = new LoadingHelper<MyViewHolder>(
				getActivity(), recyclerView, mAdapter, this, contentLoadingProgressBar,
				new LoadingHelper.ErrorViewsCreator() {

					@Override
					public View createTopErrorView(ViewGroup root) {
					    // return an error view
					}

					@Override
					public View createBottomErrorView(ViewGroup root) {
					    // return an error view
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
		// this enables the initial loading, pull to refresh and endless loading
		mLoadingHelper.enableInitialProgressLoading(true);
		mLoadingHelper.enableEndlessLoading(true);
		mLoadingHelper.enablePullToRefreshUpdate(true);
		// this sets the colors for the pull to refresh loading
        mLoadingHelper.setColorCircularLoading(Color.DKGRAY);
        mLoadingHelper.setColorCircularLoadingActive(Color.GREEN);
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        // this sets a header and footer view for the list
        mLoadingHelper.setHeaderView(layoutInflater.inflate(R.layout.header, recyclerView, false));
        mLoadingHelper.setFooterView(layoutInflater.inflate(R.layout.footer, recyclerView, false));
        // starts the loading helper
		mLoadingHelper.start();
	}

	@Override
	public void onResume() {
		super.onResume();
		mLoadingHelper.onResume();
	}

	@Override
	public void loadInitial() {
	    // load initial data in the background, make the first network request
	    // when finished call: mLoadingHelper.finishLoadInitial();
	}

	@Override
	public void loadNext() {
	    // load next data in the background (endless loading)
	    // when finished call: mLoadingHelper.finishLoadingNext();
	}

	@Override
	public void loadPrevious() {
	    // load the previous data in the background (pull to refresh)
	    // when finished call: mLoadingHelper.finishLoadingPrevious();
	}

	@Override
	public void clearAdapter() {
		mAdapter.clear();
	}
}
```

## License

    Copyright 2014, 2015 Jorge Muñoz Fuentes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
   
