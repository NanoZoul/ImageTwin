package cl.sodired.ahenriquez.imagetwin;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import cl.sodired.ahenriquez.imagetwin.util.CircleTransform;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by sandi on 19-12-2016.
 */

@Slf4j
public class PicInfo extends AppCompatActivity {

    @BindView(R.id.imageInfo)
    ImageView imagen;

    @BindView(R.id.ubicacionInfo)
    TextView ubicacion;

    @BindView(R.id.negativeInfo)
    TextView negative;

    @BindView(R.id.warningInfo)
    TextView warning;

    @BindView(R.id.positiveInfo)
    TextView positive;

    @BindView(R.id.fechaInfo)
    TextView fecha;

    @BindView(R.id.ciudadPais)
    TextView ciudadPais;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pic_info);
        Context context = getApplicationContext();
        //Formatear Fecha
        //Inicio de ButterKnife
        ButterKnife.bind(this);
        Intent intent = getIntent();
        Log.d("INFOPIC",String.valueOf(intent.getStringExtra("longitud")));
        //Obtener Localizacion
        double latitud = 0;
        double longitud = 0;
        if(intent.getDoubleExtra("latitud",latitud)==0){
            ciudadPais.setText("Sin dirección");
            ubicacion.setText("Sin información ciudad-pais");
        }else {
            List<Address> geo = localizacion(intent.getDoubleExtra("latitud", latitud),
                    intent.getDoubleExtra("longitud", longitud));
            //Dar valores a los elementos
            ciudadPais.setText(geo.get(0).getLocality() + " - " + geo.get(0).getCountryName());
            ubicacion.setText(geo.get(0).getAddressLine(0));
        }
        negative.setText(intent.getStringExtra("negative"));
        positive.setText(intent.getStringExtra("positive"));
        warning.setText(intent.getStringExtra("warning"));
        fecha.setText(intent.getStringExtra("fecha"));
        Picasso.with(context).load("http://192.168.0.14:8181/" + intent.getStringExtra("url")).resize(600,600).centerCrop().into(imagen);
    }

    public List<Address> localizacion(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try{
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            return addresses;
            /*
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL
            */
        }catch (IOException e){
            Log.d("Localizacion", String.valueOf(e));
        }
        return null;
    }
}
