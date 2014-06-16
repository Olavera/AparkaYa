package com.example.aparkaya;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
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
		private Button save;

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
			initilizeMap();
			iniciarTask();
			
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

		

		@Override
		public void onMapClick(LatLng puntoPulsado) {
			ParserXML_DOM parser = new ParserXML_DOM(getApplicationContext());

			parser.guardarPunto("prueba", puntoPulsado);

			iniciarTask();
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
