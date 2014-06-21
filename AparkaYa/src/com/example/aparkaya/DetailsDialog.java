package com.example.aparkaya;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.aparkaya.model.WebPoint;
import com.example.aparkaya.webService.HttpPostAux;
import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class DetailsDialog extends Activity {
	WebPoint wp;
	HttpPostAux post;
	String user, pass;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.details_dialog);
		
		post = new HttpPostAux();
		user = getIntent().getStringExtra("user");
		pass = getIntent().getStringExtra("pass");

		String fechaString = getIntent().getExtras().getString(Constants.FECHA);
		SimpleDateFormat dateFormat0 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
		SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM", Locale.getDefault());
		SimpleDateFormat dateFormat2 = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date fecha;
        try{
        	fecha = dateFormat0.parse(fechaString);
		} catch (ParseException e) {
			fecha = null;
		}
		wp = new WebPoint(
			getIntent().getExtras().getInt(Constants.ID_PUNTO),	
			getIntent().getExtras().getString(Constants.USUARIO),
			new LatLng(
					getIntent().getExtras().getDouble(Constants.LONGITUD),
					getIntent().getExtras().getDouble(Constants.LATITUD)),
			getIntent().getExtras().getInt(Constants.REPUTACION),
			fecha);
		
		Button btnAparcar = (Button) findViewById(R.id.btnOcupar);
		Button btnPositivo = (Button) findViewById(R.id.btnVotarPositivo);
		Button btnNegativo = (Button) findViewById(R.id.btnVotarNegativo);
		Button btnEliminar = (Button) findViewById(R.id.btnEliminar);
		
		if(user.equals(wp.getUsuario()))
		{
			btnAparcar.setEnabled(false);
			btnPositivo.setEnabled(false);
			btnNegativo.setEnabled(false);
		}
		else
		{
			btnEliminar.setEnabled(false);
		}
		
		TextView tvContUser = (TextView)findViewById(R.id.tvContUser);
		TextView tvFecha = (TextView)findViewById(R.id.tvContFecha);
		TextView tvContLongitude = (TextView)findViewById(R.id.tvContLongitude);
		TextView tvContLatitude = (TextView)findViewById(R.id.tvContLatitude);
		TextView tvContReputation = (TextView)findViewById(R.id.tvContReputation);
		
		DecimalFormat df = new DecimalFormat("#.######");
		tvContUser.setText(wp.getUsuario());
		tvFecha.setText(" a las " + dateFormat2.format(wp.getFecha()) + " el " + dateFormat1.format(wp.getFecha()));
		tvContLongitude.setText(df.format(wp.getCords().longitude));
		tvContLatitude.setText(df.format(wp.getCords().latitude));
		tvContReputation.setText(Integer.toString(wp.getReputacion()));
	}
	
	public void Aparcar(View v) {
		new asyncPointActions().execute(Constants.ACTION_PARK);
	}
	
	public void votarPositivo(View v) {
		new asyncPointActions().execute(Constants.ACTION_VOTE_POSSITIVE);
	}
	
	public void votarNegativo(View v) {
		new asyncPointActions().execute(Constants.ACTION_VOTE_NEGATIVE);
	}
	
	public void Eliminar(View v) {
		new asyncPointActions().execute(Constants.ACTION_DELETE);
	}
	
	private class asyncPointActions extends AsyncTask< Integer, String, Integer > {

		protected Integer doInBackground(Integer... params) {
			//obtenemos user y pass
			int action = params[0];

			int id=-1;

			ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();
			postparameters2send.add(new BasicNameValuePair("user",user));
			postparameters2send.add(new BasicNameValuePair("password",pass));
			postparameters2send.add(new BasicNameValuePair(Constants.ACTION, Integer.toString(action)));
			postparameters2send.add(new BasicNameValuePair(Constants.ID_PUNTO, Integer.toString(wp.getId_punto())));

			//realizamos una peticion y como respuesta obtenes un array JSON
			JSONArray jdata=post.getserverdata(postparameters2send, "http://aparkaya.webcindario.com/cambiarPuntos.php");

			//si lo que obtuvimos no es null
			if (jdata!=null && jdata.length() > 0){

				JSONObject json_data; //creamos un objeto JSON
				try {
					json_data = jdata.getJSONObject(0); //leemos el primer segmento en nuestro caso el unico
					id=json_data.getInt("id");//accedemos al valor 
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}              

				//validamos el valor obtenido
				if (id==1){           
					return Constants.RESULT_OK; //login valido
				}
				else{
					return Constants.RESULT_FAILED_UPDATE_POINT; //usuario no registrado
				}

			}     
			return Constants.RESULT_ERR; //login invalido 

		}

		protected void onPostExecute(Integer result) {

			if (result==Constants.RESULT_OK){
				Toast.makeText(getApplicationContext(),"Punto actualizado", Toast.LENGTH_SHORT).show();
				finish();
			} 
			else if(result==Constants.RESULT_FAILED_UPDATE_POINT){
				Toast.makeText(getApplicationContext(),"Error al actualizar punto", Toast.LENGTH_SHORT).show();
			}
			else{
				Toast.makeText(getApplicationContext(),"No se pudo conectar con el servidor", Toast.LENGTH_SHORT).show();
			}
		}    

	}
	

}
