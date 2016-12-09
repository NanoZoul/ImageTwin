package cl.sodired.ahenriquez.imagetwin;

import android.app.Application;

import com.google.common.base.Stopwatch;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.squareup.leakcanary.LeakCanary;

import cl.sodired.ahenriquez.imagetwin.util.DeviceUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Singleton principal de la aplicacion.
 *
 * @author Diego P. Urrutia Astorga
 * @version 20161020
 */
@Slf4j
public class MainApp extends Application {

    /**
     * Al iniciarse por primera vez la aplicacion.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        //Iniciamos DBFLOW
        FlowManager.init(new FlowConfig.Builder(this).build());
        FlowManager.getDatabase(Database.class).getWritableDatabase();
        // LeakCanary solo en aplicacion principal
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }

        // Startdate
        final Stopwatch stopwatch = Stopwatch.createStarted();

        log.debug("Install LeakCanary in {}.", DeviceUtils.getDeviceName(this));

        LeakCanary.install(this);

        log.info("Init in {}.", stopwatch);

    }

}
