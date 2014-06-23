package com.example.aparkaya;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aparkaya.webService.HttpPostAux;

public class InfoCuenta extends Activity {
	// Objeto para la conexion http
	private HttpPostAux post;
	// Variables de sesion
	private String user, pass;
	// Dialogo de progreso que se muestra cuando el usuario debe
	// esperar un proceso del programa antes de seguir interactuando
	private ProgressDialog pDialog;
	// Textview correspondientes a los campos donde se muestra
	// la informacion de la cuenta recuperada del servidor
	private TextView tvuser, tvemail, tvrep;
	
	
	/**
	 * Creamos un cuadro de diálogo que contiene una vista asociada
	 * a la información de la cuenta del usuario, donde referenciamos
	 * al Dialog todos los elementos de la vista para mostrar. Finalmente
	 * ejecutamos el método asíncrono que devuelve la información asociada
	 * al usuario actual
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Genera la vista como un tipo dialogo
		Dialog dialog = new Dialog(this, R.style.newDialog);
		// Configura las opciones del dialogo
		dialog.setContentView(R.layout.info_cuenta);
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setOnKeyListener(new Dialog.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface arg0, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					finish();
				}
				return true;
			}
		});

		// Creamos el objeto HttpPostAux para las llamas al servidor web
		post = new HttpPostAux();
		user = getIntent().getExtras().getString(Constants.USER);
		pass = getIntent().getExtras().getString(Constants.PASSWORD);

		tvuser = (TextView) dialog.findViewById(R.id.tvContUser_info);
		tvemail = (TextView) dialog.findViewById(R.id.tvContEmail_info);
		tvrep = (TextView) dialog.findViewById(R.id.tvContRep_info);

		// Mostramos el dialogo de informacion
		dialog.show();
		iniciarProgressDialog();
		// Ejecutamos el proceso asincrono para recuperar los datos del servidor
		new asyncAccountInfo().execute();
	}

	/**
	 * Inicia el dialogo de progreso
	 */
	public void iniciarProgressDialog() {
		pDialog = new ProgressDialog(InfoCuenta.this);
		pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pDialog.setMessage(getResources().getString(R.string.loading));
		pDialog.setCancelable(true);
	}

	private class asyncAccountInfo extends AsyncTask<Void, Integer, Integer> {

		String email;
		int rep;

		protected Integer doInBackground(Void... params) {

			int id = -1;
			// Creamos un ArrayList del tipo clave-valor y agregamos los valores
			// necesarios
			ArrayList<NameValuePair> postparameters2send = new ArrayList<NameValuePair>();
			postparameters2send.add(new BasicNameValuePair(Constants.USER, user));
			postparameters2send.add(new BasicNameValuePair(Constants.PASSWORD, pass));

			// Realiza una peticion enviando los datos mediante el metodo POST
			// y obtiene como respuesta un array JSON
			JSONArray jdata = post.getserverdata(postparameters2send,
					Constants.php_obtenerInfoCuenta);

			// Si no obtenemos una respuesta nula de la direccion parseamos la
			// informacion
			if (jdata != null && jdata.length() > 0) {
				// Obtenemos el primer objeto que es el unico que nos deberia devolver el servidor,
				// y intentamos recuperar la informacion
				try {
					
					JSONObject json_data = jdata.getJSONObject(0);
					email = json_data.getString(Constants.EMAIL);
					rep = json_data.getInt(Constants.REPUTACION);
					return Constants.RESULT_OK;
				// Si falla intentamos recuperar el identificador de fallos de autentificacion de usuario
				} catch (JSONException e) {
					try {
						JSONObject json_data = jdata.getJSONObject(0);
						id = json_data.getInt(Constants.ID);

						if (id == 2) {
							return Constants.RESULT_NOTUSER;
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
			return Constants.RESULT_ERR; 
		}

		@Override
		protected void onPreExecute() {
			pDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					asyncAccountInfo.this.cancel(true);
				}
			});
			pDialog.show();
		}
		
		/**
		 * Método que ejecuta e introduce en los campos de la vista asociada.
		 * En caso de fallo mostramos mensaje de error
		 */

		protected void onPostExecute(Integer result) {
			pDialog.dismiss();
			if (result == Constants.RESULT_OK) {
				// Introducimos los campos recuperados de la base de datos en el dialog
				tvuser.setText(user);
				tvemail.setText(email);
				tvrep.setText(Integer.toString(rep));
			} else if (result == Constants.RESULT_NOTUSER) {
				finish();
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.err_cant_find_user), Toast.LENGTH_SHORT).show();
			} else {
				finish();
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.err_cant_connect),
						Toast.LENGTH_SHORT).show();
			}
		}

	}
}
