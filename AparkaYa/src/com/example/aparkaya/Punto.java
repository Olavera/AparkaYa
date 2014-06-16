package com.example.aparkaya;

import com.google.android.gms.maps.model.LatLng;

public class Punto {
	
	private String nombre;
	private LatLng cords;
	private int ocupado;
	
	public Punto(String nombre, LatLng cords,int ocupado) {
		super();
		this.nombre = nombre;
		this.cords = cords;
		this.ocupado=ocupado;
	
	}
	

	public Punto() {
		super();
	}


	public int getOcupado() {
		return ocupado;
	}

	public void setOcupado(int ocupado) {
		this.ocupado = ocupado;
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
