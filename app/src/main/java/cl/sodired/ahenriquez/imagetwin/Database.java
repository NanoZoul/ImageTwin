package cl.sodired.ahenriquez.imagetwin;

/**
 * Created by durrutia on 02-Nov-16.
 */
@com.raizlabs.android.dbflow.annotation.Database(name = Database.NAME, version = Database.VERSION)
public class Database {

    /**
     * Key de la base de datos
     */
    public static final String NAME = "Database";

    /**
     * Version de la BD
     */
    public static final int VERSION = 1;

    /**
     * Tamanio del cache
     */
    public static final int CACHE_SIZE = 1000;
}
