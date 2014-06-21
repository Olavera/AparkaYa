package com.example.aparkaya;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aparkaya.localService.PointsRefreshService;
import com.example.aparkaya.model.EnumTypeRefreshAction;
import com.example.aparkaya.model.RefreshAction;
import com.example.aparkaya.model.WebPoint;
import com.example.aparkaya.parser.ParserXML_DOM;
import com.example.aparkaya.webService.HttpPostAux;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

@SuppressLint("ValidFragment")
public class AparkaYa extends ActionBarActivity implements
		ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	private HttpPostAux post;
	private String user, pass;
	private Messenger messenger;
	private PointsRefreshService localService;
	private HashMap<String, WebPoint> hashmap_idMarker_WebPoint;
	private SparseArray<String> array_idPoint_idMarker;
	private GoogleMap mapa = null;
	private ListView lstListado;
	private AdaptadorPuntos adapter;
	private ArrayList<WebPoint> listpoints;

	private String t_refresco = "";
	private String area_busqueda = "";
	private String ordenar_por = "";

	private SharedPreferences prefs;

	private Handler handler = new Handler() {
		public void handleMessage(Message message) {
			repaintPoints(message.arg1, localService.getArrayPoints());
		}
	};

	private MyServiceConnection mConnection;

	public class MyServiceConnection implements ServiceConnection {
		public void onServiceConnected(ComponentName className, IBinder binder) {
			PointsRefreshService.MyBinder b = (PointsRefreshService.MyBinder) binder;
			localService = b.getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			localService.onDestroy();
			localService = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the action bar.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.

		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		post = new HttpPostAux();
		user = getIntent().getStringExtra("user");
		pass = getIntent().getStringExtra("pass");
		hashmap_idMarker_WebPoint = new HashMap<String, WebPoint>();
		array_idPoint_idMarker = new SparseArray<String>();
		listpoints = new ArrayList<WebPoint>();
		adapter = new AdaptadorPuntos(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mapa != null) {
			if (mapa.getMyLocation() != null)
				mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(
						new LatLng(mapa.getMyLocation().getLatitude(), mapa
								.getMyLocation().getLongitude()), 15));
			else
				Toast.makeText(getApplicationContext(), "Esperando ubicacion",
						Toast.LENGTH_SHORT).show();
		}

		// SHARED PREFERENCES

		prefs = getSharedPreferences("MisPreferencias", Context.MODE_PRIVATE);

		t_refresco = prefs.getString(Constants.TIEMPO_REFRESCO, "5 segundos");
		area_busqueda = prefs.getString(Constants.AREA_BUSQUEDA, "500 metros");
		ordenar_por = prefs.getString(Constants.ORDENAR_POR, "nombre");
		Toast.makeText(getApplicationContext(), "Preferencias:"  + t_refresco + " | " + area_busqueda + " | " + ordenar_por, Toast.LENGTH_LONG)
				.show();

		// -----------------------------------------------------------------------

		mConnection = new MyServiceConnection();

		messenger = new Messenger(handler);

		// Start service using AlarmManager
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, 10);
		Intent intt = new Intent(this, PointsRefreshService.class);
		intt.putExtra("MESSENGER", messenger);
		PendingIntent pintent = PendingIntent.getService(this, 0, intt,
				PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				cal.getTimeInMillis(), Constants.LOCALSERVER_TIME_REFRESH,
				pintent);

		Intent intent = new Intent(this, PointsRefreshService.class);
		intent.putExtra("MESSENGER", messenger);
		intent.putExtra("user", user);
		intent.putExtra("pass", pass);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();

		// SHARED PREFERENCES

		SharedPreferences prefs = getSharedPreferences("MisPreferencias",
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(Constants.TIEMPO_REFRESCO, t_refresco);

		editor.putString(Constants.AREA_BUSQUEDA, area_busqueda);
		editor.putString(Constants.ORDENAR_POR, ordenar_por);
		editor.commit();

		// ----------------------------------------------------------

		Intent intt = new Intent(this, PointsRefreshService.class);
		intt.putExtra("MESSENGER", messenger);
		PendingIntent pintent = PendingIntent.getService(this, 0, intt,
				PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarm.cancel(pintent);

		unbindService(mConnection);
		mConnection = null;
		localService.onDestroy();
	}

	private void repaintPoints(int result, ArrayList<WebPoint> auxpoints) {
		
		if (result == Constants.RESULT_OK) {
			Toast.makeText(getApplicationContext(), "Strart refresh",
					Toast.LENGTH_SHORT).show();
			new asyncSendPoint().execute(auxpoints);
		} else if (result == Constants.RESULT_NOTUSER) {
			Toast.makeText(getApplicationContext(), "Usuario no reconocido",
					Toast.LENGTH_SHORT).show();
		} else if (result == Constants.RESULT_ERR) {
			Toast.makeText(getApplicationContext(),
					"Error al actualizar información", Toast.LENGTH_SHORT)
					.show();
		}
		
	}
	
	private class asyncSendPoint extends AsyncTask<ArrayList<WebPoint>, String, Integer> {

		// Lista de acciones de refresco que se ejecutaran sobre los puntos del mapa
		// en la fase de PostExecute
		ArrayList<RefreshAction> refreshActions;

		protected Integer doInBackground(ArrayList<WebPoint>... params) {
			ArrayList<WebPoint> auxpoints = params[0];
			refreshActions = new ArrayList<RefreshAction>();
			
			SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM",
					Locale.getDefault());
			SimpleDateFormat dateFormat2 = new SimpleDateFormat("HH:mm",
					Locale.getDefault());
			Date datenow = new Date();
			long timeUmbral = datenow.getTime() - Constants.TIME_UMBRAL;
			
			SparseArray<String> copyOf_idPoint_idMarker = new SparseArray<String>();
			
			int key = 0;
			for(int i = 0; i < array_idPoint_idMarker.size(); i++) {
			   key = array_idPoint_idMarker.keyAt(i);
			   copyOf_idPoint_idMarker.put(key, array_idPoint_idMarker.get(key));
			}
			
			for (WebPoint punto : auxpoints) {
				int franja = obtenerFranja(punto.getFecha());
				String textDate = "No se obtuvo fecha";
				if (punto.getFecha().getTime() < timeUmbral)
					textDate = "Difundido el: "
							+ dateFormat1.format(punto.getFecha());
				else {
					textDate = "Difundido a las "
							+ dateFormat2.format(punto.getFecha());
				}
				
				String idMarker = copyOf_idPoint_idMarker.get(punto.getId_punto(), Constants.POINT_NO_MAPPED);
				if (idMarker.equals(Constants.POINT_NO_MAPPED))
				{
					refreshActions.add(new RefreshAction(punto, EnumTypeRefreshAction.ADD, franja, textDate, idMarker));
				}
				else
				{
					WebPoint oldP = hashmap_idMarker_WebPoint.get(idMarker);
					Marker m = oldP.getMarker();
					punto.setMarker(m);
					if(/*((m.getPosition().latitude!=punto.getCords().latitude) ||
							(m.getPosition().longitude!=punto.getCords().longitude)) ||*/
							(obtenerFranja(oldP.getFecha()) != franja))
					{

						refreshActions.add(new RefreshAction(punto, EnumTypeRefreshAction.UPDATE, franja, textDate, idMarker));
						hashmap_idMarker_WebPoint.put(idMarker, punto);
					}
					else if(oldP.getReputacion() != punto.getReputacion()){
						
					}
					copyOf_idPoint_idMarker.remove(punto.getId_punto());
				}
			}
			
			key = 0;
			for(int i = 0; i < copyOf_idPoint_idMarker.size(); i++) {
			   key = copyOf_idPoint_idMarker.keyAt(i);
			   refreshActions.add(new RefreshAction( hashmap_idMarker_WebPoint.get(key)//new WebPoint(key, "", null, 0, null)
			   , EnumTypeRefreshAction.REMOVE, 0, "", array_idPoint_idMarker.get(key)));
			}
			
			return Constants.RESULT_OK;
		}
		
		protected void onPostExecute(Integer result) {
			for (RefreshAction act : refreshActions)
			{
				if(act.getTypeAction()==EnumTypeRefreshAction.ADD){
					Marker marker = mapa
							.addMarker(new MarkerOptions()
									.position(act.getnewPointInfo().getCords())
									.title(act.getTextDate())
									.snippet(
											act.getnewPointInfo().getUsuario()
													+ " ("
													+ Integer.toString(act.getnewPointInfo()
															.getReputacion()) + ")")
									.icon(BitmapDescriptorFactory.defaultMarker(obtenerIconoFranja(act.getFranja()))));
					String key = marker.getId();
					WebPoint punto = act.getnewPointInfo();
					punto.setMarker(marker);
					hashmap_idMarker_WebPoint.put(key, punto);
					array_idPoint_idMarker.put(punto.getId_punto(), key);
					listpoints.add(punto);
				}else if(act.getTypeAction()==EnumTypeRefreshAction.UPDATE){
					WebPoint newP = act.getnewPointInfo();
					Marker m = newP.getMarker();
					//m.setPosition(newP.getCords());
					m.setIcon(BitmapDescriptorFactory.defaultMarker(obtenerIconoFranja(act.getFranja())));
					m.setTitle(act.getTextDate());
				}else if(act.getTypeAction()==EnumTypeRefreshAction.REMOVE){
					String idMarker = array_idPoint_idMarker.get(act.getnewPointInfo().getId_punto());
					array_idPoint_idMarker.remove(act.getnewPointInfo().getId_punto());
					hashmap_idMarker_WebPoint.get(idMarker).getMarker().remove();
					hashmap_idMarker_WebPoint.remove(idMarker);
					listpoints.remove(act.getnewPointInfo());
				}
			}
			//Collections.sort(listpoints, new Id_punto_Comparator());
			adapter.notifyDataSetChanged();
			Toast.makeText(getApplicationContext(), "Stop refresh", Toast.LENGTH_SHORT).show();
		}
	}
	
	class Id_punto_Comparator implements Comparator<WebPoint> {
		@Override
		public int compare(WebPoint p1, WebPoint p2) {
			return Integer.valueOf(p1.getId_punto()).compareTo(
					Integer.valueOf(p2.getId_punto()));
		}

	}
	
	public int obtenerFranja(Date d){
		int franja;
		long dateNow = new Date().getTime();
		long datePoint = d.getTime();
		
		if (datePoint < (dateNow - Constants.TIME_UMBRAL)){
			franja = 0;
		}else if (datePoint < (dateNow - Constants.TIME_BAD)){
			franja = 1;
		}else if (datePoint < (dateNow - Constants.TIME_REGULAR)){
			franja = 2;
		}else if (datePoint < (dateNow - Constants.TIME_GOOD)){
			franja = 3;
		}else{
			franja = 4;
		}
		
		return franja;
	}
	
	public float obtenerIconoFranja(int franja){
		float color;
		
		switch (franja){
			case 0: color = BitmapDescriptorFactory.HUE_VIOLET; break;
			case 1: color = BitmapDescriptorFactory.HUE_RED; break;
			case 2: color = BitmapDescriptorFactory.HUE_ORANGE; break;
			case 3: color = BitmapDescriptorFactory.HUE_YELLOW; break;
			case 4: color = BitmapDescriptorFactory.HUE_GREEN; break;
			default: color = BitmapDescriptorFactory.HUE_ROSE; break;
		}
		return color;
	}

	

	class AdaptadorPuntos extends ArrayAdapter<WebPoint> {
		Activity context;

		public AdaptadorPuntos(Activity act) {
			super(act, R.layout.vista_punto, listpoints);
			this.context = act;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = context.getLayoutInflater();
			View v = inflater.inflate(R.layout.vista_punto, null);

			// Creamos un objeto directivo
			WebPoint punto = listpoints.get(position);
			// Rellenamos el nombre
			TextView nombre = (TextView) v.findViewById(R.id.textNameList);
			nombre.setText(punto.getId_punto() + ": " + punto.getUsuario()
					+ " (" + punto.getReputacion() + ")");
			// Rellenamos el cargo
			TextView coordenadas = (TextView) v.findViewById(R.id.textLatLng);
			coordenadas.setText(punto.getCords().toString());

			return v;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_opciones_aparkaya, menu);
		return true;
	}

	// código para cada opción de menú
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int segundos = 0;
		int distancia_busqueda = 0;
		String ordenado_por = "";

		switch (item.getItemId()) {
		case R.id.t_refresco_5:
			t_refresco = "5 segundos";
			segundos = 5;
			TiempoRefresco(segundos);
			return true;
		case R.id.t_refresco_10:
			t_refresco = "10 segundos";
			segundos = 10;
			TiempoRefresco(segundos);
			return true;
		case R.id.t_refresco_15:
			t_refresco = "15 segundos";
			segundos = 15;
			TiempoRefresco(segundos);
			return true;
		case R.id.area_500m:
			distancia_busqueda = 500;
			area_busqueda = "500 metros";
			AreaBusqueda(distancia_busqueda);
			return true;
		case R.id.area_1km:
			distancia_busqueda = 1000;
			area_busqueda = "1 km";
			AreaBusqueda(distancia_busqueda);
			return true;
		case R.id.ordena_nombre:
			ordenado_por = "nombre";
			ordenar_por = "nombre";
			menuOrdenar(ordenado_por);
			return true;
		case R.id.ordena_distancia:
			ordenado_por = "distancia";
			ordenar_por = "distancia";
			menuOrdenar(ordenado_por);
			return true;
		case R.id.info_cuenta:
			menuInfoCuenta();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void menuInfoCuenta() {

		Intent i = new Intent(this, InfoCuenta.class);
		startActivity(i);

	}

	public void TiempoRefresco(int segundos_refresco) {

		Toast.makeText(getApplicationContext(), t_refresco, Toast.LENGTH_SHORT)
				.show();

		// FALTA QUE CAMBIE EL INTERVALO DE LA ALARMA PARA EL REFRESCO

	}

	public void AreaBusqueda(int distancia_busqueda) {

		Toast.makeText(getApplicationContext(), area_busqueda,
				Toast.LENGTH_SHORT).show();
		// FALTA HACER EL PHP para ordenar segun la distancia pasada

	}

	public void menuOrdenar(String ordenador_por) {

		Toast.makeText(getApplicationContext(), ordenar_por, Toast.LENGTH_SHORT)
				.show();
		// Falta hacer php para ordenar segun lo que le pases

	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			Fragment fragment;

			if (position == 0) {
				fragment = new FragmentoMapa();
			} else {
				fragment = new FragmentoHuecos();
			}
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 2 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.Mapa).toUpperCase(l);
			case 1:
				return getString(R.string.Huecos).toUpperCase(l);
			}
			return null;
		}
	}

	public class FragmentoMapa extends Fragment implements
			OnMapLongClickListener, OnInfoWindowClickListener {

		private Button save, difundir;

		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */

		@SuppressLint("ValidFragment")
		public FragmentoMapa() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			View rootView = inflater.inflate(R.layout.fragment_mapa, container,
					false);
			save = (Button) rootView.findViewById(R.id.btnguardar);
			save.setOnClickListener(new btnGuardarListener());

			difundir = (Button) rootView.findViewById(R.id.btndifundir);
			difundir.setOnClickListener(new btnDifundir());

			initilizeMap();
			return rootView;
		}

		/**
		 * function to load map. If map is not created it will create it for you
		 */
		private void initilizeMap() {

			if (mapa == null) {
				mapa = ((SupportMapFragment) getSupportFragmentManager()
						.findFragmentById(R.id.map)).getMap();

				// check if map is created successfully or not
				if (mapa == null) {
					Toast.makeText(getApplicationContext(),
							"Sorry! unable to create maps", Toast.LENGTH_SHORT)
							.show();
				} else {
					mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
					mapa.setMyLocationEnabled(true);
					mapa.getUiSettings().setZoomControlsEnabled(false);
					mapa.getUiSettings().setCompassEnabled(true);
					mapa.setOnInfoWindowClickListener(this);
					mapa.setOnMapLongClickListener(this);
				}

			}
		}

		private void iniciarTask() {
			// RetrieveFeed task = new RetrieveFeed();
			// task.execute();
		}

		@Override
		public void onMapLongClick(LatLng puntoPulsado) {
			/*
			 * ParserXML_DOM parser = new
			 * ParserXML_DOM(getApplicationContext());
			 * 
			 * parser.guardarPunto("prueba", puntoPulsado);
			 * 
			 * iniciarTask();
			 */
			new asyncSendPoint().execute(puntoPulsado);
		}

		@Override
		public void onInfoWindowClick(Marker marker) {
			Intent intent = new Intent(AparkaYa.this, DetailsDialog.class);
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"dd-MM-yyyy HH:mm:ss", Locale.getDefault());
			WebPoint p = hashmap_idMarker_WebPoint.get(marker.getId());
			intent.putExtra("user", user);
			intent.putExtra("pass", pass);
			intent.putExtra(Constants.ID_PUNTO, p.getId_punto());
			intent.putExtra(Constants.USUARIO, p.getUsuario());
			intent.putExtra(Constants.LATITUD, p.getCords().latitude);
			intent.putExtra(Constants.LONGITUD, p.getCords().longitude);
			intent.putExtra(Constants.REPUTACION, p.getReputacion());
			intent.putExtra(Constants.FECHA, dateFormat.format(p.getFecha()));
			startActivity(intent);
		}

		public class btnGuardarListener implements OnClickListener {
			@Override
			public void onClick(View v) {
				ParserXML_DOM parser = new ParserXML_DOM(
						getApplicationContext());

				parser.guardarPunto("Coche",
						new LatLng(mapa.getCameraPosition().target.latitude,
								mapa.getCameraPosition().target.longitude));

				iniciarTask();
			}
		}

		public class btnDifundir implements OnClickListener {
			@Override
			public void onClick(View v) {
				if (mapa.getMyLocation() != null)
					new asyncSendPoint().execute(new LatLng(mapa
							.getMyLocation().getLatitude(), mapa
							.getMyLocation().getLongitude()));
				else
					Toast.makeText(getApplicationContext(),
							"Ubicación GPS no detectada", Toast.LENGTH_SHORT)
							.show();
			}
		}

		private class asyncSendPoint extends AsyncTask<LatLng, String, String> {

			protected String doInBackground(LatLng... params) {

				LatLng ll = params[0];
				int id = -1;
				/*
				 * Creamos un ArrayList del tipo nombre valor para agregar los
				 * datos recibidos por los parametros anteriores y enviarlo
				 * mediante POST a nuestro sistema para relizar la validacion
				 */
				ArrayList<NameValuePair> postparameters2send = new ArrayList<NameValuePair>();

				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss", Locale.getDefault());
				Date date = new Date();
				String fecha = dateFormat.format(date);

				postparameters2send.add(new BasicNameValuePair("user", user));
				postparameters2send
						.add(new BasicNameValuePair("password", pass));
				postparameters2send.add(new BasicNameValuePair("latitud",
						Double.toString(ll.latitude)));
				postparameters2send.add(new BasicNameValuePair("longitud",
						Double.toString(ll.longitude)));
				postparameters2send.add(new BasicNameValuePair("fecha", fecha));

				// realizamos una peticion y como respuesta obtenes un array
				// JSON
				JSONArray jdata = post.getserverdata(postparameters2send,
						"http://aparkaya.webcindario.com/enviarPunto.php");

				// si lo que obtuvimos no es null
				if (jdata != null && jdata.length() > 0) {

					JSONObject json_data; // creamos un objeto JSON
					try {
						json_data = jdata.getJSONObject(0); // leemos el primer
															// segmento en
															// nuestro
															// caso el unico
						id = json_data.getInt("id");// accedemos al valor
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// validamos el valor obtenido
					if (id == 1) {
						return "ok"; //
					} else if (id == 2) {
						return "notUser";
					}
				}
				return "err"; //

			}

			protected void onPostExecute(String result) {

				if (result.equals("ok")) {
					Toast.makeText(getApplicationContext(),
							"Punto enviado correctamente", Toast.LENGTH_SHORT)
							.show();
				} else if (result.equals("notUser")) {
					Toast.makeText(getApplicationContext(),
							"Usuario no reconocido", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(),
							"Fallo al enviar el punto", Toast.LENGTH_SHORT)
							.show();
				}
			}
		}
		/*
		 * private class asyncCallPoints extends AsyncTask< Void, String, String
		 * > {
		 * 
		 * Vector<Punto> auxpoints = new Vector<Punto>();
		 * 
		 * protected String doInBackground(Void... params) {
		 * 
		 * ArrayList<NameValuePair> postparameters2send= new
		 * ArrayList<NameValuePair>(); postparameters2send.add(new
		 * BasicNameValuePair("user",user)); postparameters2send.add(new
		 * BasicNameValuePair("password",pass));
		 * 
		 * //realizamos una peticion y como respuesta obtenes un array JSON
		 * JSONArray jdata=post.getserverdata(postparameters2send,
		 * "http://aparkaya.webcindario.com/obtenerPuntos.php");
		 * 
		 * //si lo que obtuvimos no es null if (jdata!=null && jdata.length() >
		 * 0){
		 * 
		 * try { for (int i = 0; i < jdata.length(); i++) { JSONObject
		 * jsonObject = jdata.getJSONObject(i);
		 * 
		 * auxpoints.add(new Punto(jsonObject.getString("id_usuario"), new
		 * LatLng(jsonObject.getDouble("latitud"),
		 * jsonObject.getDouble("longitud")), jsonObject.getInt("libre"))); }
		 * 
		 * } catch (JSONException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); try { JSONObject json_data =
		 * jdata.getJSONObject(0);
		 * 
		 * int id = json_data.getInt("id");
		 * 
		 * if (id == 2) { return "notUser"; } } catch (JSONException e2) { //
		 * TODO Auto-generated catch block e2.printStackTrace(); } } return
		 * "ok"; //lista de puntos obtenida correctamente
		 * 
		 * } return "err"; //error
		 * 
		 * }
		 * 
		 * protected void onPostExecute(String result) {
		 * 
		 * if (result.equals("ok")){
		 * Toast.makeText(getApplicationContext(),"Puntos obtenidos correctamente"
		 * , Toast.LENGTH_SHORT).show(); points = auxpoints;//Sustituimos la
		 * lista de puntos antigua por la nueva mapa.clear();
		 * 
		 * for (Punto punto : points) { if (punto.getNombre().equals("Coche"))
		 * mapa.addMarker(new MarkerOptions() .position(punto.getCords())
		 * .title(punto.getNombre()) .snippet(punto.getNombre())
		 * .icon(BitmapDescriptorFactory
		 * .defaultMarker(BitmapDescriptorFactory.HUE_RED))); else
		 * mapa.addMarker(new MarkerOptions() .position(punto.getCords())
		 * .title(punto.getNombre()) .snippet(punto.getNombre())
		 * .icon(BitmapDescriptorFactory
		 * .defaultMarker(BitmapDescriptorFactory.HUE_GREEN))); } } else if
		 * (result.equals("notUser")){
		 * Toast.makeText(getApplicationContext(),"Usuario no reconocido",
		 * Toast.LENGTH_SHORT).show(); }else{
		 * Toast.makeText(getApplicationContext
		 * (),"No se pudieron obtener los puntos", Toast.LENGTH_SHORT).show(); }
		 * } }
		 * 
		 * private class RetrieveFeed extends
		 * android.os.AsyncTask<String,Integer,Boolean> {
		 * 
		 * 
		 * protected Boolean doInBackground(String... params) {
		 * 
		 * ParserXML_DOM parser = new ParserXML_DOM(getApplicationContext());
		 * points = parser.listaPuntos();
		 * 
		 * return true; }
		 * 
		 * protected void onPostExecute(Boolean result) {
		 * 
		 * mapa.clear();
		 * 
		 * for (Punto punto : points) { if (punto.getNombre().equals("Coche"))
		 * mapa.addMarker(new MarkerOptions() .position(punto.getCords())
		 * .title(punto.getNombre()) .snippet(punto.getNombre())
		 * .icon(BitmapDescriptorFactory
		 * .defaultMarker(BitmapDescriptorFactory.HUE_RED))); else
		 * mapa.addMarker(new MarkerOptions() .position(punto.getCords())
		 * .title(punto.getNombre()) .snippet(punto.getNombre())
		 * .icon(BitmapDescriptorFactory
		 * .defaultMarker(BitmapDescriptorFactory.HUE_GREEN))); } }
		 * 
		 * }
		 */
	}

	public class FragmentoHuecos extends Fragment {

		ViewGroup vg;

		@SuppressLint("ValidFragment")
		public FragmentoHuecos() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			vg = container;
			return inflater.inflate(R.layout.fragment_huecos, container, false);
		}

		@Override
		public void onActivityCreated(Bundle state) {
			super.onActivityCreated(state);
			lstListado = (ListView) getView().findViewById(R.id.listView1);
			lstListado.setOnItemClickListener(onclick_punto);
			lstListado.setAdapter(adapter);
		}

		OnItemClickListener onclick_punto = new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
			}
		};
	}
}
