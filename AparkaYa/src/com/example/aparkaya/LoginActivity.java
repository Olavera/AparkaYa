package com.example.aparkaya;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.aparkaya.webService.HttpPostAux;

public class LoginActivity extends Activity {

	private EditText usuario, contrasenia;
	private HttpPostAux post;
	private SharedPreferences prefs;
	private String nombre_usuario, contrasenia_usuario;
	private ProgressDialog pDialog;

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		prefs = getSharedPreferences(Constants.MyPreferences, Context.MODE_PRIVATE);

		nombre_usuario = prefs.getString(Constants.USUARIO_PREFS, "");
		contrasenia_usuario = prefs.getString(Constants.CONTRASENIA_PREFS, "");

		if (nombre_usuario != "") {
			usuario.setText(nombre_usuario);
		}
		if (contrasenia_usuario != "") {
			contrasenia.setText(contrasenia_usuario);
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		post = new HttpPostAux();
		usuario = (EditText) findViewById(R.id.usuario);
		contrasenia = (EditText) findViewById(R.id.contrasenia);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void validarUsuario(View v) {
		pDialog = new ProgressDialog(LoginActivity.this);
		pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pDialog.setMessage(getResources().getString(R.string.loading));
		pDialog.setCancelable(true);
		new asynclogin().execute(usuario.getText().toString(), contrasenia
					.getText().toString());
	}

	public void registrarUsuario(View v) {
		Intent i = new Intent(this, RegisterActivity.class);
		i.putExtra("user", usuario.getText().toString());
		i.putExtra("pass", contrasenia.getText().toString());
		startActivity(i);
	}

	private class asynclogin extends AsyncTask<String, Integer, String> {

		String user, pass;

		protected String doInBackground(String... params) {
			// obtenemos user y pass
			user = params[0];
			pass = params[1];

			int id = -1;

			/*
			 * Creamos un ArrayList del tipo nombre valor para agregar los datos
			 * recibidos por los parametros anteriores y enviarlo mediante POST
			 * a nuestro sistema para relizar la validacion
			 */
			ArrayList<NameValuePair> postparameters2send = new ArrayList<NameValuePair>();

			postparameters2send.add(new BasicNameValuePair("user", user));
			postparameters2send.add(new BasicNameValuePair("password", pass));

			// realizamos una peticion y como respuesta obtenes un array JSON
			JSONArray jdata = post.getserverdata(postparameters2send,
					"http://aparkaya.webcindario.com/login.php");

			// si lo que obtuvimos no es null
			if (jdata != null && jdata.length() > 0) {

				JSONObject json_data; // creamos un objeto JSON
				try {
					json_data = jdata.getJSONObject(0); // leemos el primer
														// segmento en nuestro
														// caso el unico
					id = json_data.getInt("id");// accedemos al valor
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// validamos el valor obtenido
				if (id == 1) {
					return "ok"; // login valido
				} else {
					return "not"; // usuario no registrado
				}

			}
			return "err"; // login invalido

		}

		@Override
		protected void onPreExecute() {
			pDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					asynclogin.this.cancel(true);
				}
			});
			pDialog.show();
		}

		protected void onPostExecute(String result) {

			if (result.equals("ok")) {
				Toast.makeText(getApplicationContext(), "Login correcto",
						Toast.LENGTH_SHORT).show();
				SharedPreferences prefs = getSharedPreferences(Constants.MyPreferences, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString(Constants.USUARIO_PREFS, user);
				editor.putString(Constants.CONTRASENIA_PREFS, pass);
				editor.commit();

				// Inicia la actividad
				Intent i = new Intent(LoginActivity.this, AparkaYa.class);
				i.putExtra("user", user);
				i.putExtra("pass", pass);
				startActivity(i);
			} else if (result.equals("not")) {
				Toast.makeText(getApplicationContext(),
						"Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(),
						"No se pudo conectar con el servidor",
						Toast.LENGTH_SHORT).show();
			}
			pDialog.dismiss();
		}

	}
}
