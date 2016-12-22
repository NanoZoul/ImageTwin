package cl.sodired.ahenriquez.imagetwin.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import cl.sodired.ahenriquez.imagetwin.PicInfo;
import cl.sodired.ahenriquez.imagetwin.R;
import cl.sodired.ahenriquez.imagetwin.domain.Pic;

/**
 * Clase AdaptadorTwin, el cual corresponde al adaptador del ListView que muestra los Twin.
 */
public class AdaptadorTwin extends ArrayAdapter<ItemTwin> {
    /**
     * Activity actual
     */
    private Activity activity;

    /**
     * Constructor del adaptador twin.
     * @param context recibe el contexto de la activity actual.
     * @param users corresponden a los ItemTwin que se muestran en el ListView.
     */
    public AdaptadorTwin(Context context, ArrayList<ItemTwin> users) {
        super(context, 0, users);
        activity = (Activity) context;
    }

    /**
     * View que muestra las imagenes
     * @param position posicion del ItemTwin actual.
     * @param convertView view anterior para ser usada en caso de error.
     * @param parent puede contener a otras Views.
     * @return convertView que corresponde a la View anterior
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Obtener Contexto
        Context context = getContext();

        // Obtener el itemTwin que contiene las Pic que seran mostradas
        final ItemTwin itemTwin = getItem(position);

        //Convert view null, muestra view anterior del parent
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
        }

        ImageButton imgUsuario = (ImageButton) convertView.findViewById(R.id.img_user);
        ImageButton imgPareja = (ImageButton) convertView.findViewById(R.id.img_pareja);

        //Se muestra la imagen del usuario con Picasso
        Picasso.with(context).load("http://192.168.0.14:8181/" + itemTwin.imagenUsuario.getUrl())
                .rotate(-90).resize(600,600)
                .transform(new CircleTransform())
                .centerCrop()
                .placeholder(R.drawable.noinet2)
                .into(imgUsuario);

        //Se muestra la imagen de la pareja con Picasso
        Picasso.with(context).load("http://192.168.0.14:8181/" + itemTwin.imagenPareja.getUrl())
                .resize(600,600)
                .transform(new CircleTransform())
                .centerCrop()
                .placeholder(R.drawable.noinet)
                .into(imgPareja);

        //Listener para la imagen local
        imgUsuario.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                abrirPicInfo(itemTwin.imagenUsuario,"usuario");
            }
        });

        //Listener para la imagen remota
        imgPareja.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                abrirPicInfo(itemTwin.imagenPareja,"pareja");
            }
        });

        return convertView;
    }

    /**
     * Metodo que abre el nuevo activity
     * @param pic que se enviara al otro metodo
     * @param tipo dice si es usuario o la pareja
     */
    private void abrirPicInfo(Pic pic, String tipo){
        Intent intent = new Intent(this.activity,PicInfo.class);
        intent.putExtra("url",pic.getUrl())
                .putExtra("idRemota",pic.getIdRemota())
                .putExtra("tipo",tipo)
                .putExtra("fecha",String.valueOf(pic.getFecha()))
                .putExtra("longitud",pic.getLongitude())
                .putExtra("latitud",pic.getLatitude())
                .putExtra("negative",pic.getNegative())
                .putExtra("positive",pic.getPositive())
                .putExtra("warning",pic.getWarning());
        activity.startActivity(intent);
    }
}
