package cl.sodired.ahenriquez.imagetwin.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

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
        // Obtener la foto para esta posicion
        ItemTwin itemTwin = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
        }
        ImageView imagen0 = (ImageView) convertView.findViewById(R.id.twin_image);
        ImageView imagen1 = (ImageView) convertView.findViewById(R.id.twin_image2);
        imagen0.setImageDrawable(itemTwin.imagenUsuario);
        imagen1.setImageDrawable(itemTwin.imagenPareja);
        return convertView;
    }
}
