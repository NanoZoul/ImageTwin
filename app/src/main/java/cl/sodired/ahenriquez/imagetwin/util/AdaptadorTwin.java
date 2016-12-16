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
        Picasso.with(context).load("file://" + itemTwin.imagenUsuario).resize(600,600).centerCrop().into(imagen0);
        if(isOnlineNet()){
            Picasso.with(context).load("http://192.168.0.20:8181/"+itemTwin.imagenPareja).resize(600,600).centerCrop().into(imagen1);
        }else{
            Picasso.with(context).load(R.drawable.noinet).resize(300,300).centerCrop().into(imagen1);
        }
        if(itemTwin.imagenPareja=="none"){
            Picasso.with(context).load(R.drawable.noinet2).resize(300,300).centerCrop().into(imagen1);
        }

        Log.d("PATHIMAGEN",itemTwin.imagenUsuario);
        return convertView;
    }

    public Boolean isOnlineNet() {

        try {
            Process p = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.es");

            int val           = p.waitFor();
            boolean reachable = (val == 0);
            return reachable;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }
}
