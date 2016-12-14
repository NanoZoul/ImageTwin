package cl.sodired.ahenriquez.imagetwin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import org.apache.commons.lang3.RandomUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cl.sodired.ahenriquez.imagetwin.domain.Pic;
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

    //Adaptador de las imagenes al ListView
    AdaptadorTwin adaptador;
    ArrayList<ItemTwin> listaDeTwins = new ArrayList<>();

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
        adaptador = new AdaptadorTwin(this,listaDeTwins);
        listPics.setAdapter(adaptador);

        //Comunicacion con la API con retrofit - PRUEBA DE GET
        WebService.Factory.getInstance().obtenerPic("pic").enqueue(new Callback<Pic>() {
            @Override
            public void onResponse(Call<Pic> call, Response<Pic> response) {
                Log.d("APIRETURN",String.valueOf(response.body()));
            }
            @Override
            public void onFailure(Call<Pic> call, Throwable t) {
                Log.d("APIRETURN",String.valueOf(t));
            }
        });
        //CREACION DE PIC DE PRUEBA
        final Pic pic2 = Pic.builder()
                .deviceId(DeviceUtils.getDeviceId(getApplicationContext()))
                .latitude(RandomUtils.nextDouble())
                .longitude(RandomUtils.nextDouble())
                .date(new Date().getTime())
                .url(this.mpath)
                .positive(RandomUtils.nextInt(0, 100))
                .negative(RandomUtils.nextInt(0, 100))
                .warning(RandomUtils.nextInt(0, 2))
                .build();
        //PRUEBA DE POST
        WebService.Factory.getInstance().sendPic(pic2).enqueue(new Callback<Pic>() {
            @Override
            public void onResponse(Call<Pic> call, Response<Pic> response) {
                Log.d("APIRETURN2","DeviceId = " + response.body());
            }
            @Override
            public void onFailure(Call<Pic> call, Throwable t) {
                Log.d("APIRETURN2",String.valueOf(t));
            }
        });

        //Prueba de base de datos
        {
            List<Pic> pics = SQLite.select().from(Pic.class).queryList();

            int i = 0;
            for (final Pic p : pics) {
                ItemTwin nuevoTwin = obtenerTwin(p);
                if(nuevoTwin!=null){
                    adaptador.add(nuevoTwin);
                }
                Log.d(String.valueOf(p.getId()) + " - ID: ", String.valueOf(p.getId()));
                Log.d(String.valueOf(p.getId()) + " - Device: ", String.valueOf(p.getDeviceId()));
                Log.d(String.valueOf(p.getId()) + " - Url: ", String.valueOf(p.getUrl()));
                Log.d(String.valueOf(p.getId()) + " - Latitud: ", String.valueOf(p.getLatitude()));
                Log.d(String.valueOf(p.getId()) + " - Longitud: ", String.valueOf(p.getLongitude()));
                Log.d(String.valueOf(p.getId()) + " - Date: ", String.valueOf(p.getDate()));
                Log.d(String.valueOf(p.getId()) + " - Positive: ", String.valueOf(p.getPositive()));
                Log.d(String.valueOf(p.getId()) + " - Negative: ", String.valueOf(p.getNegative()));
                Log.d(String.valueOf(p.getId()) + " - Warning: ", String.valueOf(p.getWarning()));
                log.debug("Prueba1","aksdjas");
                i++;
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
            //Redimension de la imagen
            Bitmap bitmap = BitmapFactory.decodeFile(this.mpath);
            Bitmap bitmapResize = Bitmap.createScaledBitmap(bitmap,600,600,true);
            //Redondeo de la imagen
            RoundedBitmapDrawable imagenRedonda1 =
                    RoundedBitmapDrawableFactory.create(getResources(), bitmapResize);
            imagenRedonda1.setCornerRadius(bitmapResize.getHeight());
            //Se crea el item twin que sera mostrado
            ItemTwin nuevoTwin = new ItemTwin(imagenRedonda1,imagenRedonda1);
            adaptador.add(nuevoTwin);
            //Crear pic y guardar en la BD
            final Pic pic = Pic.builder()
                    .deviceId(DeviceUtils.getDeviceId(context))
                    .latitude(RandomUtils.nextDouble())
                    .longitude(RandomUtils.nextDouble())
                    .date(new Date().getTime())
                    .url(this.mpath)
                    .positive(RandomUtils.nextInt(0, 100))
                    .negative(RandomUtils.nextInt(0, 100))
                    .warning(RandomUtils.nextInt(0, 2))
                    .build();
            // Commit
            pic.save();

        }
    }

    /**
     * Metodo que al darle un pic, me entrega el Twin
     * @param pic
     * @return
     */
    private ItemTwin obtenerTwin(Pic pic){
        String path = pic.getUrl();
        //Redimension de la imagen
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if(bitmap!=null){
            Bitmap bitmapResize = Bitmap.createScaledBitmap(bitmap,600,600,true);
            //Redondeo de la imagen
            RoundedBitmapDrawable imagenRedonda1 =
                    RoundedBitmapDrawableFactory.create(getResources(), bitmapResize);
            imagenRedonda1.setCornerRadius(bitmapResize.getHeight());
            //Se crea el item twin que sera mostrado
            ItemTwin nuevoTwin = new ItemTwin(imagenRedonda1,imagenRedonda1);
            return nuevoTwin;
        }
        return null;
    }

}
