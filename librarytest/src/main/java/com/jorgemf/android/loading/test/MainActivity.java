package com.jorgemf.android.loading.test;

import android.app.Activity;
import android.os.Bundle;

import com.jorgemf.android.loading.test.R;


public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new ConcreteFragment())
					.commit();
		}
	}

}
