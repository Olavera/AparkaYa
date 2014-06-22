package com.example.aparkaya;

public class Constants {
	
	
	public static final int RESULT_OK = 1;
	public static final int RESULT_NOTUSER = 2;
	public static final int RESULT_USER_EXISTS = 3;
	public static final int RESULT_EMAIL_EXISTS = 4;
	public static final int RESULT_FAILED_UPDATE_POINT = 5;
	public static final int RESULT_ERR = 6;
	
	public static final long LOCALSERVER_TIME_REFRESH = 60000;//20segundos por defecto
	public static final long TIME_UMBRAL = 86400000;//24h en milisegundos
	public static final long TIME_GOOD = 600000;//10 min
	public static final long TIME_REGULAR = 1800000;//30 min
	public static final long TIME_BAD = 3600000;//1 h
	
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
	
	public static final String TIEMPO_REFRESCO = "tiempo_refresco";
	public static final int TIEMPO_REFRESCO_OPCION_1 = 5;
	public static final int TIEMPO_REFRESCO_OPCION_2 = 15;
	public static final int TIEMPO_REFRESCO_OPCION_3 = 30;
	public static final int TIEMPO_REFRESCO_OPCION_4 = 60;
	
	public static final String AREA_BUSQUEDA = "area_busqueda";
	public static final int AREA_BUSQUEDA_OPCION_1 = 500;
	public static final int AREA_BUSQUEDA_OPCION_2 = 1000;
	public static final int AREA_BUSQUEDA_OPCION_3 = 1500;
	public static final int AREA_BUSQUEDA_OPCION_4 = 0;
	
	public static final String ORDENAR_LISTA_POR = "ordenar_lista_por";
	public static final int ORDENAR_LISTA_POR_OPCION_1 = 1; //Tiempo
	public static final int ORDENAR_LISTA_POR_OPCION_2 = 2; //Distancia
	public static final int ORDENAR_LISTA_POR_OPCION_3 = 3; //Nombre
	public static final int ORDENAR_LISTA_POR_OPCION_4 = 4; //Reputacion
	
	public static final String TIEMPO_MAXIMO_EN_DIFUSION = "tiempo_maximo_difusion";
	public static final int TIEMPO_MAXIMO_EN_DIFUSION_OPCION_1 = 1;
	public static final int TIEMPO_MAXIMO_EN_DIFUSION_OPCION_2 = 2;
	public static final int TIEMPO_MAXIMO_EN_DIFUSION_OPCION_3 = 3;
	public static final int TIEMPO_MAXIMO_EN_DIFUSION_OPCION_4 = 4;
	
	public static final String USUARIO_PREFS = "nombre_usuario";
	public static final String CONTRASENIA_PREFS = "contrasenia_usuario";
	
	public static final String POINT_NO_MAPPED = "punto_no_mapeado";

}
