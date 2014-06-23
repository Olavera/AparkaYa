package com.example.aparkaya.localService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.aparkaya.Constants;
import com.example.aparkaya.model.WebPoint;
import com.example.aparkaya.webService.HttpPostAux;
import com.google.android.gms.maps.model.LatLng;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.View.OnCreateContextMenuListener;
import android.widget.Toast;

public class PointsRefreshService extends Service{
	// Constante para suscribirse a los mensajes de broadcast
	public static final String NOTIFICATION = "com.example.aparkaya";
	// Creedenciales del usuario registrado
	private String user, pass;
	// Lista de puntos que se refresca períodicamente del servidor web
	private ArrayList<WebPoint> points;
	// Clase para el manejo de HttpPOST y JSON
	private HttpPostAux post;
	// Variable de menu de opciones tiempo maximo desde
	// que se difundio
	private int t_max_en_difusion;
	// Binder que se devuelve a la actividad al iniciar el servicio
	// con onBind()
	private final IBinder mBinder = new MyBinder();

	public class MyBinder extends Binder {
		public PointsRefreshService getService() {
			return PointsRefreshService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Bundle extras = intent.getExtras();
		if (extras != null) {
			// Recuperamos de extras los valores que necesitaremos
			// para hacer las peticiones al servicio
			user = extras.getString(Constants.USER);
			pass = extras.getString(Constants.PASSWORD);
			t_max_en_difusion = extras.getInt(Constants.TIEMPO_MAXIMO_EN_DIFUSION);
		}
		// Se ejecuta la primera llamada al servicio web
		//para que refresque los puntos
		new asyncCallPoints().execute();	
		return mBinder;
	}
	
	@Override
	public void onCreate() {
		post = new HttpPostAux();
		super.onCreate();
	}

	/**
	 * Se llama a este metodo cada vez que se intenta iniciar el servicio y ya esta creado
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Pide refrescar al servicio web
		new asyncCallPoints().execute();	
		// Devolvemos este flag para que el servicio no intente regenerar llamadas 
		// por su cuenta sino que se espere a alarm o a un refreco manual
		return START_NOT_STICKY;
	}
	
	/**
	 * Devuelve la lista de puntos obtenida en la ultima llamada al servicio web
	 */
	public ArrayList<WebPoint> getArrayPoints()
	{
		return points;
	}
	
	public void manualRefresh(){
		new asyncCallPoints().execute();
	}
	
	public void set_t_max_en_difusion(int t){
		t_max_en_difusion = t;
	}
	
	
	private class asyncCallPoints extends AsyncTask< Void, String, Integer > {
		
		ArrayList<WebPoint> auxpoints = new ArrayList<WebPoint>();

		protected Integer doInBackground(Void... params) {
			// Variable para calcular el tiempo limite para mostrar marcadores 
			// en funcion de el tiempo de difusion maximo seleccionado
			long limitTime;
			if(t_max_en_difusion!=0){
				// Obtenemos la hora actual y la formateamos
				Date date = new Date();
				limitTime = date.getTime() - (t_max_en_difusion*1000);
			}else{
				limitTime = 0;
			}

			// Creamos un ArrayList del tipo clave-valor y agregamos los valores necesarios
			ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();
			postparameters2send.add(new BasicNameValuePair(Constants.USER, user));
			postparameters2send.add(new BasicNameValuePair(Constants.PASSWORD, pass));
			
			// Realiza una peticion enviando los datos mediante el metodo POST 
			// y obtiene como respuesta un array JSON
			JSONArray jdata=post.getserverdata(postparameters2send, Constants.php_obtenerPuntos);

			// Si no obtenemos una respuesta nula de la direccion parseamos la informacion
			// En este caso la informacion devuelta sera solo un identificador con el resultado de la consulta
			if (jdata!=null && jdata.length() > 0){

				try {
					// Obtenemos uno a uno los objetos JSON
					for (int i = 0; i < jdata.length(); i++) {
				        JSONObject jsonObject = jdata.getJSONObject(i);
				        
				        // Creamos una 
				        String fechaString = jsonObject.getString(Constants.FECHA);
				        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
				        Date fecha;
				        //Intentamos parsear la fecha
				        try{
				        	if(!fechaString.equals("null"))
				        		fecha = dateFormat.parse(fechaString);
				        	else
				        		fecha = new Date();
						} catch (ParseException e) {
							fecha = null;
						}
				        // Añadimos el objeto si no excede el limite de tiempo
				        if (fecha.getTime()>limitTime)
				        	auxpoints.add(new WebPoint(jsonObject.getInt(Constants.ID_PUNTO),
				        							jsonObject.getString(Constants.USUARIO), 
				        							new LatLng(jsonObject.getDouble(Constants.LATITUD), 
				        									jsonObject.getDouble(Constants.LONGITUD)),
				        							jsonObject.getInt(Constants.REPUTACION),
				        							fecha));
					}
				    
				}
				catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					try {
						JSONObject json_data = jdata.getJSONObject(0);
															
						int id = json_data.getInt("id");

						if (id == 2)
						{
							return Constants.RESULT_NOTUSER;
						}
					} catch (JSONException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}  
				}
				return Constants.RESULT_OK; //lista de puntos obtenida correctamente
			}     
			return Constants.RESULT_ERR; //error
		}

		protected void onPostExecute(Integer result) {
			// Si el servicio obtuvo la lista con existo remplaza la antigua por la nueva
			if (result == Constants.RESULT_OK){
				points = auxpoints;
			}
			// Notifica mediante broadcast que acaba ha terminado
			// la llamada al servicio web y esta actualizado
			Intent intent = new Intent(NOTIFICATION);
			intent.putExtra(Constants.RESULT, result);
			sendBroadcast(intent);
		}
	}

}  
