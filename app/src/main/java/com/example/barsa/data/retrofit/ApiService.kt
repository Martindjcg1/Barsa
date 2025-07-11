package com.example.barsa.data.retrofit

import com.example.barsa.data.retrofit.models.ApiNotification
import com.example.barsa.data.retrofit.models.ChangePasswordRequest
import com.example.barsa.data.retrofit.models.ChangePasswordResponse
import com.example.barsa.data.retrofit.models.ApiWrapperResponse
import com.example.barsa.data.retrofit.models.BitacoraListadoInventario
import com.example.barsa.data.retrofit.models.BitacoraListadoProduccion
import com.example.barsa.data.retrofit.models.CreateMaterialResponse
import com.example.barsa.data.retrofit.models.DeleteMaterialResponse
import com.example.barsa.data.retrofit.models.DetencionRemota

import com.example.barsa.data.retrofit.models.DetencionTiempoRequest
import com.example.barsa.data.retrofit.models.FinalizarTiempoRequest
import com.example.barsa.data.retrofit.models.IniciarTiempoRequest
import com.example.barsa.data.retrofit.models.InventoryPaginationResponse
import com.example.barsa.data.retrofit.models.ListadoPapeletasResponse
import com.example.barsa.data.retrofit.models.ListadoTiemposResponse
import com.example.barsa.data.retrofit.models.LoginRequest
import com.example.barsa.data.retrofit.models.LoginResponse
import com.example.barsa.data.retrofit.models.LogoutResponse
import com.example.barsa.data.retrofit.models.RefreshResponse
import com.example.barsa.data.retrofit.models.RegisterRequest
import com.example.barsa.data.retrofit.models.RegisterResponse
import com.example.barsa.data.retrofit.models.PausarTiempoRequest
import com.example.barsa.data.retrofit.models.ReiniciarTiempoRequest
import com.example.barsa.data.retrofit.models.TiempoRemoto
import com.example.barsa.data.retrofit.models.ToggleUserStatusResponse
import com.example.barsa.data.retrofit.models.UpdateMaterialResponse
import com.example.barsa.data.retrofit.models.UpdatePersonalInfoRequest
import com.example.barsa.data.retrofit.models.UpdatePersonalInfoResponse
import com.example.barsa.data.retrofit.models.UpdateUserRequest
import com.example.barsa.data.retrofit.models.UpdateUserResponse
import com.example.barsa.data.retrofit.models.UserDetailResponse
import com.example.barsa.data.retrofit.models.UserProfile
import com.example.barsa.data.retrofit.models.UsuarioInfoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

import retrofit2.http.*
import okhttp3.MultipartBody
import okhttp3.RequestBody


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

    // CORREGIDO: Cambiado de @PUT a @PATCH
    @PATCH("user-authentication/update-info-usuario-personal")
    suspend fun updatePersonalInfo(
        @Header("Authorization") token: String,
        @Body updateData: UpdatePersonalInfoRequest
    ): UpdatePersonalInfoResponse

    @GET("user-authentication/listado-usuarios")
    suspend fun getUsers(
        @Header("Authorization") token: String,
        @Query("nombre") nombre: String? = null,
        @Query("nombreUsuario") nombreUsuario: String? = null,
        @Query("email") email: String? = null,
        @Query("rol") rol: String? = null,
        @Query("estado") estado: String? = null
    ): List<UserProfile>

    @GET("user-authentication/obtener-info-usuario/{id}")
    suspend fun getUserDetail(
        @Header("Authorization") token: String,
        @Path("id") userId: String
    ): UserDetailResponse

    @GET("user-authentication/obtener-info-usuario-personal")
    suspend fun obtenerInfoUsuarioPersonal(
        @Header("Authorization") token: String
    ): Response<UsuarioInfoResponse>

    @PATCH("user-authentication/{id}")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Path("id") userId: String,
        @Body updateData: UpdateUserRequest
    ): UpdateUserResponse

    @PUT("user-authentication/desactivar-usuario/{id}")
    suspend fun toggleUserStatus(
        @Header("Authorization") token: String,
        @Path("id") userId: String
    ): ToggleUserStatusResponse

    @GET("bitacora/listado-produccion")
    suspend fun getListadoProduccion(
        @Header("Authorization") token: String,
        @Query("fechaInicio") fechaInicio: String? = null,
        @Query("fechaFin") fechaFin: String? = null,
        @Query("id") id: Int? = null,
        @Query("folio") folio: Int? = null,
        @Query("etapa") etapa: String? = null,
        @Query("movimiento") movimiento: String? = null,
        @Query("usuario") usuario: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): BitacoraListadoProduccion

    @GET("bitacora/listado-inventario")
    suspend fun getListadoInventario(
        @Header("Authorization") token: String,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("fechaInicio") fechaInicio: String? = null,
        @Query("fechaFin") fechaFin: String? = null,
        @Query("id") id: Int? = null,
        @Query("codigo") borrado: String? = null
    ): BitacoraListadoInventario

}


