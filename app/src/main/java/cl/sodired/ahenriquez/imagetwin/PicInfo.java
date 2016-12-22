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
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import butterknife.BindView;
import butterknife.ButterKnife;
import cl.sodired.ahenriquez.imagetwin.domain.Pic;
import cl.sodired.ahenriquez.imagetwin.domain.Pic_Table;
import cl.sodired.ahenriquez.imagetwin.service.WebService;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Public class PicInfo, corresponde a la activity que se genera al precionar un pic.
 */
@Slf4j
public class PicInfo extends AppCompatActivity {

    /**
     * View correspondiente al mensaje pop-up.
     */
    @BindView(android.R.id.content)
    View view;

    /**
     * ImageView correspondiente a la imagen de este activity.
     */
    @BindView(R.id.imageInfo)
    ImageView imagen;

    /**
     * TextView donde se muestra la direccion.
     */
    @BindView(R.id.ubicacionInfo)
    TextView direccion;

    /**
     * TextView donde se muestran los dislikes.
     */
    @BindView(R.id.negativeInfo)
    TextView negative;

    /**
     * TextView donde se muestran los warnings.
     */
    @BindView(R.id.warningInfo)
    TextView warning;

    /**
     * TextView donde se muestran los likes.
     */
    @BindView(R.id.positiveInfo)
    TextView positive;

    /**
     * TextView de la fecha en que se tomo la foto.
     */
    @BindView(R.id.fechaInfo)
    TextView fecha;

    /**
     * TextView donde se muestra la ciudad y pais.
     */
    @BindView(R.id.ciudadPais)
    TextView ciudadPais;

    /**
     * Boton que permite agregar un like.
     */
    @BindView(R.id.likeBtn)
    Button likeBtn;

    /**
     * Boton que permite agregar un warning.
     */
    @BindView(R.id.warnBtn)
    Button warnBtn;

    /**
     * Boton que permite agregar un dislike.
     */
    @BindView(R.id.dislikeBtn)
    Button dislikeBtn;

    /**
     * Variable correspondiente a la id en la BD Remota.
     */
    int idRemota;

    /**
     * Variable que contiene los likes al iniciar este activity.
     */
    int likes;

    /**
     * Variable que contiene los dislikes al iniciar este activity.
     */
    int dislikes;

    /**
     * Variable que contiene los warnings al iniciar este activity.
     */
    int warnings;

