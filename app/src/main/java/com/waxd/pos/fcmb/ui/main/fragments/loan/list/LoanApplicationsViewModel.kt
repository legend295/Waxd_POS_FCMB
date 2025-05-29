package com.waxd.pos.fcmb.ui.main.fragments.loan.list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.waxd.pos.fcmb.rest.FarmerLoanApplicationResponse
import com.waxd.pos.fcmb.utils.firebase.FirebaseWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoanApplicationsViewModel @Inject constructor(private val firebaseWrapper: FirebaseWrapper) :
    ViewModel() {

    var isLoading = false
    var isLastPage = false

    val farmerLoanApplicationsResponse: MutableLiveData<ArrayList<FarmerLoanApplicationResponse>> =
        MutableLiveData()

    fun getFarmerLoanApplication(query: String, lastVisibleDocument: DocumentSnapshot?) {
        firebaseWrapper.getLoanApplications(query, lastVisibleDocument) { list ->
            farmerLoanApplicationsResponse.value = list
        }
    }
}