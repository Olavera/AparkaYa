package com.example.aparkaya;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {

	private EditText usuario, contrasenia1, contrasenia2;
	HttpPostAux post;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);		
		post = new HttpPostAux();

		usuario = (EditText) findViewById(R.id.usuario);
		contrasenia1 = (EditText) findViewById(R.id.contrasenia1);
		contrasenia2 = (EditText) findViewById(R.id.contrasenia2);

	}

	public void Registra(View view) {

		if (contrasenia1.getText().equals(contrasenia2.getText()) ) {
		
			Toast.makeText(getApplicationContext(),"Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
		} else {//pasamos los posibles filtros tambien podemos comprobar si el usuario ya esta registrado
			new asyncRegister().execute(usuario.getText().toString(),contrasenia1.getText().toString());
		}

	}

	private class asyncRegister extends AsyncTask<String, String, String> {

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
			JSONArray jdata = post.getserverdata(postparameters2send,"http://padandroid.webcindario.com/index2.php");

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

		protected void onPostExecute(String result) {

			if (result.equals("ok")){
				Toast.makeText(getApplicationContext(),"Registrado correctamente", Toast.LENGTH_SHORT).show();

				//Inicia la actividad
				Intent i = new Intent(RegisterActivity.this, AparkaYa.class);
				startActivity(i);
			} 
			else if(result.equals("not")){
				Toast.makeText(getApplicationContext(),"Nombre de usuario ya en uso", Toast.LENGTH_SHORT).show();
			}
			else{
				Toast.makeText(getApplicationContext(),"No se pudo conectar con el servidor", Toast.LENGTH_SHORT).show();
			}
		}  

	}
}