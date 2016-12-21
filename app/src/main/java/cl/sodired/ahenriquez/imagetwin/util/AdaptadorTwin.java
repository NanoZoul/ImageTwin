package cl.sodired.ahenriquez.imagetwin.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import cl.sodired.ahenriquez.imagetwin.PicInfo;
import cl.sodired.ahenriquez.imagetwin.R;
import cl.sodired.ahenriquez.imagetwin.domain.Pic;

/**
 * Created by sandi on 21-11-2016.
 */

public class AdaptadorTwin extends ArrayAdapter<ItemTwin> {
    Activity activity;
    PackageUtils pu;

    /**
     * Constructor
     * @param context contect
     * @param users users
     */
    public AdaptadorTwin(Context context, ArrayList<ItemTwin> users) {
        super(context, 0, users);
        activity = (Activity) context;
    }


    /**
     * View
     * @param position position
     * @param convertView convertView
     * @param parent parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Obtener Contexto
        Context context = getContext();
        // Obtener las fotos para esta posicion
        final ItemTwin itemTwin = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
        }
        //Obtener ImageView
        ImageButton imagen0 = (ImageButton) convertView.findViewById(R.id.twin_image);
        ImageButton imagen1 = (ImageButton) convertView.findViewById(R.id.twin_image2);
        //Muestro las imagenes con Picasso
            Picasso.with(context).load("http://172.20.10.4:8181/" + itemTwin.imagenUsuario.getUrl())
                    .rotate(-90).resize(600,600)
                    .transform(new CircleTransform())
                    .centerCrop()
                    .into(imagen0);

            Picasso.with(context).load("http://172.20.10.4:8181/" + itemTwin.imagenPareja.getUrl())
                    .resize(600,600)
                    .transform(new CircleTransform())
                    .centerCrop()
                    .into(imagen1);
        //Listener para la imagen local
        imagen0.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                abrirPicInfo(itemTwin.imagenUsuario,"usuario");
            }
        });
        //Listener para la imagen remota
        imagen1.setOnClickListener(new View.OnClickListener(){
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
    public void abrirPicInfo(Pic pic, String tipo){
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
