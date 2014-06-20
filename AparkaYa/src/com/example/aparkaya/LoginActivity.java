package com.example.aparkaya;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class LoginActivity extends Activity {

	private EditText usuario, contrasenia;
	HttpPostAux post;
	

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
		new asynclogin().execute(usuario.getText().toString(),contrasenia.getText().toString());
	}

	public void registrarUsuario(View v) {
		Intent i = new Intent(this, RegisterActivity.class );
        startActivity(i);
	}

	private class asynclogin extends AsyncTask< String, String, String > {

		String user,pass;

		protected String doInBackground(String... params) {
			//obtenemos user y pass
			user=params[0];
			pass=params[1];

			int id=-1;

			/*Creamos un ArrayList del tipo nombre valor para agregar los datos recibidos por los parametros anteriores
			 * y enviarlo mediante POST a nuestro sistema para relizar la validacion*/ 
			ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();

			postparameters2send.add(new BasicNameValuePair("user",user));
			postparameters2send.add(new BasicNameValuePair("password",pass));

			//realizamos una peticion y como respuesta obtenes un array JSON
			JSONArray jdata=post.getserverdata(postparameters2send, "http://aparkaya.webcindario.com/login.php");

			//si lo que obtuvimos no es null
			if (jdata!=null && jdata.length() > 0){

				JSONObject json_data; //creamos un objeto JSON
				try {
					json_data = jdata.getJSONObject(0); //leemos el primer segmento en nuestro caso el unico
					id=json_data.getInt("id");//accedemos al valor 
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}              

				//validamos el valor obtenido
				if (id==1){           
					return "ok"; //login valido
				}
				else{
					return "not"; //usuario no registrado
				}

			}     
			return "err"; //login invalido 

		}

		protected void onPostExecute(String result) {

			if (result.equals("ok")){
				Toast.makeText(getApplicationContext(),"Logueado correctamente", Toast.LENGTH_SHORT).show();

				//Inicia la actividad
				Intent i = new Intent(LoginActivity.this, AparkaYa.class);
				i.putExtra("user", user);
				i.putExtra("pass", pass);
				startActivity(i);
			} 
			else if(result.equals("not")){
				Toast.makeText(getApplicationContext(),"Usuario no registrado", Toast.LENGTH_SHORT).show();
			}
			else{
				Toast.makeText(getApplicationContext(),"No se pudo conectar con el servidor", Toast.LENGTH_SHORT).show();
			}
		}    

	}
}
