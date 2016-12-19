package cl.sodired.ahenriquez.imagetwin.domain;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;

import cl.sodired.ahenriquez.imagetwin.Database;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Pic
 *
 * @author Diego P. Urrutia Astorga
 * @version 20161102
 */
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Table(
        database = Database.class,
        cachingEnabled = true,
        orderedCursorLookUp = true, // https://github.com/Raizlabs/DBFlow/blob/develop/usage2/Retrieval.md#faster-retrieval
        cacheSize = Database.CACHE_SIZE
)
public class Pic extends BaseModel {

    /**
     * Identificador unico
     */
    @Column
    @Getter
    @PrimaryKey(autoincrement = true)
    Long id;

    /**
     * Identificador del dispositivo
     */
    @Column
    @Getter
    String deviceId;

    /**
     * Fecha de la foto
     */
    @Column
    @Getter
    Long fecha;

    /**
     * URL de la foto
     */
    @Column
    @Getter
    String url;

    /**
     * Latitud
     */
    @Column
    @Getter
    Double latitude;

    /**
     * Longitud
     */
    @Column
    @Getter
    Double longitude;

    /**
     * Numero de likes
     */
    @Column
    @Getter
    Integer positive;

    /**
     * Numero de dis-likes
     */
    @Column
    @Getter
    Integer negative;

    /**
     * Numero de warnings
     */
    @Column
    @Getter
    Integer warning;

    /**
     * Imagen
     */
    @Column
    @Getter
    String imagen;
}
