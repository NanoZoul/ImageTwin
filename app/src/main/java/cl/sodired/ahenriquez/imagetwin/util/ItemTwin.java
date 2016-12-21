package cl.sodired.ahenriquez.imagetwin.util;

import android.support.v4.graphics.drawable.RoundedBitmapDrawable;

import cl.sodired.ahenriquez.imagetwin.domain.Pic;

/**
 * Created by sandi on 21-11-2016.
 */

/**
 * Item twin que es anadido al listView
 */
public class ItemTwin {
    public Pic imagenUsuario;
    public Pic imagenPareja;

    public ItemTwin(Pic imagen1, Pic imagen2){
        this.imagenUsuario = imagen1;
        this.imagenPareja = imagen2;
    }
}
