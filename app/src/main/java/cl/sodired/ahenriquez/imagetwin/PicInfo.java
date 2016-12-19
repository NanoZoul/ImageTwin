package cl.sodired.ahenriquez.imagetwin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;
import cl.sodired.ahenriquez.imagetwin.util.CircleTransform;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by sandi on 19-12-2016.
 */

@Slf4j
public class PicInfo extends AppCompatActivity {

    @BindView(R.id.imageInfo)
    ImageView imagen;

    @BindView(R.id.ubicacionInfo)
    TextView ubicacion;

    @BindView(R.id.negativeInfo)
    TextView negative;

    @BindView(R.id.warningInfo)
    TextView warning;

    @BindView(R.id.positiveInfo)
    TextView positive;

    @BindView(R.id.fechaInfo)
    TextView fecha;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pic_info);
        Context context = getApplicationContext();
        //Inicio de ButterKnife
        ButterKnife.bind(this);
        Intent intent = getIntent();
        Log.d("INFOPIC",String.valueOf(intent.getStringExtra("longitud")));
        //Dar valores a los elementos
        ubicacion.setText("Longitud: " + intent.getStringExtra("longitud") + " Latitud: " + intent.getStringExtra("latitud"));
        negative.setText(intent.getStringExtra("negative"));
        positive.setText(intent.getStringExtra("positive"));
        warning.setText(intent.getStringExtra("warning"));
        fecha.setText(intent.getStringExtra("fecha"));
        Picasso.with(context).load("http://192.168.0.14:8181/" + intent.getStringExtra("url")).resize(600,600).centerCrop().into(imagen);
    }

}
