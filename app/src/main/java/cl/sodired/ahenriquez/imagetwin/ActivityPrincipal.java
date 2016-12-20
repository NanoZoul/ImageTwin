package cl.sodired.ahenriquez.imagetwin;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
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
import cl.sodired.ahenriquez.imagetwin.util.PackageUtils;
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

    //Adaptador de las imagenes al ListView
    AdaptadorTwin adaptador;
    ArrayList<ItemTwin> listaDeTwins = new ArrayList<>();

    //ProgressDialog
    ProgressDialog cargando;

    //Varible permisos GPS
    private static final int MY_PERMISSION_ACCESS_COURSE_LOCATION = 11;

    //Instancia de las utilidades
    PackageUtils pu = new PackageUtils();

    //View para el mensaje popup
    @BindView(android.R.id.content)
    View view;


    /**
     * Metodo on create
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Inicio de ButterKnife
        ButterKnife.bind(this);
        //Mensaje Cargando
        cargando = new ProgressDialog(this);
        cargando.setTitle("Iniciando");
        cargando.setMessage("Por favor espere...");
        cargando.show();
        //Barra de navegacion
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pu.isNetDisponible(getApplicationContext()) && pu.isOnlineNet()) {
                    String nombreimagen = getCode() + ".jpg";
                    mpath = Environment.getExternalStorageDirectory() + File.separator + MEDIA_DIRECTORY
                            + File.separator + nombreimagen;
                    File mi_foto = new File(mpath);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mi_foto));
                    startActivityForResult(intent, 200);
                }else{
                    Snackbar.make(view, "Ops! No hemos detectado conexion de red. Verifica tu conexión", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
        //Asignacion del adaptador
        adaptador = new AdaptadorTwin(this,listaDeTwins);
        listPics.setAdapter(adaptador);
        if(pu.isNetDisponible(getApplicationContext()) && pu.isOnlineNet()) {
            //Mostar los Twin almacenados en la BD
            {
                List<Twin> twins = SQLite.select().from(Twin.class).queryList();
                int i = 0;
                if(twins.size()>0){
                    for (final Twin t : twins) {
                        ItemTwin nuevoItemTwin = obtenerItemTwin(t);
                        if (nuevoItemTwin != null) {
                            adaptador.add(nuevoItemTwin);
                            Log.d(String.valueOf(t.getLocal().getId()) + " - DeviceLocal: ", String.valueOf(t.getLocal().getDeviceId()));
                            Log.d(String.valueOf(t.getLocal().getId()) + " - UrlLocal: ", String.valueOf(t.getLocal().getUrl()));
                            Log.d(String.valueOf(t.getRemote().getId()) + " - DeviceRemoto: ", String.valueOf(t.getRemote().getDeviceId()));
                            Log.d(String.valueOf(t.getRemote().getId()) + " - UrlRemota: ", String.valueOf(t.getRemote().getUrl()));
                            i++;
                        }
                    }
                }else{
                    Snackbar.make(view, "Bienvenido, para comenzar toma una foto", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                cargando.dismiss();
            }
        }else{
            cargando.dismiss();
            Snackbar.make(view, "Ops! No hemos detectado conexion de red. Verifica tu conexión", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
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
            //Ubicacion al momento de tomar la foto
            double [] ubicacion = obtenerUbicacion();
            Log.d("UBICACION",String.valueOf(ubicacion[0]));

            //Codificar la imagen en octal
            Bitmap resized = escalarImagen(BitmapFactory.decodeFile(mpath));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            byte[] b = baos.toByteArray();
            String imagen = Base64.encodeToString(b,Base64.DEFAULT);

            //Crear pic y guardar en la BD
            final Pic pic = Pic.builder()
                    .deviceId(DeviceUtils.getDeviceId(context))
                    .latitude(ubicacion[0])
                    .longitude(ubicacion[1])
                    .fecha(new Date().getTime())
                    .url(this.mpath)
                    .positive(0)
                    .negative(0)
                    .warning(0)
                    .imagen(imagen)
                    .build();
            //Genero un nuevo twin con el pic creado
            generarTwinBD(pic);
        }
    }

    /**
     * Metodo que al darle un twin, me entrega un ItemTwin
     * @param twin
     * @return
     */
    private ItemTwin obtenerItemTwin(Twin twin){
        //Se crea el item twin que sera mostrado
        return new ItemTwin(twin.getLocal(),twin.getRemote());
    }

    private void generarTwinBD(Pic pic){
        //POST
        //Comprobamos si hay internet diponible
        if(pu.isNetDisponible(getApplicationContext()) && pu.isOnlineNet()){
            cargando = new ProgressDialog(this);
            cargando.setTitle("Conectando");
            cargando.setMessage("Por favor espere...");
            cargando.show();
           WebService.Factory.getInstance().sendPic(pic).enqueue(new Callback<Twin>() {
                @Override
                public void onResponse(Call<Twin> call, Response<Twin> response) {
                        final Pic picRemoto = response.body().getRemote();
                        picRemoto.save();
                        final Pic picLocal = response.body().getLocal();
                        picLocal.save();
                        final Twin nuevoTwin = Twin.builder()
                                .local(picLocal)
                                .remote(picRemoto)
                                .build();
                        nuevoTwin.save();
                        Log.d("MYERROR",String.valueOf(nuevoTwin));
                        ItemTwin nuevoItemTwin = obtenerItemTwin(nuevoTwin);
                        adaptador.add(nuevoItemTwin);
                        cargando.dismiss();
                }
                @Override
                public void onFailure(Call<Twin> call, Throwable t) {
                    Log.d("APIRETURN2",String.valueOf(t));
                    cargando.dismiss();
                    Snackbar.make(view, "Ops! Tenemos problemas para acceder a nuestra base de datos.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        }else{
            View view = findViewById(android.R.id.content);
            Snackbar.make(view, "Ops! No hemos detectado conexion de red. Verifica tu conexión", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
        //Comunicacion con la API con retrofit - PRUEBA DE GET
        /*WebService.Factory.getInstance().obtenerPic("pic").enqueue(new Callback<Pic>() {
            @Override
            public void onResponse(Call<Pic> call, Response<Pic> response) {
                if(response.body().getUrl()!=null) {
                    Log.d("APIRETURN",String.valueOf(response.body()));
                    picHolder = response.body();
                    almacenarPicDesdeBD();
                }else{
                    almacenarPicDesdeBD();
                    Log.d("AQUIPASE2","none");
                }
            }
            @Override
            public void onFailure(Call<Pic> call, Throwable t) {
                Log.d("APIRETURN",String.valueOf(t));
            }
        });*/
    }

    public double[] obtenerUbicacion(){
        Criteria criteria = new Criteria();
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    MY_PERMISSION_ACCESS_COURSE_LOCATION);
        }
        Location location = mlocManager.getLastKnownLocation(mlocManager
                .getBestProvider(criteria, false));
        double [] ubicacion = new double[2];
        if(location!=null){
            double latitude = location.getLatitude();
            double longitud = location.getLongitude();

            ubicacion[0] = latitude;
            ubicacion[1] = longitud;
        }else{
            ubicacion[0]= 0;
            ubicacion[1]= 0;
        }
        return ubicacion;
    }

    public Bitmap escalarImagen(Bitmap myBitmap){
        final int maxSize = 600;
        int outWidth;
        int outHeight;
        int inWidth = myBitmap.getWidth();
        int inHeight = myBitmap.getHeight();
        if(inWidth > inHeight){
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(myBitmap, outWidth, outHeight, false);
        return resizedBitmap;
    }
}
