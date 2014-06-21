package com.example.aparkaya;

public class Constants {
	
	
	public static final int RESULT_OK = 1;
	public static final int RESULT_NOTUSER = 2;
	public static final int RESULT_USER_EXISTS = 3;
	public static final int RESULT_EMAIL_EXISTS = 4;
	public static final int RESULT_FAILED_UPDATE_POINT = 5;
	public static final int RESULT_ERR = 6;
	
	public static final long LOCALSERVER_TIME_REFRESH = 20000;//20segundos por defecto
	
	public static final String ID_PUNTO = "id_punto";
	public static final String USUARIO = "usuario";
	public static final String LATITUD = "latitud";
	public static final String LONGITUD = "longitud";
	public static final String REPUTACION = "reputacion";
	public static final String FECHA = "fecha";
	
	public static final int ACTION_PARK = 1;
	public static final int ACTION_VOTE_POSSITIVE = 2;
	public static final int ACTION_VOTE_NEGATIVE = 3;
	public static final int ACTION_DELETE = 4;
	
	public static final String ACTION = "action";

}
