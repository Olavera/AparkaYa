package com.example.aparkaya;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

public class DetailsDialog extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details_dialog);
		
		String user = getIntent().getExtras().getString(Constants.USER);
		double longitude = getIntent().getExtras().getDouble(Constants.LONGITUDE);
		double latitude = getIntent().getExtras().getDouble(Constants.LATITUDE);
		String reputation = getIntent().getExtras().getString(Constants.REPUTATION);
		
		TextView tvContUser = (TextView)findViewById(R.id.tvContUser);
		TextView tvContLongitude = (TextView)findViewById(R.id.tvContLongitude);
		TextView tvContLatitude = (TextView)findViewById(R.id.tvContLatitude);
		TextView tvContReputation = (TextView)findViewById(R.id.tvContReputation);
		
		tvContUser.setText(user);
		tvContLongitude.setText(Double.toString(longitude));
		tvContLatitude.setText(Double.toString(latitude));
		tvContReputation.setText(reputation);
	}

}
