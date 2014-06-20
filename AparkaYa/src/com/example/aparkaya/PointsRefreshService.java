package com.example.aparkaya;

import java.util.ArrayList;
import java.util.Vector;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.widget.Toast;

public class PointsRefreshService extends Service{

	public static final String NOTIFICATION = "com.example.aparkaya";
	private String user, pass;
	private Messenger outMessenger;
	private Vector<Punto> points;
	private HttpPostAux post;
	private final IBinder mBinder = new MyBinder();

	public class MyBinder extends Binder {
		PointsRefreshService getService() {
			return PointsRefreshService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Bundle extras = intent.getExtras();
		// Get messenger from the Activity
		if (extras != null) {
			outMessenger = (Messenger) extras.get("MESSENGER");
			user = extras.getString("user");
			pass = extras.getString("pass");
		}
		// Return our messenger to the Activity to get commands
		return mBinder;
	}

	@Override
	public void onCreate() {
		post = new HttpPostAux();
		Toast.makeText(getApplicationContext(), "Service Created", Toast.LENGTH_SHORT).show();
		super.onCreate();
		new asyncCallPoints().execute();	
	}

	@Override
	public void onDestroy() {
		Toast.makeText(getApplicationContext(), "Service Destroy", Toast.LENGTH_SHORT).show();
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		new asyncCallPoints().execute();	
		//publishResults("Prueba");
		return super.onStartCommand(intent, flags, startId);
	}
	
	public Vector<Punto> getVectorPoints()
	{
		return points;
	}
	
	private class asyncCallPoints extends AsyncTask< Void, String, Integer > {
		
		Vector<Punto> auxpoints = new Vector<Punto>();

		protected Integer doInBackground(Void... params) {

			/*Creamos un ArrayList del tipo nombre valor para agregar los datos recibidos por los parametros anteriores
			 * y enviarlo mediante POST a nuestro sistema para relizar la validacion*/ 
			ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();
			postparameters2send.add(new BasicNameValuePair("user",user));
			postparameters2send.add(new BasicNameValuePair("password",pass));
			
			//realizamos una peticion y como respuesta obtenes un array JSON
			JSONArray jdata=post.getserverdata(postparameters2send, "http://aparkaya.webcindario.com/obtenerPuntos.php");

			//si lo que obtuvimos no es null
			if (jdata!=null && jdata.length() > 0){

				try {
					for (int i = 0; i < jdata.length(); i++) {
				        JSONObject jsonObject = jdata.getJSONObject(i);
				     
				        auxpoints.add(new Punto(jsonObject.getString("id_usuario"), 
				    		new LatLng(jsonObject.getDouble("latitud"), 
				    				jsonObject.getDouble("longitud")),
				    				jsonObject.getInt("libre")));
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
			Message backMsg = Message.obtain();
			backMsg.arg1 = result;
			try {
				if (outMessenger != null)
					outMessenger.send(backMsg);
			} catch (android.os.RemoteException e1) {
				Log.w(getClass().getName(), "Excepción enviando mensaje", e1);
			}
		}
	}

}  
