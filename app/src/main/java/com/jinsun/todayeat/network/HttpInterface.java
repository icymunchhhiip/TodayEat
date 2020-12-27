package com.jinsun.todayeat.network;

import com.jinsun.todayeat.model.ExceptedPlaceModel;
import com.jinsun.todayeat.model.poi.POIData;
import com.jinsun.todayeat.model.poidetail.POIDetailData;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface HttpInterface {
//    @POST("/recipe/insert-recipe.php")
//    Call<String> setRecipe(
//            @Body SelectedRecipe selectedRecipe
//    );
//
//    @DELETE("/recipe/del-recipe.php")
//    Call<String> deleteRecipe(
//            @Query("recipe_id") int recipeId
//    );

    /**
     * @param version       Constants.TMAP_VERSION
     * @param page          Constants.POI_PAGE
     * @param count         Constants.POI_COUNT
     * @param categories    ;기준으로 음식 종류
     * @param radius        1~33 전국은 0
     * @param appKey        strings.tmap
     * */

    @GET("pois/search/around")
    Call<POIData> getPOIData(
            @Query("version") int version,
            @Query("page") int page,
            @Query("count") int count,
            @Query("categories") String categories,
            @Query("centerLon") Double centerLon,
            @Query("centerLat") Double centerLat,
            @Query("radius") int radius,
            @Query("appKey") String appKey
    );

    @GET("pois/{poiId}")
    Call<POIDetailData> getPOIDetailData(
            @Path("poiId") String poiId,
            @Query("version") int version,
            @Query("appKey") String appKey
    );

    @GET("read.php")
    Call<List<ExceptedPlaceModel>> getPlaces(
            @Query("userid") int userId
    );

    @GET("change.php")
    Call<String> setPlaces(
            @Query("userid") int userId,
            @Query("placeid") int placeId,
            @Query("name") String name,
            @Query("addr") String addr,
            @Query("dong") String dong,
            @Query("action") String action
    );

    @GET("user.php")
    Call<String> setUser(
            @Query("userid") int userId,
            @Query("action") String action
    );
}
