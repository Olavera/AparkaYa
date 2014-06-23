package com.example.aparkaya;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.aparkaya.webService.HttpPostAux;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {

	//Campos del formulario para introducir el usuario
	private EditText usuario, email, contrasenia1, contrasenia2;
	// Objeto para la conexion http
	private HttpPostAux post;
	// Textview correspondientes a los campos donde se muestra
	// la informacion de la cuenta recuperada del servidor
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
	 * Al crear activity añadimos el contenido de la vista
	 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		post = new HttpPostAux();

		usuario = (EditText) findViewById(R.id.usuario_registro);
		usuario.setText(getIntent().getStringExtra("user"));
		email = (EditText) findViewById(R.id.email_registro);
		contrasenia1 = (EditText) findViewById(R.id.contrasenia_registro);
		contrasenia1.setText(getIntent().getStringExtra("pass"));
		contrasenia2 = (EditText) findViewById(R.id.contrasenia2_registro);

	}
	
	/**
	 * Método invocado al pulsar el botón Confirmar que comprueba las 
	 * contraseñas introducidas, si son iguales mostramos ProgressDialog
	 * y ejecutamos el método asícrono que realiza el registro
	 */

	public void Registra(View view) {
		if (!contrasenia1.getText().toString().equals(contrasenia2.getText().toString())) {
			Toast.makeText(getApplicationContext(),
					"Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
		} else {// pasamos los posibles filtros tambien podemos comprobar si el
				// usuario ya esta registrado
			pDialog = new ProgressDialog(RegisterActivity.this);
			pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pDialog.setMessage(getResources().getString(R.string.loading));
			pDialog.setCancelable(true);
			new asyncRegister().execute(email.getText().toString(), usuario
					.getText().toString(), contrasenia1.getText().toString());
		}

	}
	
	/**
	 * Método asíncrono en segundo plano que ejecuta llamada Post para
	 * recibir unos flags en formato JSON.
	 */

	private class asyncRegister extends AsyncTask<String, Integer, Integer> {
		String email, user, pass;

		protected Integer doInBackground(String... params) {
			// obtenemos user y pass
			email = params[0];
			user = params[1];
			pass = params[2];

			int id = -1;

			/*
			 * Creamos un ArrayList del tipo nombre valor para agregar los datos
			 * recibidos por los parametros anteriores y enviarlo mediante POST
			 * a nuestro sistema para relizar la validacion
			 */
			ArrayList<NameValuePair> postparameters2send = new ArrayList<NameValuePair>();

			postparameters2send.add(new BasicNameValuePair("email", email));
			postparameters2send.add(new BasicNameValuePair("user", user));
			postparameters2send.add(new BasicNameValuePair("password", pass));

			// realizamos una peticion y como respuesta obtenes un array JSON
			JSONArray jdata = post.getserverdata(postparameters2send, Constants.php_registro);

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
					return Constants.RESULT_OK; // registro valido
				} else if (id == 2) {
					return Constants.RESULT_USER_EXISTS; // usuario ya existe
				} else if (id == 3) {
					return Constants.RESULT_EMAIL_EXISTS; // email ya existe
				}
			}
			return Constants.RESULT_ERR; // registro invalido

		}

		@Override
		protected void onPreExecute() {
			pDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					asyncRegister.this.cancel(true);
				}
			});
			pDialog.show();
		}
		
		/**
		 * Al realizar Post si el resultado si el resultado OK
		 * guardamos en mis preferencias el usuario y la 
		 * contraseña y lanzamos la actividad de AparkaYa.
		 * Eliminamos el ProgressDialog
		 */

		protected void onPostExecute(Integer result) {

			if (result == Constants.RESULT_OK) {
				Toast.makeText(getApplicationContext(),
						"Registrado correctamente", Toast.LENGTH_SHORT).show();

				SharedPreferences prefs = getSharedPreferences(Constants.MyPreferences, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString(Constants.USUARIO_PREFS, user);
				editor.putString(Constants.CONTRASENIA_PREFS, pass);
				editor.commit();
				
				// Inicia la actividad
				Intent i = new Intent(RegisterActivity.this, AparkaYa.class);
				i.putExtra("user", user);
				i.putExtra("pass", pass);
				startActivity(i);
			} else if (result == Constants.RESULT_USER_EXISTS) {
				Toast.makeText(getApplicationContext(),
						"Nombre de usuario ya en uso", Toast.LENGTH_SHORT)
						.show();
			} else if (result == Constants.RESULT_EMAIL_EXISTS) {
				Toast.makeText(getApplicationContext(), "Email ya en uso",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(),
						"No se pudo conectar con el servidor",
						Toast.LENGTH_SHORT).show();
			}
			pDialog.dismiss();
		}

	}
}