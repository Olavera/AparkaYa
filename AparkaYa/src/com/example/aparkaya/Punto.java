package com.example.aparkaya;

import com.google.android.gms.maps.model.LatLng;

public class Punto {
	
	String nombre;
	LatLng cords;
	
	public Punto(String nombre, LatLng cords) {
		super();
		this.nombre = nombre;
		this.cords = cords;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public LatLng getCords() {
		return cords;
	}

	public void setCords(LatLng cords) {
		this.cords = cords;
	}

	

}
