package com.example.aparkaya;

import java.util.ArrayList;
import java.util.Calendar;
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
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

@SuppressLint("ValidFragment")
public class AparkaYa extends ActionBarActivity implements ActionBar.TabListener {

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
	private AlarmManager alarm;
	private PendingIntent pintent;

   /* Messenger messenger = null;
    

	private Handler handler = new Handler() {
		public void handleMessage(Message message) {
			Bundle data = message.getData();
			if (message.arg1 == RESULT_OK && data != null) {
				String text = data.getString("");
				Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
			}
		}
	};

	private ServiceConnection conn = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			messenger = new Messenger(binder);
		}

		public void onServiceDisconnected(ComponentName className) {
			messenger = null;
		}
	};*/
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				String result = bundle.getString("RESULT");
				Toast.makeText(AparkaYa.this, "Mensaje: " + result, Toast.LENGTH_SHORT).show();
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
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
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
        alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		/*Intent intent = null;
		intent = new Intent(this, PointsRefreshService.class);
		//Creamos un nuevo Messenger para la comunicación    
		// Desde el Service al Activity
		Messenger messenger = new Messenger(handler);
		intent.putExtra("MESSENGER", messenger);

		bindService(intent, conn, Context.BIND_AUTO_CREATE);*/
		
		registerReceiver(receiver, new IntentFilter(PointsRefreshService.NOTIFICATION));
		
