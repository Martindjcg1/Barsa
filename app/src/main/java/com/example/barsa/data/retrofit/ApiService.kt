package com.example.barsa.data.retrofit

import com.example.barsa.data.retrofit.models.DesactivarDetencionResponse
import com.example.barsa.data.retrofit.models.DetencionRemota
import com.example.barsa.data.retrofit.models.ListadoPapeletasResponse
import com.example.barsa.data.retrofit.models.LoginRequest
import com.example.barsa.data.retrofit.models.LoginResponse
import com.example.barsa.data.retrofit.models.TiempoRemoto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApiService {
    @POST("user-authentication/login")
    suspend fun login(@Body credentials: LoginRequest): LoginResponse
}

interface PapeletaApiService {
    @GET("papeleta/get-listado-papeletas")
    suspend fun getListadoPapeletas(
        @Header("Authorization") authToken: String,
        @Query("page") page: Int,
        @Query("tipoId") tipoId: String? = null,
        @Query("folio") folio: Int? = null,
        @Query("fecha") fecha: String? = null,
        @Query("status") status: String? = null,
        @Query("observacionGeneral") observacionGeneral: String? = null
    ): ListadoPapeletasResponse

    @GET("produccion/obtener-tiempos/{folio}")
    suspend fun getTiemposPorFolio(
        @Header("Authorization") authToken: String,
        @Path("folio") folio: Int
    ): List<TiempoRemoto>

    @GET("produccion/obtener-ultima-detencion/{folio}")
    suspend fun getUltimaDetencion(
        @Header("Authorization") authToken: String,
        @Path("folio") folio: Int
    ): List<DetencionRemota>

    @FormUrlEncoded
    @PUT("produccion/desactivar-detencion-tiempo")
    suspend fun desactivarDetencionTiempo(
        @Header("Authorization") authToken: String,
        @Field("folio") folio: Int,
        @Field("etapa") etapa: String
    ): Response<DesactivarDetencionResponse>

}

