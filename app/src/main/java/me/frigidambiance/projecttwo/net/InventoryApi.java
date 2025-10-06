package me.frigidambiance.projecttwo.net;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface InventoryApi {

    // Push local dirty changes
    @POST("inventory/bulk")
    Call<BulkResponse> bulkUpsert(@Body List<InventoryDto> items);

    // Pull server-side changes since a checkpoint (ISO-8601 UTC)
    @GET("inventory/changes")
    Call<ChangesResponse> getChanges(@Query("since") String sinceIsoUtc);
}
