package com.example.aparkaya.model;

/**
 * Clase usada para el refresco de puntos
 * Describe una accion que se va a ejecutar sobre el punto
 */
public class RefreshAction {
	// Punto
	private WebPoint newPointInfo;
	// Accion
	private EnumTypeRefreshAction typeAction;
	// Informacion adicional
	private int franja;
	private String textDate;
	private String idMarker;

	public RefreshAction(WebPoint newPointInfo,
			EnumTypeRefreshAction typeAction, int franja, String textDate,
			String idMarker) {
		super();
		this.newPointInfo = newPointInfo;
		this.typeAction = typeAction;
		this.franja = franja;
		this.textDate = textDate;
		this.idMarker = idMarker;
	}

	public WebPoint getnewPointInfo() {
		return newPointInfo;
	}

	public void setnewPointInfo(WebPoint punto) {
		this.newPointInfo = punto;
	}

	public EnumTypeRefreshAction getTypeAction() {
		return typeAction;
	}

	public void setTypeAction(EnumTypeRefreshAction typeAction) {
		this.typeAction = typeAction;
	}

	public int getFranja() {
		return franja;
	}

	public void setFranja(int franja) {
		this.franja = franja;
	}

	public String getTextDate() {
		return textDate;
	}

	public void setTextDate(String textDate) {
		this.textDate = textDate;
	}

	public String getIdMarker() {
		return idMarker;
	}

	public void setIdMarker(String idMarker) {
		this.idMarker = idMarker;
	}
	
	
}
