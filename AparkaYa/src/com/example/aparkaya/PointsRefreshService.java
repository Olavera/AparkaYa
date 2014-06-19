package com.example.aparkaya;

import java.util.ArrayList;
import java.util.Vector;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

public class PointsRefreshService extends Service{
    
  /*  //Este es el objeto que recibe interacciones de los clientes. 
    private final IBinder mBinder = new LocalBinder();
    
    private static final long UPDATE_INTERVAL = 5000;
	private String user, pass;
	
	private HttpPostAux post;

    private Messenger outMessenger;
    
    //Clase de acceso para los clientes. 
    //Como sabemos que este servicio siempre se ejecuta en el mismo proceso,
    //no es necesario tratar con IPC
    public class LocalBinder extends Binder{    
    	PointsRefreshService getService(){
            return PointsRefreshService.this;
        }
    }
    
    @Override
    public void onCreate(){
    	post = new HttpPostAux();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags,int startId){
		user = intent.getStringExtra("user");;
		pass = intent.getStringExtra("pass");;
        //Queremos que el servicio continúe ejecutándose hasta que es explícitamente parado,
		//así que devolvemos sticky
        return START_STICKY;    
    }

    @Override
    public IBinder onBind(Intent intent){
    	Bundle extras = intent.getExtras();
    	// Get messager from the Activity
    	if (extras != null) {
    		outMessenger = (Messenger) extras.get("MESSENGER");
    	}
        return mBinder; 
    }*/

	public static final String NOTIFICATION = "com.example.aparkaya";
	private Messenger outMessenger;
	private Vector<Punto> points;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void onCreate() {
		Toast.makeText(getApplicationContext(), "Service Created", Toast.LENGTH_SHORT).show();
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), "Service Destroy", Toast.LENGTH_SHORT).show();
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Bundle extras = intent.getExtras();
		if (extras!=null){
			outMessenger = (Messenger) extras.get("MESSENGER");
		}
		Message backMsg = Message.obtain();
		backMsg.arg1 = Activity.RESULT_OK;
		Bundle bundle = new Bundle();
		bundle.putString("txt", "Prueba");
		backMsg.setData(bundle);
		try {
			if (outMessenger != null)
				outMessenger.send(backMsg);
		} catch (android.os.RemoteException e1) {
			Log.w(getClass().getName(), "Excepción enviando mensaje", e1);
		}
		//publishResults("Prueba");
		return super.onStartCommand(intent, flags, startId);
	}
	
	public Vector<Punto> getVectorPoints()
	{
		return points;
	}
	
	private class asyncCallPoints extends AsyncTask< Void, String, String > {
		
		Vector<Punto> auxpoints = new Vector<Punto>();

		protected String doInBackground(Void... params) {

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
							return "notUser";
						}
					} catch (JSONException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				}              
				return "ok"; //lista de puntos obtenida correctamente

			}     
			return "err"; //error

		}

		protected void onPostExecute(String result) {

			if (result.equals("ok")){
				Toast.makeText(getApplicationContext(),"Puntos obtenidos correctamente", Toast.LENGTH_SHORT).show();
				points = auxpoints;//Sustituimos la lista de puntos antigua por la nueva
				mapa.clear();

				for (Punto punto : points) {
					if (punto.getNombre().equals("Coche"))
						mapa.addMarker(new MarkerOptions()
						.position(punto.getCords())
						.title(punto.getNombre())
						.snippet(punto.getNombre())
						.icon(BitmapDescriptorFactory
								.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
					else
						mapa.addMarker(new MarkerOptions()
						.position(punto.getCords())
						.title(punto.getNombre())
						.snippet(punto.getNombre())
						.icon(BitmapDescriptorFactory
								.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
				}
			}
			else if (result.equals("notUser")){
				Toast.makeText(getApplicationContext(),"Usuario no reconocido", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(getApplicationContext(),"No se pudieron obtener los puntos", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	
	
	
/*
	private void publishResults(String result){
		Intent intent = new Intent(NOTIFICATION);
		intent.putExtra("RESULT", result);
		sendBroadcast(intent);
	}*/

}  