		// Start service using AlarmManager
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 10);
       
        Intent intent = new Intent(this, PointsRefreshService.class);
        pintent = PendingIntent.getService(this, 0, intent, 0);
       
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 10000, pintent);
        startService(new Intent(getBaseContext(), PointsRefreshService.class));     
	}

	@Override
	protected void onPause() {
		super.onPause();
		alarm.cancel(pintent);
		stopService(new Intent(getBaseContext(), PointsRefreshService.class));
	    unregisterReceiver(receiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
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
			
			if(position==0){
				fragment = new FragmentoMapa();
			}
			else{			
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

	public class FragmentoMapa extends Fragment implements OnMapClickListener, OnInfoWindowClickListener{

		private GoogleMap mapa = null;
		private Vector<Punto> points;
		private Button save, difundir;

		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */

		@SuppressLint("ValidFragment")
		public FragmentoMapa() {
		}
		
		// this method is only called once for this fragment
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        // retain this fragment
	        setRetainInstance(true);
	    }

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			
			View rootView = inflater.inflate(R.layout.fragment_mapa, container, false);
			save = (Button) rootView.findViewById(R.id.btnguardar);
			save.setOnClickListener(new btnGuardarListener());
			
			difundir = (Button) rootView.findViewById(R.id.btndifundir);
			difundir.setOnClickListener(new btnDifundir());
			
			initilizeMap();
			//iniciarTask();
			loadPoints();
			return rootView;
		}
		
		

		@Override
		public void setRetainInstance(boolean retain) {
			// TODO Auto-generated method stub
			super.setRetainInstance(retain);
		}

		/** function to load map. 
		 *  If map is not created it will create it for you
		 */
		private void initilizeMap() {
	
			if (mapa == null) {
				mapa = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

				// check if map is created successfully or not
				if (mapa == null) {
					Toast.makeText(getApplicationContext(),
							"Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
				}
				else{
					mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
					mapa.setMyLocationEnabled(true);
					if (mapa.getMyLocation() != null)
						mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(
								new LatLng( mapa.getMyLocation().getLatitude(), 
										mapa.getMyLocation().getLongitude()), 15));
					else
						Toast.makeText(getApplicationContext(),
								"Esperando ubicacion", Toast.LENGTH_SHORT).show();
					mapa.getUiSettings().setZoomControlsEnabled(false);
					mapa.getUiSettings().setCompassEnabled(true);
					mapa.setOnInfoWindowClickListener(this);
					mapa.setOnMapClickListener(this);
				}

			}
		}
		
		private void iniciarTask(){
			RetrieveFeed task = new RetrieveFeed();
			task.execute();
		}
		
		private void loadPoints(){
			new asyncCallPoints().execute();
		}

		

		@Override
		public void onMapClick(LatLng puntoPulsado) {
			/*ParserXML_DOM parser = new ParserXML_DOM(getApplicationContext());

			parser.guardarPunto("prueba", puntoPulsado);

			iniciarTask();*/
			new asyncSendPoint().execute(new Punto("1", puntoPulsado, 1));
		}

		@Override
		public void onInfoWindowClick(Marker marker) {
			ParserXML_DOM parser = new ParserXML_DOM(getApplicationContext());

			parser.eliminarPunto(marker.getTitle(), marker.getPosition());

			iniciarTask();
			
		}
		
		public class btnGuardarListener implements OnClickListener
		{
			@Override
			public void onClick(View v) {
				ParserXML_DOM parser = new ParserXML_DOM(getApplicationContext());

				parser.guardarPunto("Coche", new LatLng(mapa.getCameraPosition().target.latitude,
						mapa.getCameraPosition().target.longitude));

				iniciarTask();
			}
		}
		
		public class btnDifundir implements OnClickListener
		{
			@Override
			public void onClick(View v) {
				if (mapa.getMyLocation() != null)
					new asyncSendPoint().execute(new Punto("1", 
							new LatLng( mapa.getMyLocation().getLatitude(), mapa.getMyLocation().getLongitude()), 
							1));
				else
					Toast.makeText(getApplicationContext(),
							"Ubicación GPS no detectada", Toast.LENGTH_SHORT).show();
			}
		}
		
		private class asyncSendPoint extends AsyncTask< Punto, String, String > {
			
			protected String doInBackground(Punto... params) {

				Punto p = params[0];
				int id = -1;
				/*Creamos un ArrayList del tipo nombre valor para agregar los datos recibidos por los parametros anteriores
				 * y enviarlo mediante POST a nuestro sistema para relizar la validacion*/ 
				ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();

				postparameters2send.add(new BasicNameValuePair("user",user));
				postparameters2send.add(new BasicNameValuePair("password",pass));
				postparameters2send.add(new BasicNameValuePair("id_usuario",p.getNombre()));
				postparameters2send.add(new BasicNameValuePair("latitud",Double.toString(p.getCords().latitude)));
				postparameters2send.add(new BasicNameValuePair("longitud",Double.toString(p.getCords().longitude)));
				postparameters2send.add(new BasicNameValuePair("libre",Integer.toString(p.getOcupado())));

				//realizamos una peticion y como respuesta obtenes un array JSON
				JSONArray jdata=post.getserverdata(postparameters2send, "http://aparkaya.webcindario.com/enviarPunto.php");

				//si lo que obtuvimos no es null
				if (jdata != null && jdata.length() > 0) {

					JSONObject json_data; // creamos un objeto JSON
					try {
						json_data = jdata.getJSONObject(0); // leemos el primer
															// segmento en nuestro
															// caso el unico
						id = json_data.getInt("id");// accedemos al valor
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// validamos el valor obtenido
					if (id == 1) {
						return "ok"; //
					}
					else if (id == 2)
					{
						return "notUser";
					}
				}
				return "err"; //

			}

			protected void onPostExecute(String result) {

				if (result.equals("ok")){
					Toast.makeText(getApplicationContext(),"Punto enviado correctamente", Toast.LENGTH_SHORT).show();
					loadPoints();
				}
				else if (result.equals("notUser")){
					Toast.makeText(getApplicationContext(),"Usuario no reconocido", Toast.LENGTH_SHORT).show();
				}
				else{
					Toast.makeText(getApplicationContext(),"Fallo al enviar el punto", Toast.LENGTH_SHORT).show();
				}
			}
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
		
		private class RetrieveFeed extends android.os.AsyncTask<String,Integer,Boolean> {


			protected Boolean doInBackground(String... params) {

				ParserXML_DOM parser = new ParserXML_DOM(getApplicationContext());
				points = parser.listaPuntos();
				
				return true;
			}

			protected void onPostExecute(Boolean result) {
				
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

		}
	}
	
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public class FragmentoHuecos extends Fragment {
		
		 ArrayList<Punto> lista_puntos = GetlistPuntos();
		 private ListView lstListado;
		
		@SuppressLint("ValidFragment")
		public FragmentoHuecos() {
		}
		
		// this method is only called once for this fragment
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        // retain this fragment
	        setRetainInstance(true);
	    }
			 
	    @Override
	    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
	 
	        return inflater.inflate(R.layout.fragment_huecos, container, false);
	    }
	    
	    @Override
		public void setRetainInstance(boolean retain) {
			// TODO Auto-generated method stub
			super.setRetainInstance(retain);
		}
	 
	    @Override
	    public void onActivityCreated(Bundle state) {
	        super.onActivityCreated(state);
	 
	        lstListado = (ListView)getView().findViewById(R.id.listView1);
	 
	        lstListado.setAdapter(new AdaptadorPuntos(this));
	    }
	 
	    class AdaptadorPuntos extends ArrayAdapter<Punto> {
	 
	            Activity context;
	 
	            AdaptadorPuntos(Fragment context) {
	                super(context.getActivity(), R.layout.vista_punto, GetlistPuntos());
	                this.context = context.getActivity();
	            }
	 
	            public View getView(int position, View convertView, ViewGroup parent) {
	            	
	            LayoutInflater inflater = context.getLayoutInflater();
	            View v = inflater.inflate(R.layout.vista_punto, null);
	            
	            // Creamos un objeto directivo
		        Punto punto = lista_puntos.get(position);
	 
		      //Rellenamos el nombre
		        TextView nombre = (TextView) v.findViewById(R.id.textNameList);
		        nombre.setText(punto.getNombre());
		        //Rellenamos el cargo
		        TextView coordenadas = (TextView) v.findViewById(R.id.textLatLng);
		        coordenadas.setText(punto.getCords().toString());
	 
	            return v;
	        }
	    }
			
		OnItemClickListener onclick_punto = new OnItemClickListener() 
		{
			
		
			public void onItemClick(AdapterView<?> parent,View view, int position, long id)
			{
				
				Intent i = new Intent(AparkaYa.this, LoginActivity.class);
				
				/*
				Punto punto = lista_puntos.get(position);
				
				i.putExtra("name_dt", rst.getName());
				i.putExtra("country_dt", rst.getCountry());
				i.putExtra("city_dt", rst.getCity());
		        i.putExtra("street_type_dt", rst.getStreet_type());
		        i.putExtra("street_dt", rst.getStreet());
		        i.putExtra("street_num_dt", String.valueOf(rst.getStreet_num()));
		        i.putExtra("food_type_dt", rst.getTypeToString());
		        i.putExtra("food_nac_dt", rst.getOrigen());
		        i.putExtra("price_dt", String.valueOf(rst.getPrice()));
		        i.putExtra("img_dt", String.valueOf(rst.getImg()));
				*/
		        startActivity(i);
			}
		};
		
		private ArrayList<Punto> GetlistPuntos(){
			
			ArrayList<Punto> listap = new ArrayList<Punto>();

		    Punto punto1 = new Punto();
		    Punto punto2 = new Punto();
		    Punto punto3 = new Punto();

		    punto1.setNombre("punto1");
		    punto1.setCords(new LatLng(40,40));
		    punto1.setOcupado(0);
		    listap.add(punto1);

		    punto2.setNombre("punto2");
		    punto2.setCords(new LatLng(80,50));
		    punto2.setOcupado(1);
		    listap.add(punto2);

		    punto3.setNombre("punto3");
		    punto3.setCords(new LatLng(75,40));
		    punto3.setOcupado(0);
		    listap.add(punto3);

		    return listap; 
		    }   
		}	
}
