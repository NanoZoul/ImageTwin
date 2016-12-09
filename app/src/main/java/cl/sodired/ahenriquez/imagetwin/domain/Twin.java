package cl.sodired.ahenriquez.imagetwin.domain;

import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Clase que relaciona 2 {@link Pic}.
 *
 * @author Diego P. Urrutia Astorga
 * @version 20161102
 */
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Builder
/*
@Table(
        database = Database.class,
        cachingEnabled = false,
        orderedCursorLookUp = true, // https://github.com/Raizlabs/DBFlow/blob/develop/usage2/Retrieval.md#faster-retrieval
        cacheSize = Database.CACHE_SIZE
)
*/
public class Twin extends BaseModel {

    /**
     * Pic local
     */
    @Getter
    @Setter
    @PrimaryKey
    @ForeignKey(tableClass = Pic.class)
    Pic local;

    /**
     * Pic desde el servidor
     */
    @Getter
    @Setter
    @PrimaryKey
    @ForeignKey(tableClass = Pic.class)
    Pic remote;

}
