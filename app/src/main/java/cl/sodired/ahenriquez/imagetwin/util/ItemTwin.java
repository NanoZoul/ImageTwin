package cl.sodired.ahenriquez.imagetwin.util;

import android.support.v4.graphics.drawable.RoundedBitmapDrawable;

/**
 * Created by sandi on 21-11-2016.
 */

public class ItemTwin {
    public String imagenUsuario;
    public String imagenPareja;

    public ItemTwin(String imagen1, String imagen2){
        this.imagenUsuario = imagen1;
        this.imagenPareja = imagen2;
    }
}
