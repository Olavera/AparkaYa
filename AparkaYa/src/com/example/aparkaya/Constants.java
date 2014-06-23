package com.example.aparkaya;

public class Constants {
	
	// Cadenas de conexion al servicio web
	public static final String DOMINIO = "http://aparkaya.webcindario.com/";
	//public static final String DOMINIO = "http://aparkaya.zxq.net/";
	public static final String php_login = DOMINIO + "login.php";
	public static final String php_registro = DOMINIO + "registro.php";
	public static final String php_obtenerPuntos = DOMINIO + "obtenerPuntos.php";
	public static final String php_enviarPunto = DOMINIO + "enviarPunto.php";
	public static final String php_cambiarPuntos = DOMINIO + "cambiarPuntos.php";
	public static final String php_obtenerInfoCuenta = DOMINIO + "obtenerInfoCuenta.php";
	
	// Valores de resultado de las llamadas al servicio web
	public static final int RESULT_OK = 1;
	public static final int RESULT_NOTUSER = 2;
	public static final int RESULT_USER_EXISTS = 3;
	public static final int RESULT_EMAIL_EXISTS = 4;
	public static final int RESULT_FAILED_UPDATE_POINT = 5;
	public static final int RESULT_ERR = 6;
	
	// Tiempo en milisegundos del intervalo por defecto entre llamadas 
	// del AlarmManager al servicio de refresco(PointsRefreshService)
	public static final long LOCALSERVER_TIME_REFRESH = 20000; //20 s
	
	// Marcas de tiempo en milisegundos para diferenciar las franjas de horas.
	public static final long TIME_GOOD = 600000; //10 min
	public static final long TIME_REGULAR = 1800000; //30 min
	public static final long TIME_BAD = 3600000; //1 h
	public static final long TIME_UMBRAL = 86400000; //24h
	
	// Claves referidas a los nombres de la columnas de la bbdd
	public static final String ID_PUNTO = "id_punto";
	public static final String USUARIO = "usuario";
	public static final String LATITUD = "latitud";
	public static final String LONGITUD = "longitud";
	public static final String REPUTACION = "reputacion";
	public static final String FECHA = "fecha";
	public static final String EMAIL = "email";
	
	// Claves para enviar objetos JSON al servicio web
	public static final String ACTION = "action";
	public static final String USER = "user";
	public static final String PASSWORD = "password";
	public static final String ID = "id";
	
	
	// Valores
	public static final String RESULT = "result";
	public static final String POINT_NO_MAPPED = "punto_no_mapeado";
	
	// Valores para las acciones de DetailsDialog
	public static final int ACTION_PARK = 1;
	public static final int ACTION_VOTE_POSSITIVE = 2;
	public static final int ACTION_VOTE_NEGATIVE = 3;
	public static final int ACTION_DELETE = 4;

	// Nombre del archivo de preferencias
	public static final String MyPreferences = "MisPreferencias";
	
	// Claves para el archivo de preferencias
	public static final String USUARIO_PREFS = "nombre_usuario";
	public static final String CONTRASENIA_PREFS = "contrasenia_usuario";
	public static final String TIEMPO_REFRESCO = "tiempo_refresco";
	public static final String AREA_BUSQUEDA = "area_busqueda";
	public static final String ORDENAR_LISTA_POR = "ordenar_lista_por";
	public static final String TIEMPO_MAXIMO_EN_DIFUSION = "tiempo_maximo_difusion";
	
	// VALORES PARA LAS OPCIONES DEL MENU	
	// Tiempos de intervalo entre llamadas al servidor de refresco, en segundos
	public static final int TIEMPO_REFRESCO_OPCION_1 = 5;
	public static final int TIEMPO_REFRESCO_OPCION_2 = 15;
	public static final int TIEMPO_REFRESCO_OPCION_3 = 30;
	public static final int TIEMPO_REFRESCO_OPCION_4 = 60;
	
	// Area de busqueda en el que se buscan puntos a partir
	// de la posicion actual, en metros
	public static final int AREA_BUSQUEDA_OPCION_1 = 500;
	public static final int AREA_BUSQUEDA_OPCION_2 = 1000;
	public static final int AREA_BUSQUEDA_OPCION_3 = 1500;
	public static final int AREA_BUSQUEDA_OPCION_4 = 0; //Global
	
	// Opciones de ordenacion de la lista de puntos
	public static final int ORDENAR_LISTA_POR_OPCION_1 = 1; //Tiempo
	public static final int ORDENAR_LISTA_POR_OPCION_2 = 2; //Distancia
	public static final int ORDENAR_LISTA_POR_OPCION_3 = 3; //Nombre
	public static final int ORDENAR_LISTA_POR_OPCION_4 = 4; //Reputacion
	
	// Tiempo maximo desde que se ha difundido un punto
	// para incluirlo en la lista, en segundos
	public static final int TIEMPO_MAXIMO_EN_DIFUSION_OPCION_1 = 600; //10 min
	public static final int TIEMPO_MAXIMO_EN_DIFUSION_OPCION_2 = 1800; //30min
	public static final int TIEMPO_MAXIMO_EN_DIFUSION_OPCION_3 = 3600; //1 h
	public static final int TIEMPO_MAXIMO_EN_DIFUSION_OPCION_4 = 0; //Desde siempre

}
