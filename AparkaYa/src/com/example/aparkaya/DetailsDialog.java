package com.example.aparkaya;

import java.text.DecimalFormat;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

public class DetailsDialog extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details_dialog);
		
		String user = getIntent().getExtras().getString(Constants.USUARIO);
		double longitude = getIntent().getExtras().getDouble(Constants.LONGITUD);
		double latitude = getIntent().getExtras().getDouble(Constants.LATITUD);
		String reputation = getIntent().getExtras().getString(Constants.REPUTACION);
		
		TextView tvContUser = (TextView)findViewById(R.id.tvContUser);
		TextView tvContLongitude = (TextView)findViewById(R.id.tvContLongitude);
		TextView tvContLatitude = (TextView)findViewById(R.id.tvContLatitude);
		TextView tvContReputation = (TextView)findViewById(R.id.tvContReputation);
		
		DecimalFormat df = new DecimalFormat("#.######");
		tvContUser.setText(user);
		tvContLongitude.setText(df.format(longitude));
		tvContLatitude.setText(df.format(latitude));
		tvContReputation.setText(reputation);
	}

}
