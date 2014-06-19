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

public class PointsRefreshService extends Service{
    
    //Este es el objeto que recibe interacciones de los clientes. 
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
    }
 
}  