    /**
     * Metodo onCreate del activity que muestra la info del pic.
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

        //Valores de Likes, dislikes y Warning recibidos del intent del activity anterior
        idRemota = intent.getIntExtra("idRemota",idRemota);
        likes = intent.getIntExtra("positive",likes);
        dislikes = intent.getIntExtra("negative",dislikes);
        warnings = intent.getIntExtra("warning",warnings);

        //Obtener Localizacion
        double latitud = 0;
        double longitud = 0;

        //Si la latitud es cero, es decir si no hay ubicacion, muestra sin ubicacion.
        if(intent.getDoubleExtra("latitud",latitud)==0){

            //Se cambian los exos de ciudadPais y direccion
            ciudadPais.setText(R.string.sni_ciudad_pais);
            direccion.setText(R.string.sin_direccion);
        } else {

            //Se crea una lista de Address que almacena todos los datos de la ubicacion.
            List<Address> geo = localizacion(intent.getDoubleExtra("latitud", latitud),
                    intent.getDoubleExtra("longitud", longitud));

            //Dar valores a los elementos ciudadPais y direccion
            ciudadPais.setText(geo.get(0).getLocality() + " - " + geo.get(0).getCountryName());
            direccion.setText(geo.get(0).getAddressLine(0));
        }

        //Dar valores a los elementos de reaccion
        negative.setText(String.valueOf(dislikes));
        positive.setText(String.valueOf(likes));
        warning.setText(String.valueOf(warnings));

        //Dar valor a la fecha
        fecha.setText(intent.getStringExtra("fecha"));

        //Comprobar si el tipo recibido es un usuario, se gira la imagen, de lo contrario no.
        if(intent.getStringExtra("tipo").equals("usuario")){

            //Libreria picasso que muestra la foto del usuario
            Picasso.with(context)
                    .load("http://192.168.0.14:8181/" + intent.getStringExtra("url"))
                    .rotate(-90)
                    .resize(600,600)
                    .centerCrop()
                    .into(imagen);
        } else {

            //Libreria picasso que muestra la foto de la pareja
            Picasso.with(context)
                    .load("http://192.168.0.14:8181/" + intent.getStringExtra("url"))
                    .resize(600,600)
                    .centerCrop()
                    .into(imagen);
        }

        //Listener del boton like
        likeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sumar("like",idRemota);
            }
        });

        //Listener del boton dislike
        dislikeBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sumar("dislike",idRemota);
            }
        });

        //Listener del boton warning
        warnBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                sumar("warn",idRemota);
            }
        });
    }

    /**
     * Metodo que entrega informacion de acuerdo a las coordenadas.
     * @param latitude latitud recibida por el GPS.
     * @param longitude longitud recibida por el GPS.
     * @return null o adresses dependiendo si se ubico o no.
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
            log.debug("Error",e);
        }
        return null;
    }

    /**
     * Agrega like, dislike o warn.
     * @param tipo es es tipo de reaccion.
     * @param idRemota es la id de la pic en la BD remota.
     */
    private void sumar(String tipo, final int idRemota){

        //Switch que discrimina el boton que se preciono para conectar con la BD Remota
        switch (tipo){
            //Agregar un like al pic
            case "like":
                //Metodo POST que envia la id del pic a la API, esta retorna el valor de los likes
                WebService.Factory.getInstance().incrementarLikes(idRemota).enqueue(new Callback<Integer>() {

                    //Respuesta positiva desde el servidor
                    @Override
                    public void onResponse(Call<Integer> call, Response<Integer> response) {

                        //Aumenta la variable de los likes en 1
                        likes = likes+1;

                        //Agrega un like al text view
                        positive.setText(String.valueOf(likes));

                        //Busco el pic donde me encuentro para guardar los nuevos likes
                        Pic pic = SQLite.select()
                                .from(Pic.class)
                                .where(Pic_Table.idRemota.is(idRemota))
                                .querySingle();

                        //Guardo los nevos likes
                        pic.setPositive(likes);

                        //Guardo el pic
                        pic.save();
                    }

                    //Error de conexion con el servidor
                    @Override
                    public void onFailure(Call<Integer> call, Throwable t) {

                        //Se muestra error de conexion con la base de datos
                        Snackbar.make(view, "Ops! Tenemos problemas para acceder a nuestra base de datos.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
            break;

            //Agregar dislike al pic
            case "dislike":

                //Metodo POST que envia la id del pic a la API, esta retorna el valor de los dislikes
                WebService.Factory.getInstance().incrementarDislikes(idRemota).enqueue(new Callback<Integer>() {

                    //Respuesta positiva desde el servidor
                    @Override
                    public void onResponse(Call<Integer> call, Response<Integer> response) {

                        //Aumenta en uno la variable de los dislikes
                        dislikes = dislikes + 1;

                        //Agrega los nuevos dislikes al TextView
                        negative.setText(String.valueOf(dislikes));

                        //Se busca el pic donde me encuentro en la base de datos
                        Pic pic = SQLite.select()
                                .from(Pic.class)
                                .where(Pic_Table.idRemota.is(idRemota))
                                .querySingle();

                        //Se actualizan los likes del Pic
                        pic.setNegative(dislikes);

                        //Se guarda el Pic
                        pic.save();
                    }

                    //Error de conexion con el servidor
                    @Override
                    public void onFailure(Call<Integer> call, Throwable t) {

                        //Se muestra mensaje de error con el servidor
                        Snackbar.make(view, "Ops! Tenemos problemas para acceder a nuestra base de datos.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                    }
                });
            break;
            case "warn":

                //Metodo POST que envia la id del pic a la API, esta retorna el valor de los warning
                WebService.Factory.getInstance().incrementarWarn(idRemota).enqueue(new Callback<Integer>() {

                    //Respuesta positiva del servidor
                    @Override
                    public void onResponse(Call<Integer> call, Response<Integer> response) {

                        //Aumenta la variable de warning en uno.
                        warnings = warnings + 1;

                        //Muestra los nuevos warning en el TextView
                        warning.setText(String.valueOf(warnings));

                        //Se busca la pic donde se encuentra para poder actualizar el warning
                        Pic pic = SQLite.select()
                                .from(Pic.class)
                                .where(Pic_Table.idRemota.is(idRemota))
                                .querySingle();

                        //Se actualizan los warning en el Pic
                        pic.setWarning(warnings);

                        //Se guarda el pic en el BD
                        pic.save();
                    }

                    //Error en la conexion con el servidor
                    @Override
                    public void onFailure(Call<Integer> call, Throwable t) {

                        //Se muestra error de conexion con el servidor
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
