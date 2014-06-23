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
	
	/**
	 * Al iniciar actividad miramos en las preferencias si existe 
	 * un usario guardado previamente para recoger sus datos.
	 */

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
	
	/**
	 * Añadimos la vista a nuestra actividad y sus elementos
	 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		post = new HttpPostAux();
		usuario = (EditText) findViewById(R.id.usuario);
		contrasenia = (EditText) findViewById(R.id.contrasenia);
	}

	/**
	 * Método invocado cuando se pincha el botón de Login
	 * mostramos un ProgressDialog para que el usuario no mande varias
	 * peticiones de eventos y ejecutamos el método asíncrono para 
	 * validar el usuario 
	 */

	public void validarUsuario(View v) {
		pDialog = new ProgressDialog(LoginActivity.this);
		pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pDialog.setMessage(getResources().getString(R.string.loading));
		pDialog.setCancelable(true);
		new asynclogin().execute(usuario.getText().toString(), contrasenia
					.getText().toString());
	}
	
	/**
	 * Método invocado cuando se pincha en el botón de Registro
	 * que te envía mediante un Intent a otra Activity y en la que
	 * pasamos con putExtra el usuario y la contreseña
	 */

	public void registrarUsuario(View v) {
		Intent i = new Intent(this, RegisterActivity.class);
		i.putExtra("user", usuario.getText().toString());
		i.putExtra("pass", contrasenia.getText().toString());
		startActivity(i);
	}
	
	/**
	 * Método asíncrono en segundo plano que procesa el login
	 */

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
			JSONArray jdata = post.getserverdata(postparameters2send, Constants.php_login);

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
		
		/**
		 * Método que devuelve el resultado del Post.
		 * Si todo OK guardamos en las preferencias los valores
		 * introducidos previamente por el usuario y lanzamos la
		 * activity de AparkaYa. En caso de error mostramos un 
		 * mensaje de error tipo Toast
		 */

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
