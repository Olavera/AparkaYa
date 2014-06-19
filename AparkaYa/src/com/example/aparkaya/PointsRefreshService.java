package com.example.aparkaya;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
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
    
    @Override
    public IBinder onBind(Intent intent) {
           // TODO: Return the communication channel to the service.
           throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
           // TODO Auto-generated method stub

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
           publishResults("Prueba");
           return super.onStartCommand(intent, flags, startId);
    }
    
    private void publishResults(String result){
    	Intent intent = new Intent(NOTIFICATION);
    	intent.putExtra("RESULT", result);
    	sendBroadcast(intent);
    }
 
}  
