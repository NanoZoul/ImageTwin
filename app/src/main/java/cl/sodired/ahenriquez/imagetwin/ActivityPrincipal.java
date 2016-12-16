package cl.sodired.ahenriquez.imagetwin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.RandomUtils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import cl.sodired.ahenriquez.imagetwin.domain.Pic;
import cl.sodired.ahenriquez.imagetwin.domain.Twin;
import cl.sodired.ahenriquez.imagetwin.service.WebService;
import cl.sodired.ahenriquez.imagetwin.util.AdaptadorTwin;
import cl.sodired.ahenriquez.imagetwin.util.DeviceUtils;
import cl.sodired.ahenriquez.imagetwin.util.ItemTwin;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Slf4j
public class ActivityPrincipal extends AppCompatActivity {

    /**
     * Atributos y variables locales
     */

    //Variables para directorio donde se almacenaran las imagenes
    private static String APP_DIRECTORY = "MyPictureApp/";
    private static String MEDIA_DIRECTORY = APP_DIRECTORY + "PictureApp";

    //Variable del Path de cada imagen
    String mpath = "";

    //ListView de las pic
    @BindView(R.id.listViewPics)
    ListView listPics;

    //Boton del menu que acciona la camara
    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    //Adaptador de las imagenes al ListView
    AdaptadorTwin adaptador;
    ArrayList<ItemTwin> listaDeTwins = new ArrayList<>();

    //Pic Holder
    Pic picHolder;


    /**
     * Metodo on create
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        //Inicio de ButterKnife
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        //Barra de navegacion
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nombreimagen = getCode() + ".jpg";
                mpath = Environment.getExternalStorageDirectory() + File.separator + MEDIA_DIRECTORY
                        + File.separator + nombreimagen;
                File mi_foto = new File(mpath);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mi_foto));
                startActivityForResult(intent, 200);
            }
        });
        //Asignacion del adaptador
        adaptador = new AdaptadorTwin(this,listaDeTwins);
        listPics.setAdapter(adaptador);

        //Mostar los Twin almacenados en la BD
        {
            List<Twin> twins = SQLite.select().from(Twin.class).queryList();
            int i = 0;
            for (final Twin t : twins) {
                ItemTwin nuevoItemTwin = obtenerItemTwin(t);
                if(nuevoItemTwin!=null){
                    adaptador.add(nuevoItemTwin);
                    log.debug(String.valueOf(t.getLocal().getId()) + " - DeviceLocal: ", String.valueOf(t.getLocal().getDeviceId()));
                    log.debug(String.valueOf(t.getLocal().getId()) + " - UrlLocal: ", String.valueOf(t.getLocal().getUrl()));
                    log.debug(String.valueOf(t.getRemote().getId()) + " - DeviceRemoto: ", String.valueOf(t.getRemote().getDeviceId()));
                    log.debug(String.valueOf(t.getRemote().getId()) + " - UrlRemota: ", String.valueOf(t.getRemote().getUrl()));
                    i++;
                }
            }
        }

    }

    /**
     * Metodo que muestra el BarMenu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_principal, menu);
        return true;
    }

    /**
     * Metodo para darle las funciones al bar menu
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Metodo privado que genera un codigo unico segun la hora y fecha del sistema
     * @return photoCode
     **/
    @SuppressLint("SimpleDateFormat")
    private String getCode()
    {
        //Atributo estatico de la clase
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String date = dateFormat.format(new Date() );
        String photoCode = "pic_" + date;
        return photoCode;
    }

