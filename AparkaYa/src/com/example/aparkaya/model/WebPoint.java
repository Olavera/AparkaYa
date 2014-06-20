package com.example.aparkaya.model;

import java.util.Date;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class WebPoint {
	
	private Integer id_punto;
	private String usuario;
	private LatLng cords;
	private int reputacion;
	private Date fecha;
	private Marker marker;
	
	public WebPoint(Integer id_punto, String usuario, LatLng cords, int reputacion, Date fecha){
		super();
		this.id_punto = id_punto;
		this.usuario = usuario;
		this.cords = cords;
		this.reputacion = reputacion;
		this.fecha = fecha;
		this.marker = null;
	}

	public Integer getId_punto() {
		return id_punto;
	}

	public void setId_punto(Integer id_punto) {
		this.id_punto = id_punto;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public LatLng getCords() {
		return cords;
	}

	public void setCords(LatLng cords) {
		this.cords = cords;
	}

	public int getReputacion() {
		return reputacion;
	}

	public void setReputacion(int reputacion) {
		this.reputacion = reputacion;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Marker getMarker() {
		return marker;
	}

	public void setMarker(Marker marker) {
		this.marker = marker;
	}
	

	

}
