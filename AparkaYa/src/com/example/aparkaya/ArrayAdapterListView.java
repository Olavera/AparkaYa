package com.example.aparkaya;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ArrayAdapterListView extends BaseAdapter{
 
    protected Activity activity;
    protected ArrayList<Punto> lista_puntos;
 
    public ArrayAdapterListView(Activity activity, ArrayList<Punto> lista_puntos) {
        this.activity = activity;
        this.lista_puntos = lista_puntos;
      }
 
    @Override
    public int getCount() {
        return lista_puntos.size();
    }
 
    @Override
    public Object getItem(int arg0) {
        return lista_puntos.get(arg0);
    }

 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
 
        // Generamos una convertView por motivos de eficiencia
        View v = convertView;
 
        //Asociamos el layout de la lista que hemos creado
        if(convertView == null){
            LayoutInflater inf = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inf.inflate(R.layout.vista_punto, null);
        }
 
        // Creamos un objeto directivo
        Punto punto = lista_puntos.get(position);
        
        /*
        //Rellenamos la fotograf�a
        ImageView foto = (ImageView) v.findViewById(R.id.imageView);
        foto.setImageDrawable(activity.getResources().getDrawable(punto.getImg()));*/
        
        //Rellenamos el nombre
        TextView nombre = (TextView) v.findViewById(R.id.textNameList);
        nombre.setText(punto.getNombre());
        //Rellenamos el cargo
        TextView coordenadas = (TextView) v.findViewById(R.id.textLatLng);
        coordenadas.setText(punto.getCords().toString());
 
        // Retornamos la vista
        return v;
    }

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
}