    /**
     * Al momento que se termine de tomar la foto, guardara el path
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("file_path", mpath);
    }

    /**
     * El path que se guardo, vuelve a iniciarse
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.mpath = savedInstanceState.getString("file_path");
    }

    /**
     * Respuesta del activity de la camara
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Context context = getApplicationContext();
        if (resultCode == RESULT_OK) {
            MediaScannerConnection.scanFile(this,
                    new String[]{this.mpath}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            //log.debug("ExternalStorage", "Scanned " + path + ":");
                            //log.debug("ExternalStorage", "-> Uri = " + uri);
                        }
                    });
            //Crear arreglo de byte para enviar a php
            //Bitmap bmp = BitmapFactory.decodeFile(mpath);
            //Achicar bitmap
            //Bitmap resized = Bitmap.createScaledBitmap(bmp, 300, 300, true);
            //ByteArrayOutputStream stream = new ByteArrayOutputStream();
            //resized.compress(Bitmap.CompressFormat.PNG, 100, stream);
            //byte[] byteArray = stream.toByteArray();
            //Ubicacion al momento de tomar la foto
            double [] ubicacion = obtenerUbicacion();
            log.debug("UBICACION1",String.valueOf(ubicacion[0]));
            log.debug("UBICACION2",String.valueOf(ubicacion[1]));
            //Crear pic y guardar en la BD
            picHolder = Pic.builder()
                    .deviceId(DeviceUtils.getDeviceId(context))
                    .latitude(ubicacion[0])
                    .longitude(ubicacion[1])
                    .date(new Date().getTime())
                    .url(this.mpath)
                    .positive(0)
                    .negative(0)
                    .warning(0)
                    .build();
            // Commit
            picHolder.save();

            generarTwinBD();

        }
    }

    /**
     * Metodo que al darle un twin, me entrega un ItemTwin
     * @param twin
     * @return
     */
    private ItemTwin obtenerItemTwin(Twin twin){
        if(twin.getLocal().getUrl().isEmpty()||twin.getRemote().getUrl().isEmpty()){
            return null;
        }
        String pathLocal = twin.getLocal().getUrl();
        String pathRemote = twin.getRemote().getUrl();
        //Se crea el item twin que sera mostrado
        ItemTwin nuevoTwin = new ItemTwin(pathLocal,pathRemote);
        return nuevoTwin;
    }

    /**
     * Genera un twin, obtenido desde la BD remota
     */
    private void generarTwinBD(){
        //Conexion con la base de datos remota
       /*WebService.Factory.getInstance().sendPic(pic).enqueue(new Callback<Twin>() {
            @Override
            public void onResponse(Call<Twin> call, Response<Twin> response) {

            }
            @Override
            public void onFailure(Call<Twin> call, Throwable t) {
                log.debug("APIRETURN2",String.valueOf(t));
            }
        });*/

        //Comunicacion con la API con retrofit - GET
        WebService.Factory.getInstance().obtenerPic("pic").enqueue(new Callback<Pic>() {
            @Override
            public void onResponse(Call<Pic> call, Response<Pic> response) {
                if(response.body().getUrl()!=null){
                    almacenarPicDesdeBD(response.body().getUrl());
                }else{
                    almacenarPicDesdeBD("none");
                }
            }
            @Override
            public void onFailure(Call<Pic> call, Throwable t) {
                log.debug("APIRETURN",String.valueOf(t));
            }
        });


    }

    /**
     * Permite encontrar la ubicacion a traves del servicio de GPS
     * @return
     */
    @SuppressWarnings({"MissingPermission"})
    public double[] obtenerUbicacion(){
        Criteria criteria = new Criteria();
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = mlocManager.getLastKnownLocation(mlocManager
                .getBestProvider(criteria, false));
        if(location!=null){
            double latitude = location.getLatitude();
            double longitud = location.getLongitude();
            double [] ubicacion = new double[2];
            ubicacion[0] = latitude;
            ubicacion[1] = longitud;
            return ubicacion;
        }
        double [] ubicacion = new double[2];
        ubicacion[0]=0;
        ubicacion[1]=0;
        return ubicacion;

    }

    public void almacenarPicDesdeBD(String url){
        Pic nuevoPic = Pic.builder()
                .url(url)
                .build();
        nuevoPic.save();

        Twin nuevoTwin = Twin.builder()
                .local(picHolder)
                .remote(nuevoPic)
                .build();
        nuevoTwin.save();

        //Muestro el twin recien creado
        ItemTwin nuevoItemTwin = obtenerItemTwin(nuevoTwin);
        if(url=="none"){
            ItemTwin issues = new ItemTwin(nuevoTwin.getLocal().getUrl(),"none");
            adaptador.add(issues);
        }else{
            adaptador.add(nuevoItemTwin);
        }

    }
}
