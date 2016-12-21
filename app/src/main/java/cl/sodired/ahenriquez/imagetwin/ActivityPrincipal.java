package cl.sodired.ahenriquez.imagetwin;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import org.apache.commons.io.output.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

/**
 * Actividad principal de la aplicacion.
 */
@Slf4j
public class ActivityPrincipal extends AppCompatActivity {

    /**
     * Variable estatica local que indica la carpeta donde se almacenaran las imagenes.
     */
    private static final String APP_DIRECTORY = "MyPictureApp/";

    /**
     * Variable estatica local que indica el directorio de las imagenes.
     */
    private static final String MEDIA_DIRECTORY = APP_DIRECTORY + "PictureApp";

    /**
     * Variable estatica local que indica los permisos del GPS.
     */
    private static final int MY_PERMISSION_ACCESS_COURSE_LOCATION = 11;

    /**
     * Variable global donde se almacenan los path de cada imagen.
     */
    String mpath = "";

    /**
     * ListView en donde se muestran los items de cada twin.
     */
    @BindView(R.id.listViewPics)
    ListView listPics;

    /**
     * Boton del menu de navegacion que acciona la camara.
     */
    @BindView(R.id.fab)
    FloatingActionButton fab;

    /**
     * Adaptador de las imagenes para mostrarlas en el ListView.
     */
    AdaptadorTwin adaptador;

    /**
     * ArrayList que almacena los Items para agregarlos al adaptador.
     */
    ArrayList<ItemTwin> listaDeTwins = new ArrayList<>();

    /**
     * Ventana de dialogo que se muestra cuando se ingresa una imagen.
     */
    ProgressDialog cargando;

    /**
     * Instancia que permite acceder a las utilidades.
     */
    PackageUtils packageUtils = new PackageUtils();

    /**
     * View para inicializar el mensaje pop-up.
     */
    @BindView(android.R.id.content)
    View view;

    /**
     * Toolbar, instancia de la barra de tareas.
     */
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    /**
     * Metodo on create del activity principal.
     * @param savedInstanceState Bundle nulo que se modifica en el transcurso de la APP.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        //Inicio de la barra de tareas
        setSupportActionBar(toolbar);

        //Inicio de ButterKnife
        ButterKnife.bind(this);

        //Mensaje de carga inicial
        cargando = new ProgressDialog(this);
        cargando.setTitle("Iniciando");
        cargando.setMessage("Por favor espere...");
        cargando.show();

        //Listener de la barra de navegacion
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                //Comprobar si existe conexion de red e internet
                if(packageUtils.isNetDisponible(getApplicationContext()) && packageUtils.isOnlineNet()) {

                    //Generacion del nombre del path y creacion del archivo
                    String nombreImagen = getCode() + ".jpg";
                    mpath = Environment.getExternalStorageDirectory() + File.separator + MEDIA_DIRECTORY
                            + File.separator + nombreImagen;
                    File mi_foto = new File(mpath);

                    //Generacion del intent que se envia al activity result de la camara
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mi_foto));

                    //Iniciar aciviy resl
                    startActivityForResult(intent, 200);

                } else {

                    //En caso de no haber conexion a internet, se envia un mensaje
                    Snackbar.make(view, "Ops! No hemos detectado conexion de red. Verifica tu conexión", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        //Asignacion del adaptador
        adaptador = new AdaptadorTwin(this,listaDeTwins);
        listPics.setAdapter(adaptador);

        //Comprobar conexion a internet
        if(packageUtils.isOnlineNet()) {
            //Mostar los Twin almacenados en la BD
            List<Twin> twins = SQLite.select().from(Twin.class).queryList();

            //Si existen twin en la BD, los muestra
            if(twins.size()>0){
                for (final Twin t : twins) {
                    ItemTwin nuevoItemTwin = obtenerItemTwin(t);
                    if (nuevoItemTwin != null) {
                        adaptador.add(nuevoItemTwin);
                        log.debug(String.valueOf(t.getLocal().getId()) + " - DeviceLocal: ", String.valueOf(t.getLocal().getDeviceId()));
                        log.debug(String.valueOf(t.getLocal().getId()) + " - UrlLocal: ", String.valueOf(t.getLocal().getUrl()));
                        log.debug(String.valueOf(t.getRemote().getId()) + " - DeviceRemoto: ", String.valueOf(t.getRemote().getDeviceId()));
                        log.debug(String.valueOf(t.getRemote().getId()) + " - UrlRemota: ", String.valueOf(t.getRemote().getUrl()));
                    }
                }
            } else {

                //En caso de no haber ningun twin, se envia mensaje de bienvenida
                Snackbar.make(view, "Bienvenido! Para comenzar, toma una fotografía.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }

            //Cierre de venana de carga
            cargando.dismiss();

        }else{

            //Cierre de ventana de carga
            cargando.dismiss();

            //No se ha detectado conexion a internet
            Snackbar.make(view, "Ops! No hemos detectado conexion de red. Verifica tu conexión", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    /**
     * Metodo que muestra el BarMenu.
     * @param menu es el menu recibido para poder mosrar.
     * @return boolean true, indicand que se inicio.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_principal, menu);
        return true;
    }

    /**
     * Metodo para darle las funciones al bar menu.
     * @param item que se envia para capturar boton y realizar una accion.
     * @return retorna true indicando que se realizo la accion.
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
     * Metodo privado que genera un codigo unico segun la hora y fecha del sistema.
     * @return El codigo generado para el nombre de la imagen.
     **/
    @SuppressLint("SimpleDateFormat")
    private String getCode()
    {
        //Generar un formato de fecha para diferencia los nombres de los archivos
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String date = dateFormat.format(new Date() );

        //Retorna el nombre pic al inicio del nombre de la foto
        return "pic_" + date;
    }

