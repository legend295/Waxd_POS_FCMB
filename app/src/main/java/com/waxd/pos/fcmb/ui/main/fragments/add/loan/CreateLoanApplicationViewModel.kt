package com.waxd.pos.fcmb.ui.main.fragments.add.loan

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.datastore.KeyStore
import com.waxd.pos.fcmb.datastore.KeyStore.decryptData
import com.waxd.pos.fcmb.rest.CreateLoanApplicationRequest
import com.waxd.pos.fcmb.rest.FarmerData
import com.waxd.pos.fcmb.rest.FarmerLoanApplicationData
import com.waxd.pos.fcmb.rest.FarmerResponse
import com.waxd.pos.fcmb.utils.firebase.FirebaseWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class CreateLoanApplicationViewModel @Inject constructor(private val firebaseWrapper: FirebaseWrapper) :
    ViewModel() {

    val request: MutableLiveData<CreateLoanApplicationRequest> = MutableLiveData(
        CreateLoanApplicationRequest()
    )

    val response: MutableLiveData<DataResult<FarmerLoanApplicationData>> = MutableLiveData()

    var coordinates: ArrayList<LatLng>? = ArrayList()

    fun createLoanApplication(context: Context) {
        request.value?.let { request ->
            response.value = DataResult.Loading
            val dateFormat =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            dateFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val currentDate = dateFormat.format(System.currentTimeMillis())
            val map = hashMapOf(
                "bvn_number" to request.bvnNumber,
                "loan_application_number" to request.loanApplicationNumber,
//                            "farmer_id" to farmerResponse.farmerData.id,
                "loan_type" to request.loanType,
                "farmer_name" to request.farmerName,
                "loan_terms" to request.loanTerms,
                "loan_amount" to request.loanAmountDouble,
                "annual_income" to request.annualIncomeDouble,
                "date_created" to currentDate,
                "date_updated" to currentDate,
                "farm_locations" to request.farmLocations,
                "registered_by" to context.decryptData(KeyStore.USER_UID)
            )

            firebaseWrapper.createFarmerLoanApplication(map) {
                response.value = it
            }
            /*request.bvnNumber?.let { it1 ->
                getFarmersByBVN(it1).collect { farmerResponse ->
                    if (farmerResponse?.farmerData == null) {
                        response.value = DataResult.Failure(
                            status = "400",
                            message = "Farmer not found."
                        )
                    } else {
                        val dateFormat =
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                        dateFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
                        val currentDate = dateFormat.format(System.currentTimeMillis())
                        val map = hashMapOf(
                            "bvn_number" to request.bvnNumber,
                            "loan_application_number" to request.loanApplicationNumber,
//                            "farmer_id" to farmerResponse.farmerData.id,
                            "loan_type" to request.loanType,
                            "farmer_name" to request.farmerName,
                            "loan_type" to request.loanType,
                            "loan_amount" to request.loanAmountDouble,
                            "annual_income" to request.annualIncomeDouble,
                            "date_created" to currentDate,
                            "date_updated" to currentDate
                        )

                        firebaseWrapper.createFarmerLoanApplication(map) {
                            response.value = it
                        }
                    }
                }
            }*/
        }

    }

    private fun getFarmersByBVN(bvnNumber: String) = flow {
        val farmerResponse = suspendCoroutine { continuation ->
            firebaseWrapper.getFarmers(bvnNumber, null) { farmers ->
                continuation.resume(farmers.firstOrNull())
            }
        }
        emit(farmerResponse)
    }

}