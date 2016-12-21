package cl.sodired.ahenriquez.imagetwin.service;

import cl.sodired.ahenriquez.imagetwin.domain.Pic;
import cl.sodired.ahenriquez.imagetwin.domain.Twin;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by sandi on 01-12-2016.
 */

public interface WebService {
    String BASE_URL = "http://172.20.10.4:8181/";
    @POST("pic/enviar")
    Call<Twin> sendPic(@Body Pic pic);

    @POST("reaccion/like")
    Call<Integer> incrementarLikes(@Body Integer valor);

    @POST("reaccion/warn")
    Call<Integer> incrementarDislikes(@Body Integer valor);

    @POST("reaccion/dislike")
    Call<Integer> incrementarWarn(@Body Integer valor);

    @GET("isBdConnected")
    Call<Boolean> isBdConnected(@Path("user") String user);

    class Factory{
        private  static WebService service;

        public static WebService getInstance(){
            if(service==null){
                Retrofit retrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(BASE_URL).build();
                WebService service = retrofit.create(WebService.class);
                return service;
            }else{
                return service;
            }
        }

    }


}