    /**
     * Al momento que se termine de tomar la foto, guardara el path.
     * @param outState Bundle que inicia una instancia para guardar el path al salir de la camara.
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("file_path", mpath);
    }

    /**
     * El path que se guardo, vuelve a iniciarse.
     * @param savedInstanceState Bundle que recupera el path al reiniciar la instancia del activity.
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.mpath = savedInstanceState.getString("file_path");
    }

    /**
     * Respuesta del activity de la camara.
     * @param requestCode codigo.
     * @param resultCode indica si se recibio bien la fotografia desde la camara.
     * @param data intent que contiene la informacion desde la camara.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Context context = getApplicationContext();

        //Si el resultado fue OK, escannea el archivo recibido por la camara y realiza las acciones
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

            //Ubicacion GPS al momento de tomar la foto
            double [] ubicacion = obtenerUbicacion();
            log.debug("UBICACION",String.valueOf(ubicacion[0]));

            //Codificacion de la imagen en octal
            Bitmap resized = escalarImagen(BitmapFactory.decodeFile(mpath));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            byte[] b = baos.toByteArray();
            String imagen = Base64.encodeToString(b,Base64.DEFAULT);

            //Crear pic sin guardar en la BD, solo para enviar la informacion al servidor
            final Pic pic = Pic.builder()
                    .deviceId(DeviceUtils.getDeviceId(context))
                    .latitude(ubicacion[0])
                    .longitude(ubicacion[1])
                    .fecha("0")
                    .url(this.mpath)
                    .positive(0)
                    .negative(0)
                    .warning(0)
                    .imagen(imagen)
                    .build();

            //Generar un nuevo twin con el pic creado
            generarTwinBD(pic);
            log.debug("Fecha",String.valueOf(new Date().getTime()));
        }
    }

    /**
     * Metodo que al darle un twin, me entrega un ItemTwin.
     * @param twin recibido para poder obtener el pic local y remoto.
     * @return ItemTwin que sera ingresado al adaptador para ser mostrado.
     */
    private ItemTwin obtenerItemTwin(Twin twin){
        //Se crea el item twin que sera mostrado
        return new ItemTwin(twin.getLocal(),twin.getRemote());
    }

