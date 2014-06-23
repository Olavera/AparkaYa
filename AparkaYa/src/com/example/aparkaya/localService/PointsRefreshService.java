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

	public static final String NOTIFICATION = "com.example.aparkaya";
	private String user, pass;
	private ArrayList<WebPoint> points;
	private HttpPostAux post;
	private int t_max_en_difusion;
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
			user = extras.getString(Constants.USER);
			pass = extras.getString(Constants.PASSWORD);
			t_max_en_difusion = extras.getInt(Constants.TIEMPO_MAXIMO_EN_DIFUSION);
		}
		new asyncCallPoints().execute();	
		return mBinder;
	}
	
	@Override
	public void onCreate() {
		post = new HttpPostAux();
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new asyncCallPoints().execute();	
		return START_NOT_STICKY;
	}
	
	public ArrayList<WebPoint> getArrayPoints()
	{
		return points;
	}
	
	public void forceRefresh(){
		new asyncCallPoints().execute();
	}
	
	public void set_t_max_en_difusion(int t){
		t_max_en_difusion = t;
	}
	
	private class asyncCallPoints extends AsyncTask< Void, String, Integer > {
		
		ArrayList<WebPoint> auxpoints = new ArrayList<WebPoint>();

		protected Integer doInBackground(Void... params) {
			long limitTime;
			if(t_max_en_difusion!=0){
				// Obtenemos la hora actual y la formateamos
				Date date = new Date();
				limitTime = date.getTime() - (t_max_en_difusion*1000);
			}else{
				limitTime = 0;
			}

			/*Creamos un ArrayList del tipo nombre valor para agregar los datos recibidos por los parametros anteriores
			 * y enviarlo mediante POST a nuestro sistema para relizar la validacion*/ 
			ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();
			postparameters2send.add(new BasicNameValuePair(Constants.USER, user));
			postparameters2send.add(new BasicNameValuePair(Constants.PASSWORD, pass));
			
			//realizamos una peticion y como respuesta obtenes un array JSON
			JSONArray jdata=post.getserverdata(postparameters2send, Constants.php_obtenerPuntos);

			//si lo que obtuvimos no es null
			if (jdata!=null && jdata.length() > 0){

				try {
					for (int i = 0; i < jdata.length(); i++) {
				        JSONObject jsonObject = jdata.getJSONObject(i);
				        
				        String fechaString = jsonObject.getString(Constants.FECHA);
				        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
				        Date fecha;
				        try{
				        	if(!fechaString.equals("null"))
				        		fecha = dateFormat.parse(fechaString);
				        	else
				        		fecha = new Date();
						} catch (ParseException e) {
							fecha = null;
						}
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
			if (result == Constants.RESULT_OK){
				points = auxpoints;
			}
			Intent intent = new Intent(NOTIFICATION);
			intent.putExtra(Constants.RESULT, result);
			sendBroadcast(intent);
		}
	}

}  
