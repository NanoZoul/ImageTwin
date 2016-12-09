package cl.sodired.ahenriquez.imagetwin.util;

import android.support.v4.graphics.drawable.RoundedBitmapDrawable;

/**
 * Created by sandi on 21-11-2016.
 */

public class ItemTwin {
    public RoundedBitmapDrawable imagenUsuario;
    public RoundedBitmapDrawable imagenPareja;

    public ItemTwin(RoundedBitmapDrawable imagen1, RoundedBitmapDrawable imagen2){
        this.imagenUsuario = imagen1;
        this.imagenPareja = imagen2;
    }
}