    /**
     * Metodo que genera envia el pic capturado al servidor, luego el servidor envia un Twin
     * el cual es almacenado en la base de datos local. A su vez solicita generar un item twin
     * y lo agrega al adaptador para ser mostrado.
     * @param pic generado por activity result y enviado para ser almacenado en BD remota.
     */
    private void generarTwinBD(Pic pic){

        //Comprobamos si hay red diponible
        if(packageUtils.isNetDisponible(getApplicationContext())){

            //Se muestra ventana de dialogo cargando
            cargando = new ProgressDialog(this);
            cargando.setTitle("Conectando");
            cargando.setMessage("Por favor espere...");
            cargando.show();

            //Metodo POST que envia un pic a la API, la cual retorna un Twin
           WebService.Factory.getInstance().sendPic(pic).enqueue(new Callback<Twin>() {

                //Respuesta positiva desde el servidor
                @Override
                public void onResponse(Call<Twin> call, Response<Twin> response) {

                        //Creacion PicRemoto y guardado en BD Local
                        final Pic picRemoto = response.body().getRemote();
                        picRemoto.save();

                        //Creacion PicLocal y guardado en BD Local
                        final Pic picLocal = response.body().getLocal();
                        picLocal.save();

                        //Creacion Twin, al cual se le inserta el pic local y remoto
                        final Twin nuevoTwin = Twin.builder()
                                .local(picLocal)
                                .remote(picRemoto)
                                .build();
                        nuevoTwin.save();

                        //Creacion del ItemTwin, el cual sera añadido al adaptador
                        ItemTwin nuevoItemTwin = obtenerItemTwin(nuevoTwin);

                        //Agregar item twin al adaptador
                        adaptador.add(nuevoItemTwin);

                        //Cierre ventana de carga
                        cargando.dismiss();
                }

                //Error con conexion con el servidor
                @Override
                public void onFailure(Call<Twin> call, Throwable t) {

                    log.debug("APIRETURN2",String.valueOf(t));

                    //Cierre de ventana de carga
                    cargando.dismiss();

                    //Mostrar mensaje de error por conexion a la BD remota
                    Snackbar.make(view, "Ops! Tenemos problemas para acceder a nuestra base de datos.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        } else {

            //Mostrar mensaje de problemas de conexion de red
            Snackbar.make(view, "Ops! No hemos detectado conexion de red. Verifica tu conexión", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    /**
     * Obtiene la ultima ubicacion conocida del GPS.
     * @return devuelve la ubicacion como arreglo double, posicion 0 latitud pos 1 longitud.
     */
    public double[] obtenerUbicacion(){

        //Utilizacion de location manager para obtener ubicacion
        Criteria criteria = new Criteria();
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Se checkean los permisos de GPS
        if ( ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.ACCESS_COARSE_LOCATION )
                != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    MY_PERMISSION_ACCESS_COURSE_LOCATION);
        }

        //Variable location que contendra la ubicacion GPS.
        Location location = mlocManager.getLastKnownLocation(mlocManager
                .getBestProvider(criteria, false));

        //Generacion de arreglo de double que contendra longitud y latitud.
        double [] ubicacion = new double[2];

        //Si location obtenido es distinto de null, se envia la longitud y latitud, si no (0,0).
        if(location!=null){

            //Se guarda latitud y longitud en el arreglo.
            ubicacion[0] = location.getLatitude();
            ubicacion[1] = location.getLongitude();
        } else {

            //Si la location fue null, se envia latitud y longitud con valor cero.
            ubicacion[0]= 0;
            ubicacion[1]= 0;
        }
        return ubicacion;
    }

    /**
     * Metodo que escala la imagen obtenida desde la camara, se obtiene una imagen mas pequeña
     * como de menor tamaño.
     * @param myBitmap es el bitmap obtenido desde la camara.
     * @return bitmap escalado.
     */
    public Bitmap escalarImagen(Bitmap myBitmap){

        //Asignacion de variables de tamanio de imagen, ancho y alto.
        final int maxSize = 600;
        int outWidth;
        int outHeight;
        int inWidth = myBitmap.getWidth();
        int inHeight = myBitmap.getHeight();

        //Si el ancho es mayor que el alto se asigna el mayor tamanio al ancho, si no al alto.
        if(inWidth > inHeight){
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }

        //Se genera y retorna el bitmap redimencionado.
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(myBitmap, outWidth, outHeight, false);
        return resizedBitmap;
    }
}
