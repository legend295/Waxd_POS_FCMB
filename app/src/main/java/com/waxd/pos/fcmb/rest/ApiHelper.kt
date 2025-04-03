package com.waxd.pos.fcmb.rest

import com.waxd.pos.fcmb.base.BaseApiResponse
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiHelper {

    @POST(ApiUrl.LOGIN)
    suspend fun login(@Body request: LoginRequest): Response<BaseApiResponse<LoginResponse>>

    @POST(ApiUrl.REGISTER_AGENT)
    suspend fun registerAgent(@Body request: RegistrationRequest): Response<RegistrationResponse>


    @GET(ApiUrl.GET_AGENT_BY_UID)
    suspend fun getAgentById(@Path("uid") uid: String): Response<AgentProfileResponse>

    @POST(ApiUrl.UPLOAD_PROFILE_IMAGE)
    suspend fun uploadProfileImage(@Body map: HashMap<String, Any?>): Response<UploadImageResponse>

    @PATCH(ApiUrl.UPDATE_PROFILE)
    suspend fun updateAgentProfile(@Body request: UpdateAgentProfileRequest): Response<UpdateProfileResponse>

    @POST(ApiUrl.REGISTER_FARMER)
    suspend fun registerFarmer(@Body request: FarmerCreateRequest): Response<FarmerCreateResponse>

    suspend fun getProfileData(hashMap: HashMap<String, Any>): Response<BaseApiResponse<JSONObject>>

    suspend fun getFarmersList(hashMap: HashMap<String, Any>): Response<BaseApiResponse<JSONObject>>

    suspend fun addFarmers(hashMap: HashMap<String, Any>): Response<BaseApiResponse<JSONObject>>

    suspend fun searchFarmers(hashMap: HashMap<String, Any>): Response<BaseApiResponse<JSONObject>>

    suspend fun getNotifications(hashMap: HashMap<String, Any>): Response<BaseApiResponse<JSONObject>>

    suspend fun getLoans(hashMap: HashMap<String, Any>): Response<BaseApiResponse<JSONObject>>

    suspend fun getLoanDetails(hashMap: HashMap<String, Any>): Response<BaseApiResponse<JSONObject>>

    suspend fun createLoanApplication(hashMap: HashMap<String, Any>): Response<BaseApiResponse<JSONObject>>


}