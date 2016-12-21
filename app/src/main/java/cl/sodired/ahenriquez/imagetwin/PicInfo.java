package cl.sodired.ahenriquez.imagetwin;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import cl.sodired.ahenriquez.imagetwin.domain.Pic;
import cl.sodired.ahenriquez.imagetwin.domain.Pic_Table;
import cl.sodired.ahenriquez.imagetwin.service.WebService;
import cl.sodired.ahenriquez.imagetwin.util.CircleTransform;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by sandi on 19-12-2016.
 */

@Slf4j
public class PicInfo extends AppCompatActivity {

    //View para el mensaje popup
    @BindView(android.R.id.content)
    View view;

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

    @BindView(R.id.likeBtn)
    Button likeBtn;

    @BindView(R.id.warnBtn)
    Button warnBtn;

    @BindView(R.id.dislikeBtn)
    Button dislikeBtn;

    int idRemota;
    int likes;
    int dislikes;
    int warnings;

    /**
     * Metodo onCreate del activity que muestra la info del pic
     * @param savedInstanceState instancia, bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pic_info);
        Context context = getApplicationContext();

        //Inicio de ButterKnife
        ButterKnife.bind(this);
        Intent intent = getIntent();

        //Valores de Likes, dislikes y Warning
        idRemota = intent.getIntExtra("idRemota",idRemota);
        likes = intent.getIntExtra("positive",likes);
        dislikes = intent.getIntExtra("negative",dislikes);
        warnings = intent.getIntExtra("warning",warnings);

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
        negative.setText(String.valueOf(dislikes));
        positive.setText(String.valueOf(likes));
        warning.setText(String.valueOf(warnings));
        fecha.setText(intent.getStringExtra("fecha"));
        if(intent.getStringExtra("tipo").equals("usuario")){
            Picasso.with(context).load("http://172.20.10.4:8181/" + intent.getStringExtra("url")).rotate(-90).resize(600,600).centerCrop().into(imagen);
        }else{
            Picasso.with(context).load("http://172.20.10.4:8181/" + intent.getStringExtra("url")).resize(600,600).centerCrop().into(imagen);
        }


        likeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sumar("like",idRemota);
            }
        });

        dislikeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sumar("dislike",idRemota);
            }
        });

        warnBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sumar("warn",idRemota);
            }
        });
    }

    /**
     * Metodo que entrega informacion de acuerdo a las coordenadas
     * @param latitude latitud recibida por el GPS
     * @param longitude longitud recibida por el GPS
     * @return null o adresses dependiendo si se hubico o no.
     */
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

        }
        return null;
    }

    /**
     * Agrega like, dislike o warn.
     * @param tipo es es tipo de reaccion
     * @param idRemota es la id de la pic en la BD remota
     */
    private void sumar(String tipo, final int idRemota){
        switch (tipo){
            case "like":
                WebService.Factory.getInstance().incrementarLikes(idRemota).enqueue(new Callback<Integer>() {
                    @Override
                    public void onResponse(Call<Integer> call, Response<Integer> response) {
                        likes = likes+1;
                        positive.setText(String.valueOf(likes));
                        Pic pic = SQLite.select().from(Pic.class).where(Pic_Table.idRemota.is(idRemota)).querySingle();
                        pic.setPositive(likes);
                        pic.save();
                    }
                    @Override
                    public void onFailure(Call<Integer> call, Throwable t) {
                        Snackbar.make(view, "Ops! Tenemos problemas para acceder a nuestra base de datos.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
            break;
            case "dislike":

                WebService.Factory.getInstance().incrementarDislikes(idRemota).enqueue(new Callback<Integer>() {
                    @Override
                    public void onResponse(Call<Integer> call, Response<Integer> response) {
                        dislikes = dislikes + 1;
                        negative.setText(String.valueOf(dislikes));
                        Pic pic = SQLite.select().from(Pic.class).where(Pic_Table.idRemota.is(idRemota)).querySingle();
                        pic.setNegative(dislikes);
                        pic.save();
                    }
                    @Override
                    public void onFailure(Call<Integer> call, Throwable t) {
                        Snackbar.make(view, "Ops! Tenemos problemas para acceder a nuestra base de datos.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                    }
                });
            break;
            case "warn":

                WebService.Factory.getInstance().incrementarWarn(idRemota).enqueue(new Callback<Integer>() {
                    @Override
                    public void onResponse(Call<Integer> call, Response<Integer> response) {
                        warnings = warnings + 1;
                        warning.setText(String.valueOf(warnings));
                        Pic pic = SQLite.select().from(Pic.class).where(Pic_Table.idRemota.is(idRemota)).querySingle();
                        pic.setWarning(warnings);
                        pic.save();
                    }
                    @Override
                    public void onFailure(Call<Integer> call, Throwable t) {
                        Snackbar.make(view, "Ops! Tenemos problemas para acceder a nuestra base de datos.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
            break;
            default:
            break;
        }
    }
}
