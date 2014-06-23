package com.example.aparkaya;

import java.text.DecimalFormat;
import java.text.ParseException;
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
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.drawable.Drawable;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aparkaya.localService.PointsRefreshService;
import com.example.aparkaya.model.EnumTypeRefreshAction;
import com.example.aparkaya.model.RefreshAction;
import com.example.aparkaya.model.WebPoint;
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
	private PointsRefreshService localService;
	private HashMap<String, WebPoint> hashmap_idMarker_WebPoint;
	private SparseArray<String> array_idPoint_idMarker;
	private GoogleMap mapa = null;
	private ListView lstListado;
	private AdaptadorPuntos adapter;
	private ArrayList<WebPoint> listpoints;

	private ProgressDialog pDialog;

	// Variables sobres las preferencias del usuario
	private int t_refresco;
	private int area_busqueda;
	private int ordenar_lista_por;
	private int t_max_en_difusion;

	private SharedPreferences prefs;

	private MyServiceConnection mConnection;

	public class MyServiceConnection implements ServiceConnection {
		public void onServiceConnected(ComponentName className, IBinder binder) {
			PointsRefreshService.MyBinder b = (PointsRefreshService.MyBinder) binder;
			localService = b.getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			localService = null;
		}
	};

	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			/*Toast.makeText(getApplicationContext(), "Recibido Broadcast",
					Toast.LENGTH_SHORT).show();*/
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				int result = bundle.getInt(Constants.RESULT);
				repaintPoints(result, localService.getArrayPoints());
			}
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
						reordenarLista();
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			actionBar.addTab(actionBar.newTab()
					.setIcon(mSectionsPagerAdapter.getPageIcon(i))
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

		prefs = getSharedPreferences(Constants.MyPreferences, Context.MODE_PRIVATE);

		t_refresco = prefs.getInt(Constants.TIEMPO_REFRESCO, Constants.TIEMPO_REFRESCO_OPCION_1);
		area_busqueda = prefs.getInt(Constants.AREA_BUSQUEDA, Constants.AREA_BUSQUEDA_OPCION_1);
		ordenar_lista_por = prefs.getInt(Constants.ORDENAR_LISTA_POR, Constants.ORDENAR_LISTA_POR_OPCION_1);
		t_max_en_difusion = prefs.getInt(Constants.TIEMPO_MAXIMO_EN_DIFUSION, Constants.TIEMPO_MAXIMO_EN_DIFUSION_OPCION_1);
		
		/*Toast.makeText(
				getApplicationContext(),
				"Preferencias:" + t_refresco + " | " + area_busqueda + " | "
						+ ordenar_lista_por, Toast.LENGTH_LONG).show();*/

		// -----------------------------------------------------------------------

		mConnection = new MyServiceConnection();

		registerReceiver(receiver, new IntentFilter(
				PointsRefreshService.NOTIFICATION));

		// Start service using AlarmManager
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, 10);
		Intent intt = new Intent(this, PointsRefreshService.class);
		intt.putExtra("user", user);
		intt.putExtra("pass", pass);
		PendingIntent pintent = PendingIntent.getService(this, 0, intt,
				PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				cal.getTimeInMillis(), Constants.LOCALSERVER_TIME_REFRESH,
				pintent);

		Intent intent = new Intent(this, PointsRefreshService.class);
		intent.putExtra("user", user);
		intent.putExtra("pass", pass);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();

		// SHARED PREFERENCES

		SharedPreferences prefs = getSharedPreferences(Constants.MyPreferences,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(Constants.TIEMPO_REFRESCO, t_refresco);
		editor.putInt(Constants.AREA_BUSQUEDA, area_busqueda);
		editor.putInt(Constants.ORDENAR_LISTA_POR, ordenar_lista_por);
		editor.putInt(Constants.TIEMPO_MAXIMO_EN_DIFUSION, t_max_en_difusion);
		editor.commit();

		// ----------------------------------------------------------

		Intent intt = new Intent(this, PointsRefreshService.class);
		PendingIntent pintent = PendingIntent.getService(this, 0, intt,
				PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarm.cancel(pintent);

		unbindService(mConnection);
		localService.onDestroy();
		stopService(new Intent(getBaseContext(), PointsRefreshService.class));
		unregisterReceiver(receiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_opciones_aparkaya, menu);
		
		return true;
	}

	// Código para cada opción de menú
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.t_refresco_op_1:
				t_refresco = Constants.TIEMPO_REFRESCO_OPCION_1;
				return true;
			case R.id.t_refresco_op_2:
				t_refresco = Constants.TIEMPO_REFRESCO_OPCION_2;
				return true;
			case R.id.t_refresco_op_3:
				t_refresco = Constants.TIEMPO_REFRESCO_OPCION_3;
				return true;
			case R.id.t_refresco_op_4:
				t_refresco = Constants.TIEMPO_REFRESCO_OPCION_4;
				return true;
			case R.id.area_busqueda_op_1:
				area_busqueda = Constants.AREA_BUSQUEDA_OPCION_1;
				return true;
			case R.id.area_busqueda_op_2:
				area_busqueda = Constants.AREA_BUSQUEDA_OPCION_2;
				return true;
			case R.id.area_busqueda_op_3:
				area_busqueda = Constants.AREA_BUSQUEDA_OPCION_3;
				return true;
			case R.id.area_busqueda_op_4:
				area_busqueda = Constants.AREA_BUSQUEDA_OPCION_4;
				return true;
			case R.id.ordenar_lista_por_op_1:
				ordenar_lista_por = Constants.ORDENAR_LISTA_POR_OPCION_1;
				reordenarLista();
				return true;
			case R.id.ordenar_lista_por_op_2:
				ordenar_lista_por = Constants.ORDENAR_LISTA_POR_OPCION_2;
				reordenarLista();
				return true;
			case R.id.ordenar_lista_por_op_3:
				ordenar_lista_por = Constants.ORDENAR_LISTA_POR_OPCION_3;
				reordenarLista();
				return true;
			case R.id.ordenar_lista_por_op_4:
				ordenar_lista_por = Constants.ORDENAR_LISTA_POR_OPCION_4;
				reordenarLista();
				return true;
			case R.id.t_max_en_difusion_op_1:
				t_max_en_difusion = Constants.TIEMPO_MAXIMO_EN_DIFUSION_OPCION_1;
				return true;
			case R.id.t_max_en_difusion_op_2:
				t_max_en_difusion = Constants.TIEMPO_MAXIMO_EN_DIFUSION_OPCION_2;
				return true;
			case R.id.t_max_en_difusion_op_3:
				t_max_en_difusion = Constants.TIEMPO_MAXIMO_EN_DIFUSION_OPCION_3;
				return true;
			case R.id.t_max_en_difusion_op_4:
				t_max_en_difusion = Constants.TIEMPO_MAXIMO_EN_DIFUSION_OPCION_4;
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
	
	// -------------------------- FRAGMENTOS --------------------------

	/**
	 * Fragmento correspondiente al mapa de puntos libres. 
	 * Corresponde al fragmento con indice 0 en el tab.
	 */
	public class FragmentoMapa extends Fragment implements
			OnMapLongClickListener, OnInfoWindowClickListener {

		private ImageButton save, difundir;

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
			save = (ImageButton) rootView.findViewById(R.id.btnguardar);
			save.setOnClickListener(new btnGuardarListener());

			difundir = (ImageButton) rootView.findViewById(R.id.btndifundir);
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

		@Override
		public void onMapLongClick(LatLng puntoPulsado) {
			starProgressDialog();
			new asyncSendPoint().execute(puntoPulsado);
			try {
				localService.forceRefresh();
			} catch (Exception e1) {
				Log.w(getClass().getName(), "Excepción forzando refresh", e1);
			}
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
				/*
				 * ParserXML_DOM parser = new ParserXML_DOM(
				 * getApplicationContext());
				 * 
				 * parser.guardarPunto("Coche", new
				 * LatLng(mapa.getCameraPosition().target.latitude,
				 * mapa.getCameraPosition().target.longitude));
				 * 
				 * iniciarTask();
				 */
				try {
					localService.forceRefresh();
				} catch (Exception e1) {
					Log.w(getClass().getName(),
							getResources()
									.getString(R.string.err_force_refresh), e1);
				}
			}
		}

		public class btnDifundir implements OnClickListener {
			@Override
			public void onClick(View v) {
				if (mapa.getMyLocation() != null) {
					starProgressDialog();
					new asyncSendPoint().execute(new LatLng(mapa
							.getMyLocation().getLatitude(), mapa
							.getMyLocation().getLongitude()));
					try {
						localService.forceRefresh();
					} catch (Exception e1) {
						Log.w(getClass().getName(),
								getResources().getString(
										R.string.err_force_refresh), e1);
					}
				} else
					Toast.makeText(getApplicationContext(),
							"Ubicación GPS no detectada", Toast.LENGTH_SHORT)
							.show();
			}
		}
	
	}

	/**
	 * Fragmento correspondiente a la lista de puntos libres
	 *  visibles en el fragmento mapa. 
	 * Corresponde al fragmento con indice 1 en el tab.
	 */
	public class FragmentoLista extends Fragment {

		ViewGroup vg;

		@SuppressLint("ValidFragment")
		public FragmentoLista() {
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
			@SuppressLint("NewApi")
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				WebPoint punto_selec = (WebPoint) lstListado
						.getItemAtPosition(position);
				getActivity().getActionBar().setSelectedNavigationItem(0);
				mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(
						punto_selec.getCords(), 15));
				punto_selec.getMarker().showInfoWindow();
			}
		};

	}
	
	// ------------------ METODOS DE REFRESCO DE INFORMACION DE FRAGMENTOS ------------------
	
	/**
	 * Comprueba el resultado de obtener la lista de nuevos puntos del servidor
	 * y ejecuta el proceso de refresco si la lista se obtuvo correctamente
	 */
	private void repaintPoints(int result, ArrayList<WebPoint> auxpoints) {

		if (result == Constants.RESULT_OK) {
			/*Toast.makeText(getApplicationContext(), "Strart refresh",
					Toast.LENGTH_SHORT).show();*/
			new asyncRefreshPoint().execute(auxpoints);
		} else if (result == Constants.RESULT_NOTUSER) {
			Toast.makeText(getApplicationContext(), "Usuario no reconocido",
					Toast.LENGTH_SHORT).show();
		} else if (result == Constants.RESULT_ERR) {
			Toast.makeText(getApplicationContext(),
					"Error al actualizar información", Toast.LENGTH_SHORT)
					.show();
		}

	}
	
	/**
	 * Selecciona un metodo de ordenacion para la lista a partir 
	 * de la variable "ordenar_lista_por", reordena y notifica los cambios
	 *  al adaptador de la lista
	 */
	public void reordenarLista() {
		// Ordenacion por id (por defecto)
		Comparator<WebPoint> comparator = new Id_punto_Comparator();
		switch (ordenar_lista_por) {
			// Ordenar por fecha
			case Constants.ORDENAR_LISTA_POR_OPCION_1: 
				comparator = new Fecha_Comparator();
				break;
			// Ordenar por distacia
			case Constants.ORDENAR_LISTA_POR_OPCION_2:
				if (mapa != null) 
					if (mapa.getMyLocation() != null)
						comparator = new Distancia_Comparator(
								new LatLng(mapa.getMyLocation().getLatitude(), mapa.getMyLocation().getLongitude()));
					else
						Toast.makeText(getApplicationContext(), getResources().getString(R.string.GPS_no_detected),
								Toast.LENGTH_SHORT).show();
				break;
			// Ordenar por nombre de usuario
			case Constants.ORDENAR_LISTA_POR_OPCION_3:
				comparator = new User_Comparator();
				break;
			// Ordenar por reputacion de usuario
			case Constants.ORDENAR_LISTA_POR_OPCION_4:
				comparator = new Rep_Comparator();
				break;
			default:
				break;
		}
		// Ejecuta la ordenacion
		Collections.sort(listpoints, comparator);
		// Notifica los cambios al adaptador de la lista
		adapter.notifyDataSetChanged();
	}
	
	// -------------------------- TAREAS ASINCRONAS --------------------------
	
	private class asyncRefreshPoint extends
	AsyncTask<ArrayList<WebPoint>, String, Integer> {

		// Lista de acciones de refresco que se ejecutaran sobre los puntos del
		// mapa
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
			for (int i = 0; i < array_idPoint_idMarker.size(); i++) {
				key = array_idPoint_idMarker.keyAt(i);
				copyOf_idPoint_idMarker.put(key,
						array_idPoint_idMarker.get(key));
			}

			for (WebPoint punto : auxpoints) {
				int franja = obtenerFranja(punto.getFecha());
				String textDate = "No se obtuvo fecha";
				if (punto.getFecha().getTime() < timeUmbral)
					textDate = "Difundido el "
							+ dateFormat1.format(punto.getFecha());
				else {
					textDate = "Difundido a las "
							+ dateFormat2.format(punto.getFecha());
				}

				String idMarker = copyOf_idPoint_idMarker.get(
						punto.getId_punto(), Constants.POINT_NO_MAPPED);
				if (idMarker.equals(Constants.POINT_NO_MAPPED)) {
					refreshActions.add(new RefreshAction(punto,
							EnumTypeRefreshAction.ADD, franja, textDate,
							idMarker));
				} else {
					WebPoint oldP = hashmap_idMarker_WebPoint.get(idMarker);
					Marker m = oldP.getMarker();
					punto.setMarker(m);
					if (/*
					 * ((m.getPosition().latitude!=punto.getCords().latitude)
					 * ||
					 * (m.getPosition().longitude!=punto.getCords().longitude
					 * )) ||
					 */
							(obtenerFranja(oldP.getFecha()) != franja)) {

						refreshActions.add(new RefreshAction(punto,
								EnumTypeRefreshAction.UPDATE, franja, textDate,
								idMarker));
						hashmap_idMarker_WebPoint.put(idMarker, punto);
					} else if (oldP.getReputacion() != punto.getReputacion()) {
						hashmap_idMarker_WebPoint.get(idMarker).setReputacion(
								punto.getReputacion());
					}
					copyOf_idPoint_idMarker.remove(punto.getId_punto());
				}
			}

			key = 0;
			for (int i = 0; i < copyOf_idPoint_idMarker.size(); i++) {
				key = copyOf_idPoint_idMarker.keyAt(i);
				String idMarker = copyOf_idPoint_idMarker.get(key);
				if (hashmap_idMarker_WebPoint.get(idMarker) != null)
					refreshActions.add(new RefreshAction(
							hashmap_idMarker_WebPoint.get(idMarker)// new
							// WebPoint(key,
							// "", null,
							// 0, null)
							, EnumTypeRefreshAction.REMOVE, 0, "",
							array_idPoint_idMarker.get(key)));
			}

			return Constants.RESULT_OK;
		}

		protected void onPostExecute(Integer result) {
			for (RefreshAction act : refreshActions) {
				if (act.getTypeAction() == EnumTypeRefreshAction.ADD) {
					Marker marker = mapa.addMarker(new MarkerOptions()
					.position(act.getnewPointInfo().getCords())
					.title(act.getTextDate())
					.snippet("por " + act.getnewPointInfo().getUsuario())
					.icon(BitmapDescriptorFactory
							.defaultMarker(obtenerIconoFranja(act
									.getFranja()))));
					String key = marker.getId();
					WebPoint punto = act.getnewPointInfo();
					punto.setMarker(marker);
					hashmap_idMarker_WebPoint.put(key, punto);
					array_idPoint_idMarker.put(punto.getId_punto(), key);
					listpoints.add(punto);
				} else if (act.getTypeAction() == EnumTypeRefreshAction.UPDATE) {
					WebPoint newP = act.getnewPointInfo();
					Marker m = newP.getMarker();
					m.setIcon(BitmapDescriptorFactory
							.defaultMarker(obtenerIconoFranja(act.getFranja())));
					m.setTitle(act.getTextDate());
				} else if (act.getTypeAction() == EnumTypeRefreshAction.REMOVE) {
					String idMarker = array_idPoint_idMarker.get(act
							.getnewPointInfo().getId_punto());
					array_idPoint_idMarker.remove(act.getnewPointInfo()
							.getId_punto());
					hashmap_idMarker_WebPoint.get(idMarker).getMarker()
					.remove();
					hashmap_idMarker_WebPoint.remove(idMarker);
					listpoints.remove(act.getnewPointInfo());
				}
			}
			adapter.notifyDataSetChanged();
			/*Toast.makeText(getApplicationContext(), "Stop refresh",
					Toast.LENGTH_SHORT).show();*/
		}
	}
	
	/**
	 * Proceso asincrono que realiza la llamada al servicio web para difundir un punto
	 * y recoge el valor de respuesta
	 */
	public class asyncSendPoint extends AsyncTask<LatLng, Integer, Integer> {

		protected Integer doInBackground(LatLng... params) {
			// Coordenadas del punto a difundir
			LatLng latlng = params[0];
			// 
			int id = -1;

			// Format para que la base de datos lo pueda reconoces como tipo DateTime
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
			// Obtenemos la hora actual y la formateamos
			Date date = new Date();
			String fecha = dateFormat.format(date);
			
			// Creamos un ArrayList del tipo clave-valor y agregamos los valores necesarios
			ArrayList<NameValuePair> postparameters2send = new ArrayList<NameValuePair>();
			postparameters2send.add(new BasicNameValuePair(Constants.USER, user));
			postparameters2send
					.add(new BasicNameValuePair(Constants.PASSWORD, pass));
			postparameters2send.add(new BasicNameValuePair(Constants.LATITUD,
					Double.toString(latlng.latitude)));
			postparameters2send.add(new BasicNameValuePair(Constants.LONGITUD,
					Double.toString(latlng.longitude)));
			postparameters2send.add(new BasicNameValuePair(Constants.FECHA, fecha));

			// Realiza una peticion enviando los datos mediante el metodo POST 
			// y obtiene como respuesta un array JSON
			JSONArray jdata = post.getserverdata(postparameters2send, Constants.php_enviarPunto);

			// Si no obtenemos una respuesta nula de la direccion parseamos la informacion
			// En este caso la informacion devuelta sera solo un identificador con el resultado de la consulta
			if (jdata != null && jdata.length() > 0) {
				JSONObject json_data;
				try {
					json_data = jdata.getJSONObject(0);
					id = json_data.getInt(Constants.ID);
				} catch (JSONException e) {
					e.printStackTrace();
				}

				return id;
			}
			return Constants.RESULT_ERR; //
		}

		@Override
		protected void onPreExecute() {
			pDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					asyncSendPoint.this.cancel(true);
				}
			});
			// Mostramos el progress dialog
			pDialog.show();
		}
		
		protected void onPostExecute(Integer result) {
			if (result == Constants.RESULT_OK) {
				Toast.makeText(getApplicationContext(),
						getResources().getString(R.string.point_spread), Toast.LENGTH_SHORT)
						.show();
			} else if (result == Constants.RESULT_NOTUSER) {
				Toast.makeText(getApplicationContext(),
						"Usuario no reconocido", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(),
						"Fallo al difundir el punto", Toast.LENGTH_SHORT)
						.show();
			}
			// Terminamos el progress dialog
			pDialog.dismiss();
		}
	}
	
	// -------------------------METODOS AUXILIARES-------------------------
	
	/**
	 *  Obtiene a que franja pertenece una fecha a partir de las marca de tiempo
	 *  declaradas en constantes y la codifica como un entero
	 */
	public int obtenerFranja(Date d) {
		int franja;
		long dateNow = new Date().getTime();
		long datePoint = d.getTime();

		if (datePoint < (dateNow - Constants.TIME_UMBRAL)) {
			franja = 0;
		} else if (datePoint < (dateNow - Constants.TIME_BAD)) {
			franja = 1;
		} else if (datePoint < (dateNow - Constants.TIME_REGULAR)) {
			franja = 2;
		} else if (datePoint < (dateNow - Constants.TIME_GOOD)) {
			franja = 3;
		} else {
			franja = 4;
		}

		return franja;
	}

	/**
	 * Apartir de una franja de tiempo devuelve el color de marker 
	 * correspondiente en codificacion HUE
	 */	
	public float obtenerIconoFranja(int franja) {
		float color;

		switch (franja) {
		case 0:
			color = BitmapDescriptorFactory.HUE_VIOLET;
			break;
		case 1:
			color = BitmapDescriptorFactory.HUE_RED;
			break;
		case 2:
			color = BitmapDescriptorFactory.HUE_ORANGE;
			break;
		case 3:
			color = BitmapDescriptorFactory.HUE_YELLOW;
			break;
		case 4:
			color = BitmapDescriptorFactory.HUE_GREEN;
			break;
		default:
			color = BitmapDescriptorFactory.HUE_ROSE;
			break;
		}
		return color;
	}

	/**
	 * Apartir de una franja de tiempo devuelve la referencia de resources
	 * del icnono marker asociado
	 */	
	public int obtenerIdRecursoIconoFranja(int franja) {
		int color;

		switch (franja) {
		case 0:
			color = R.drawable.violet_marker;
			break;
		case 1:
			color = R.drawable.red_marker;
			break;
		case 2:
			color = R.drawable.orange_marker;
			break;
		case 3:
			color = R.drawable.yellow_marker;
			break;
		case 4:
			color = R.drawable.green_marker;
			break;
		default:
			color = R.drawable.rose_marker;
			break;
		}
		return color;
	}
	
	/**
	 * Metodo que vuelve la distancia entre dos coordenadas
	 */
	public double distanciaEntreCoordenadas(LatLng coord1, LatLng coord2){
		return Math.abs(Math.sqrt(Math.pow(coord2.latitude - coord1.latitude, 2) +
				Math.pow(coord2.longitude - coord1.longitude, 2)));
	}

	/**
	 * Inicia el dialogo de progreso para acciones 
	 * que necesitan una respuesta antes de continuar
	 */
	public void starProgressDialog(){
		pDialog = new ProgressDialog(AparkaYa.this);
		pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pDialog.setMessage(getResources().getString(R.string.loading));
		pDialog.setCancelable(true);
	}
	
	
	// -------------------------- ADAPTADORES --------------------------
	
	/**
	 * Adaptador para el tab de seleccion de fragmentos
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		/**
		 * Devuelve una instancia del fragmento correspondiente al indice
		 * de la pestaña pasada como argumento
		 */
		@Override
		public Fragment getItem(int position) {
			Fragment fragment;

			if (position == 0) {
				fragment = new FragmentoMapa();
			} else {
				fragment = new FragmentoLista();
			}
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 2 total pages.
			return 2;
		}
		
		/**
		 * Devuelve el drawable correspondiente a la pestaña
		 */
		public Drawable getPageIcon(int position) {
			switch (position) {
			case 0:
				return getResources().getDrawable(R.drawable.icon_map);
			case 1:
				return getResources().getDrawable(R.drawable.icon_list);
			}
			return null;
		}
	}
	
	/**
	 * Adaptador para los elementos de la lista de puntos del fragmento FragmentoLista
	 */
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

			// Format para mostrar las coordenadas con un limite de decimales
			DecimalFormat df = new DecimalFormat("#.####");
			// Format para mostrar el dia y el mes de la fecha
			SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM",
					Locale.getDefault());
			// Format para mostrar la hora de la fecha
			SimpleDateFormat dateFormat2 = new SimpleDateFormat("HH:mm",
					Locale.getDefault());

			// Colocamos la imagen correspondiente
			ImageView img = (ImageView) v.findViewById(R.id.imagenListView);
			img.setImageResource(obtenerIdRecursoIconoFranja(obtenerFranja(punto.getFecha())));
			// Rellenamos el titulo con el nombre y la reputacion de usuario
			TextView tit = (TextView) v.findViewById(R.id.txt_point_title);
			tit.setText(punto.getId_punto() + ": " + punto.getUsuario() + " ("
					+ punto.getReputacion() + ")");
			// Rellenamos el subtitulo 1 con la fecha de difusion
			TextView sub1 = (TextView) v.findViewById(R.id.txt_point_sub1);
			sub1.setText("Difundido a las "
					+ dateFormat2.format(punto.getFecha()) + " el "
					+ dateFormat1.format(punto.getFecha()));
			// Rellenamos el subtitulo 3 con las coordenadas
			TextView sub2 = (TextView) v.findViewById(R.id.txt_point_sub2);
			sub2.setText("Coords: (" + df.format(punto.getCords().latitude)
					+ " / " + df.format(punto.getCords().longitude) + ")");

			return v;
		}
	}
	
	
	// -------------------------COMPARADORES------------------------- 
	// Comparadores usados para ordenar la lista de puntos a partir 
	// de diferentes atributos de los puntos.
	
	// Ordenar por fecha, mas nuevos primero
	class Fecha_Comparator implements Comparator<WebPoint> {
		@Override
		public int compare(WebPoint p1, WebPoint p2) {
			int flag = Long.valueOf(p2.getFecha().getTime()).compareTo(
					Long.valueOf(p1.getFecha().getTime()));
			return flag;
		}
	}
	
	// Ordenar por coordenadas respecto a posicion actual, mas cercanos  primero
	class Distancia_Comparator implements Comparator<WebPoint> {
		
		LatLng posicion;
		
		public Distancia_Comparator(LatLng posicion_actual){
			super();
			posicion = posicion_actual;
		}
		@Override
		public int compare(WebPoint p1, WebPoint p2) {
			return Double.valueOf(distanciaEntreCoordenadas(posicion, p1.getCords()))
					.compareTo(Double.valueOf(distanciaEntreCoordenadas(posicion, p2.getCords())));
		}
	}

	// Ordenar por nombre de usuario
	class User_Comparator implements Comparator<WebPoint> {
		@Override
		public int compare(WebPoint p1, WebPoint p2) {
			return p1.getUsuario().toUpperCase()
					.compareTo(p2.getUsuario().toUpperCase());
		}
	}

	// Ordenar por reputacion de usuario, mayor reputacion primero
	class Rep_Comparator implements Comparator<WebPoint> {
		@Override
		public int compare(WebPoint p1, WebPoint p2) {
			return Integer.valueOf(p2.getReputacion()).compareTo(
					Integer.valueOf(p1.getReputacion()));
		}
	}
	
	// Ordenar por id del punto, menor id primero
	class Id_punto_Comparator implements Comparator<WebPoint> {
		@Override
		public int compare(WebPoint p1, WebPoint p2) {
			return Integer.valueOf(p1.getId_punto()).compareTo(
					Integer.valueOf(p2.getId_punto()));
		}
	}
	
}
