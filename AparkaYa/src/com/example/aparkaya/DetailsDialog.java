package com.example.aparkaya;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aparkaya.model.WebPoint;
import com.example.aparkaya.webService.HttpPostAux;
import com.google.android.gms.maps.model.LatLng;

public class DetailsDialog extends Activity {
	
	//Referencia del punto
	private WebPoint wp;
	// Objeto para la conexion http
	private HttpPostAux post;
	//Variables de sesión
	private String user, pass;
	// Dialogo de progreso que se muestra cuando el usuario debe
	// esperar un proceso del programa antes de seguir interactuando
	private ProgressDialog pDialog;
	
	/**
	 * Se crea un cuadro de diálogo que tiene contenida una vista
	 * a la que añadimos todos los elementos descritos en el layout
	 * Para los botones se comprueba si es el usuario logueado o
	 * es otro usuario distinto para deshabilitar botones.Finalmente,
	 * se muestra el cuadro de diálogo.
	 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Dialog dialog = new Dialog(this, R.style.newDialog);
		
		dialog.setContentView(R.layout.details_dialog);
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
				
		post = new HttpPostAux();
		user = getIntent().getStringExtra("user");
		pass = getIntent().getStringExtra("pass");

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
		
		ImageButton btnAparcar = (ImageButton) dialog.findViewById(R.id.btnOcupar);
		btnAparcar.setOnClickListener(new Aparcar());
		ImageButton btnPositivo = (ImageButton) dialog.findViewById(R.id.btnVotarPositivo);
		btnPositivo.setOnClickListener(new votarPositivo());
		ImageButton btnNegativo = (ImageButton) dialog.findViewById(R.id.btnVotarNegativo);
		btnNegativo.setOnClickListener(new votarNegativo());
		ImageButton btnEliminar = (ImageButton) dialog.findViewById(R.id.btnEliminar);
		btnEliminar.setOnClickListener(new Eliminar());
		
		if(user.equalsIgnoreCase(wp.getUsuario()))
		{
			btnAparcar.setEnabled(false);
			btnPositivo.setEnabled(false);
			btnNegativo.setEnabled(false);
		}
		else
		{
			btnEliminar.setEnabled(false);
		}
		
		TextView tvContUser = (TextView)dialog.findViewById(R.id.tvContUser);
		TextView tvFecha = (TextView)dialog.findViewById(R.id.tvContFecha);
		TextView tvContLongitude = (TextView)dialog.findViewById(R.id.tvContLongitude);
		TextView tvContLatitude = (TextView)dialog.findViewById(R.id.tvContLatitude);
		TextView tvContReputation = (TextView)dialog.findViewById(R.id.tvContReputation);
		
		DecimalFormat df = new DecimalFormat("#.######");
		tvContUser.setText(wp.getUsuario());
		tvFecha.setText(" a las " + dateFormat2.format(wp.getFecha()) + " el " + dateFormat1.format(wp.getFecha()));
		tvContLongitude.setText(df.format(wp.getCords().longitude));
		tvContLatitude.setText(df.format(wp.getCords().latitude));
		tvContReputation.setText(Integer.toString(wp.getReputacion()));
		
		dialog.show();
	}
	
	/**
	 * Método para mostrar un ProgressDialog al usuario de Procesando...
	 * para que el usuario no mande varias peticiones y mejorar la 
	 * usabilidad de la aplicación
	 */
	
	public void iniciarProgressDialog(){
		pDialog = new ProgressDialog(DetailsDialog.this);
		pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pDialog.setMessage(getResources().getString(R.string.loading));
		pDialog.setCancelable(true);
	}
	
	// ---------------- LISTENERS PARA LOS BOTONES DEL DIALOG ----------------
	/**
	 * Todos hacen lo mismo, iniciar el progress dialog y llamar al proceso asincrono
	 * que hace la peticion al servidor.
	 * Pero cada uno pasa en la llama el tipo de accion que corresponde a su boton.
	 */
	
	public class Aparcar implements OnClickListener {
		@Override
		public void onClick(View v) {
			iniciarProgressDialog();
			new asyncPointActions().execute(Constants.ACTION_PARK);
		}
	}
	
	public class votarPositivo implements OnClickListener {
		@Override
		public void onClick(View v) {
			iniciarProgressDialog();
			new asyncPointActions().execute(Constants.ACTION_VOTE_POSSITIVE);
		}
	}
	
	public class votarNegativo implements OnClickListener {
		@Override
		public void onClick(View v) {
			iniciarProgressDialog();
			new asyncPointActions().execute(Constants.ACTION_VOTE_NEGATIVE);
		}
	}
	
	public class Eliminar implements OnClickListener {
		@Override
		public void onClick(View v) {
			iniciarProgressDialog();
			new asyncPointActions().execute(Constants.ACTION_DELETE);
		}
	}
	
	/**
	 * Método asíncrono que envía los datos al php que se encarga de
	 * la acción ejecutada de cada botón
	 *
	 */
	
	private class asyncPointActions extends AsyncTask< Integer, Integer, Integer > {

		protected Integer doInBackground(Integer... params) {
			//obtenemos user y pass
			int action = params[0];

			int id=-1;

			ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();
			postparameters2send.add(new BasicNameValuePair(Constants.USER,user));
			postparameters2send.add(new BasicNameValuePair(Constants.PASSWORD,pass));
			postparameters2send.add(new BasicNameValuePair(Constants.ACTION, Integer.toString(action)));
			postparameters2send.add(new BasicNameValuePair(Constants.ID_PUNTO, Integer.toString(wp.getId_punto())));

			//realizamos una peticion y como respuesta obtenes un array JSON
			JSONArray jdata=post.getserverdata(postparameters2send, Constants.php_cambiarPuntos);

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
					return Constants.RESULT_OK; //login valido
				}
				else{
					return Constants.RESULT_FAILED_UPDATE_POINT; //usuario no registrado
				}

			}     
			return Constants.RESULT_ERR; //login invalido 
		}
		
		@Override
		protected void onPreExecute() {
			pDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					asyncPointActions.this.cancel(true);
				}
			});
			pDialog.show();
		}
		

		protected void onPostExecute(Integer result) {
			pDialog.dismiss();
			if (result==Constants.RESULT_OK){
				Toast.makeText(getApplicationContext(),"Punto actualizado", Toast.LENGTH_SHORT).show();
				Intent returnIntent = new Intent();
				setResult(Constants.RESULT_CODE_RETURN_DETAILS_DIALOG, returnIntent);
				finish();
			} 
			else if(result==Constants.RESULT_FAILED_UPDATE_POINT){
				Toast.makeText(getApplicationContext(),"Error al actualizar punto", Toast.LENGTH_SHORT).show();
			}
			else{
				Toast.makeText(getApplicationContext(),"No se pudo conectar con el servidor", Toast.LENGTH_SHORT).show();
			}
		}    

	}
	

}
