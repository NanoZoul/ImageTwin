package cl.sodired.ahenriquez.imagetwin.util;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import cl.sodired.ahenriquez.imagetwin.R;

/**
 * Created by sandi on 21-11-2016.
 */

public class AdaptadorTwin extends ArrayAdapter<ItemTwin> {
    public AdaptadorTwin(Context context, ArrayList<ItemTwin> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Obtener Contexto
        Context context = getContext();
        // Obtener las fotos para esta posicion
        ItemTwin itemTwin = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
        }
        //Obtener ImageView
        ImageView imagen0 = (ImageView) convertView.findViewById(R.id.twin_image);
        ImageView imagen1 = (ImageView) convertView.findViewById(R.id.twin_image2);
        //Muestro las imagenes con Picasso
        Picasso.with(context).load("http://192.168.0.14:8181/" + itemTwin.imagenUsuario).resize(600,600).centerCrop().into(imagen0);
        Picasso.with(context).load("http://192.168.0.14:8181/" + itemTwin.imagenPareja).resize(600,600).centerCrop().into(imagen1);
        Log.d("PATHIMAGEN",itemTwin.imagenUsuario);
        return convertView;
    }
}