interface InventoryApiService {
    @GET("materia/get-listado-materia")
    suspend fun getInventoryItems(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("codigoMat") codigoMat: String? = null,
        @Query("descripcion") descripcion: String? = null,
        @Query("unidad") unidad: String? = null,
        @Query("proceso") proceso: String? = null,
        @Query("borrado") borrado: String? = null
    ): InventoryPaginationResponse

    @Multipart
    @POST("materia/crear-materia")
    suspend fun createMaterial(
        @Header("Authorization") token: String,
        @Part("codigoMat") codigoMat: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("unidad") unidad: RequestBody,
        @Part("pcompra") pcompra: RequestBody,
        @Part("existencia") existencia: RequestBody,
        @Part("max") max: RequestBody,
        @Part("min") min: RequestBody,
        @Part("inventarioInicial") inventarioInicial: RequestBody,
        @Part("unidadEntrada") unidadEntrada: RequestBody,
        @Part("cantxunidad") cantxunidad: RequestBody,
        @Part("proceso") proceso: RequestBody,
        @Part files: List<MultipartBody.Part>? = null // Archivos de imagen
    ): CreateMaterialResponse

    @Multipart
    @PUT("materia/update-materia/{codigoMat}")
    suspend fun updateMaterial(
        @Header("Authorization") token: String,
        @Path("codigoMat") codigoMat: String,
        @Part("descripcion") descripcion: RequestBody,
        @Part("unidad") unidad: RequestBody,
        @Part("pcompra") pcompra: RequestBody,
        @Part("existencia") existencia: RequestBody,
        @Part("max") max: RequestBody,
        @Part("min") min: RequestBody,
        @Part("inventarioInicial") inventarioInicial: RequestBody, // Cambié el nombre
        @Part("unidadEntrada") unidadEntrada: RequestBody,
        @Part("cantxunidad") cantxunidad: RequestBody, // Cambié el nombre
        @Part("proceso") proceso: RequestBody,
        @Part("borrado") borrado: RequestBody, // Agregué este campo
        @Part imagenes: List<MultipartBody.Part>? = null // Múltiples archivos con el mismo nombre "imagenes"
    ): UpdateMaterialResponse


    @DELETE("materia/borrar-materia/{codigoMat}")
    suspend fun deleteMaterial(
        @Header("Authorization") token: String,
        @Path("codigoMat") codigoMat: String
    ): Response<DeleteMaterialResponse>
}


interface NotificationApiService {

    @GET("user-authentication/notificacion/lista-notificaciones")
    suspend fun getNotifications(
        @Header("Authorization") token: String
    ): Response<List<ApiNotification>>

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
    ): Response<ApiWrapperResponse>

    @POST("produccion/iniciar-tiempo")
    suspend fun iniciarTiempo(
        @Header("Authorization") authToken: String,
        @Body body: IniciarTiempoRequest
    ): Response<ApiWrapperResponse>

    @PUT("produccion/pausar-tiempo")
    suspend fun pausarTiempo(
        @Body request: PausarTiempoRequest,
        @Header("Authorization") token: String
    ): Response<ApiWrapperResponse>

    @GET("produccion/obtener-tiempo")
    suspend fun getTiempoPorEtapa(
        @Header("Authorization") authToken: String,
        @Query("folio") folio: Int,
        @Query("etapa") etapa: String
    ): List<TiempoRemoto>

    @PUT("produccion/reiniciar-tiempo")
    suspend fun reiniciarTiempo(
        @Header("Authorization") authToken: String,
        @Body body: ReiniciarTiempoRequest
    ): Response<ApiWrapperResponse>

    @PUT("produccion/finalizar-tiempo")
    suspend fun finalizarTiempo(
        @Header("Authorization") token: String,
        @Body request: FinalizarTiempoRequest
    ): Response<ApiWrapperResponse>

    @POST("produccion/detencion-tiempo")
    suspend fun reportarDetencionTiempo(
        @Header("Authorization") token: String,
        @Body request: DetencionTiempoRequest
    ): Response<ApiWrapperResponse>

    @GET("produccion/obtener-detencion")
    suspend fun getDetencionesPorEtapa(
        @Header("Authorization") authToken: String,
        @Query("folio") folio: Int,
        @Query("etapa") etapa: String
    ): List<DetencionRemota>

    @GET("produccion/obtener-detenciones-folio/{folio}")
    suspend fun getDetencionesPorFolio(
        @Header("Authorization") authToken: String,
        @Path("folio") folio: Int
    ): List<DetencionRemota>

    @GET("produccion/obtener-tiempos-periodo")
    suspend fun obtenerTiemposPorPeriodo(
        @Header("Authorization") authToken: String,
        @Query("fechaInicio") fechaInicio: String,
        @Query("fechaFin") fechaFin: String,
        @Query("page") page: Int
    ): ListadoTiemposResponse

}

