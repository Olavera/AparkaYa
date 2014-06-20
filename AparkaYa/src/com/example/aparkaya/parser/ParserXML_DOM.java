package com.example.aparkaya.parser;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.example.aparkaya.model.WebPoint;
import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.util.Log;

public class ParserXML_DOM{

	private static String FICHERO = "puntos.xml";

	private Context contexto;

	private Document documento;

	private boolean cargadoDocumento;



	public ParserXML_DOM(Context contexto) {

		this.contexto = contexto;

		cargadoDocumento = false;

	}


	public void guardarPunto (String nombre, LatLng cords) {

		try {

			if (!cargadoDocumento) {

				leerXML (contexto.openFileInput (FICHERO) );

			}

		} catch (FileNotFoundException e) {

			crearXML ();

		} catch (Exception e) {

			Log.e ("Puntos", e.getMessage(), e);

		}

		nuevo (nombre, cords);

		try {

			escribirXML(contexto.openFileOutput (FICHERO,
					Context.MODE_PRIVATE));

		} catch (Exception e) {

			Log.e("Puntos", e.getMessage(), e);

		}

	}
	
	public void eliminarPunto (String nombre, LatLng cords) {

		try {

			if (!cargadoDocumento) {

				leerXML (contexto.openFileInput (FICHERO) );

			}

		} catch (FileNotFoundException e) {

			crearXML ();

		} catch (Exception e) {

			Log.e ("Puntos", e.getMessage(), e);

		}

		quitar(nombre, cords);

		try {

			escribirXML(contexto.openFileOutput (FICHERO,
					Context.MODE_PRIVATE));

		} catch (Exception e) {

			Log.e("Puntos", e.getMessage(), e);

		}

	}

	public Vector<WebPoint> listaPuntos() {

		try {

			if (!cargadoDocumento) {

				leerXML(contexto.openFileInput (FICHERO));

			}

		} catch (FileNotFoundException e) {

			crearXML();

		} catch (Exception e) {

			Log.e("Puntos", e.getMessage(), e);

		}

		return aVectorString();

	}

	private void crearXML() {

		try {

			DocumentBuilderFactory fabrica =
					DocumentBuilderFactory.newInstance();

			DocumentBuilder constructor = fabrica.newDocumentBuilder();

			documento = constructor.newDocument();

			Element raiz = documento.createElement("lista_puntos");

			documento.appendChild(raiz);

			cargadoDocumento = true;

		} catch(Exception e) {

			Log.e("Puntos", e.getMessage(), e);

		}

	}



	private void leerXML(InputStream entrada) throws Exception {

		DocumentBuilderFactory fabrica =

				DocumentBuilderFactory.newInstance();

		DocumentBuilder constructor = fabrica.newDocumentBuilder();

		documento = constructor.parse(entrada);

		cargadoDocumento = true;

	}


	private void nuevo(String nombre, LatLng cords) {

		Element punto = documento.createElement("punto");

		Element e_nombre = documento.createElement("nombre");

		Text texto = documento.createTextNode(nombre);

		e_nombre.appendChild(texto);

		punto.appendChild(e_nombre);

		Element e_lat = documento.createElement("latitud");

		texto = documento.createTextNode(String.valueOf(cords.latitude));

		e_lat.appendChild(texto);

		punto.appendChild(e_lat);
		
		Element e_lng = documento.createElement("longitud");

		texto = documento.createTextNode(String.valueOf(cords.longitude));

		e_lng.appendChild(texto);

		punto.appendChild(e_lng);

		Element raiz = documento.getDocumentElement();

		raiz.appendChild(punto);

	}

	private void quitar(String nombre, LatLng cords) {
		
		String nom = "";
		Double lat = 0.0;
		Double lng = 0.0;
		
		Element raiz = documento.getDocumentElement();

		NodeList puntos = raiz.getElementsByTagName("punto");

		for(int i = 0; i < puntos.getLength(); i++) {

			Node punto = puntos.item(i);

			NodeList propiedades = punto.getChildNodes();

			for(int j = 0; j < propiedades.getLength(); j++) {

				Node propiedad = propiedades.item(j);

				String etiqueta = propiedad.getNodeName();

				if(etiqueta.equals("nombre")) {

					nom = propiedad.getFirstChild().getNodeValue();

				} else if(etiqueta.equals("latitud")) {

					lat = Double.parseDouble(propiedad.getFirstChild().getNodeValue());

				} else if(etiqueta.equals("longitud")) {

					lng= Double.parseDouble(propiedad.getFirstChild().getNodeValue());

				}
			}
			
			if (nom.equals(nombre) && (lat == cords.latitude) && (lng == cords.longitude))
			{
				raiz.removeChild(punto);
				break;
			}

		}

	}
	

	private Vector<WebPoint> aVectorString() {

		Vector<WebPoint> result = new Vector<WebPoint>();

		String nombre = "";
		Double lat = 0.0;
		Double lng = 0.0;

		Element raiz = documento.getDocumentElement();

		NodeList puntos = raiz.getElementsByTagName("punto");

		for(int i = 0; i < puntos.getLength(); i++) {

			Node punto = puntos.item(i);

			NodeList propiedades = punto.getChildNodes();

			for(int j = 0; j < propiedades.getLength(); j++) {

				Node propiedad = propiedades.item(j);

				String etiqueta = propiedad.getNodeName();

				if(etiqueta.equals("nombre")) {

					nombre = propiedad.getFirstChild().getNodeValue();

				} else if(etiqueta.equals("latitud")) {

					lat = Double.parseDouble(propiedad.getFirstChild().getNodeValue());

				} else if(etiqueta.equals("longitud")) {

					lng= Double.parseDouble(propiedad.getFirstChild().getNodeValue());

				}

			}

			result.add(new WebPoint(1,nombre, new LatLng(lat, lng),0)); // MODIFICADO a 0 hay que cambiarlo

		}

		return result;

	}

	private void escribirXML(OutputStream salida) throws Exception {
		TransformerFactory fabrica = TransformerFactory.newInstance();

		Transformer transformador = fabrica.newTransformer();

		transformador.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");

		transformador.setOutputProperty(OutputKeys.INDENT, "yes");

		DOMSource fuente = new DOMSource(documento);

		StreamResult resultado = new StreamResult(salida);

		transformador.transform(fuente, resultado);

	}

}
