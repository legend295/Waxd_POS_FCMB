package com.waxd.pos.fcmb.ui.main.fragments.loan.details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.rest.FarmerLoanApplicationData
import com.waxd.pos.fcmb.rest.RestRepository
import com.waxd.pos.fcmb.utils.firebase.FirebaseWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoanDetailViewModel @Inject constructor(private val firebaseWrapper: FirebaseWrapper) :
    ViewModel() {

    val response: MutableLiveData<DataResult<FarmerLoanApplicationData>> = MutableLiveData()
    var loanId: String? = null


    fun getLoanDetailsById(loanId: String) {
        response.value = DataResult.Loading

        firebaseWrapper.getLoanApplicationById(loanId) { response ->
            this.response.value = response
        }
    }
}