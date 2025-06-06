package com.example.barsa.data.retrofit

import com.example.barsa.data.retrofit.models.ChangePasswordRequest
import com.example.barsa.data.retrofit.models.ChangePasswordResponse
import com.example.barsa.data.retrofit.models.DesactivarDetencionResponse
import com.example.barsa.data.retrofit.models.DetencionRemota

import com.example.barsa.data.retrofit.models.ListadoPapeletasResponse
import com.example.barsa.data.retrofit.models.LoginRequest
import com.example.barsa.data.retrofit.models.LoginResponse
import com.example.barsa.data.retrofit.models.LogoutResponse
import com.example.barsa.data.retrofit.models.RefreshResponse
import com.example.barsa.data.retrofit.models.RegisterRequest
import com.example.barsa.data.retrofit.models.RegisterResponse
import com.example.barsa.data.retrofit.models.TiempoRemoto
import com.example.barsa.data.retrofit.models.UserDetailResponse
import com.example.barsa.data.retrofit.models.UserProfile
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

    @POST("user-authentication/registro")
    suspend fun register(
        @Header("Authorization") token: String,
        @Body registerData: RegisterRequest
    ): RegisterResponse


    @POST("user-authentication/logout")
    suspend fun logout(@Header("Authorization") token: String): LogoutResponse

    @POST("user-authentication/refresh")
    suspend fun refreshToken(@Header("Authorization") token: String): RefreshResponse

    @PUT("user-authentication/cambiar-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body changePasswordData: ChangePasswordRequest
    ): ChangePasswordResponse

    // ACTUALIZADO: Ahora devuelve directamente una lista de UserProfile
    @GET("user-authentication/listado-usuarios")
    suspend fun getUsers(
        @Header("Authorization") token: String,
        @Query("nombre") nombre: String? = null,
        @Query("nombreUsuario") nombreUsuario: String? = null,
        @Query("email") email: String? = null,
        @Query("rol") rol: String? = null,
        @Query("estado") estado: String? = null
    ): List<UserProfile>

    // NUEVO ENDPOINT PARA OBTENER INFORMACIÓN DETALLADA DE USUARIO
    @GET("user-authentication/obtener-info-usuario/{id}")
    suspend fun getUserDetail(
        @Header("Authorization") token: String,
        @Path("id") userId: String
    ): UserDetailResponse
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

