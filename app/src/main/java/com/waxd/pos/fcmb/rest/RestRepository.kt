package com.waxd.pos.fcmb.rest

import android.content.Context
import com.waxd.pos.fcmb.base.BaseApiResponse
import com.waxd.pos.fcmb.base.BaseDataSource
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.datastore.DataStoreWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import javax.inject.Inject

class RestRepository @Inject constructor(
    private var apiHelper: ApiHelper,
    context: Context
) :
    BaseDataSource(context) {

    fun login(request: LoginRequest): Flow<DataResult<BaseApiResponse<LoginResponse>>> =
        flow { emit(safeApiCall { apiHelper.login(request) }) }

    fun registerAgent(request: RegistrationRequest): Flow<DataResult<RegistrationResponse>> =
        flow { emit(safeApiCall { apiHelper.registerAgent(request) }) }

    fun registerFarmer(request: FarmerCreateRequest): Flow<DataResult<FarmerCreateResponse>> =
        flow { emit(safeApiCall { apiHelper.registerFarmer(request) }) }

    fun getAgentById(uid: String): Flow<DataResult<AgentProfileResponse>> =
        flow { emit(safeApiCall { apiHelper.getAgentById(uid) }) }

    fun uploadProfileImage(request: HashMap<String, Any?>): Flow<DataResult<UploadImageResponse>> =
        flow { emit(safeApiCall { apiHelper.uploadProfileImage(request) }) }

    fun updateAgentProfile(request: UpdateAgentProfileRequest): Flow<DataResult<UpdateProfileResponse>> =
        flow { emit(safeApiCall { apiHelper.updateAgentProfile(request) }) }


}