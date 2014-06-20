package com.example.aparkaya;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.example.aparkaya.model.WebPoint;
import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.TextView;

public class DetailsDialog extends Activity {
	WebPoint wp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details_dialog);
		
		
		String fechaString = getIntent().getExtras().getString(Constants.FECHA);
		SimpleDateFormat dateFormat0 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
		SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM", Locale.getDefault());
		SimpleDateFormat dateFormat2 = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date fecha;
        try{
        	fecha = dateFormat0.parse(fechaString);
		} catch (ParseException e) {
			fecha = null;
		}
		wp = new WebPoint(
			getIntent().getExtras().getInt(Constants.ID_PUNTO),	
			getIntent().getExtras().getString(Constants.USUARIO),
			new LatLng(
					getIntent().getExtras().getDouble(Constants.LONGITUD),
					getIntent().getExtras().getDouble(Constants.LATITUD)),
			getIntent().getExtras().getInt(Constants.REPUTACION),
			fecha);
		
		
		TextView tvContUser = (TextView)findViewById(R.id.tvContUser);
		TextView tvFecha = (TextView)findViewById(R.id.tvContFecha);
		TextView tvContLongitude = (TextView)findViewById(R.id.tvContLongitude);
		TextView tvContLatitude = (TextView)findViewById(R.id.tvContLatitude);
		TextView tvContReputation = (TextView)findViewById(R.id.tvContReputation);
		
		DecimalFormat df = new DecimalFormat("#.######");
		tvContUser.setText(wp.getUsuario());
		tvFecha.setText(" a las " + dateFormat2.format(wp.getFecha()) + " el " + dateFormat1.format(wp.getFecha()));
		tvContLongitude.setText(df.format(wp.getCords().longitude));
		tvContLatitude.setText(df.format(wp.getCords().latitude));
		tvContReputation.setText(Integer.toString(wp.getReputacion()));
	}

}
