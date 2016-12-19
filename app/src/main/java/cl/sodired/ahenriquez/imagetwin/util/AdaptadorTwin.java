package cl.sodired.ahenriquez.imagetwin.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
    public AdaptadorTwin(Context context, ArrayList<ItemTwin> users) {
        super(context, 0, users);
        activity = (Activity) context;
    }



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
        Picasso.with(context).load("http://192.168.0.14:8181/" + itemTwin.imagenUsuario.getUrl()).resize(600,600).transform(new CircleTransform()).centerCrop().into(imagen0);
        Picasso.with(context).load("http://192.168.0.14:8181/" + itemTwin.imagenPareja.getUrl()).resize(600,600).transform(new CircleTransform()).centerCrop().into(imagen1);

        imagen0.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                abrirPicInfo(itemTwin.imagenUsuario);
            }
        });
        return convertView;
    }

    public void abrirPicInfo(Pic pic){
        Log.d("INFOPIC",String.valueOf(pic.getLatitude()));
        Intent intent = new Intent(this.activity,PicInfo.class);
        intent.putExtra("url",pic.getUrl())
                .putExtra("fecha",String.valueOf(pic.getFecha()))
                .putExtra("longitud",String.valueOf(pic.getLongitude()))
                .putExtra("latitud",String.valueOf(pic.getLatitude()))
                .putExtra("negative",String.valueOf(pic.getNegative()))
                .putExtra("positive",String.valueOf(pic.getPositive()))
                .putExtra("warning",String.valueOf(pic.getWarning()));
        activity.startActivity(intent);
    }
}
