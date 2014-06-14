package com.example.aparkaya;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class RegisterActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
	}

	public void registra(View view) {
		Intent i = new Intent(this, AparkaYa.class );
		startActivity(i);
	}   
//hola prueba

}