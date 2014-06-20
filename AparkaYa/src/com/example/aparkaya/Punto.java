package com.example.aparkaya;

import java.sql.Date;

import com.google.android.gms.maps.model.LatLng;

public class Punto {
	
	private Integer id_punto;
	private String usuario;
	private LatLng cords;
	private int reputacion;
	private Date fecha;
	
	public Punto(Integer id_punto, String usuario, LatLng cords, int reputacion){
		super();
		this.id_punto = id_punto;
		this.usuario = usuario;
		this.cords = cords;
		this.reputacion = reputacion;
		this.fecha = null;
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
	

	

}
