package com.waxd.pos.fcmb.ui.main.fragments.farmer.list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.waxd.pos.fcmb.rest.FarmerData
import com.waxd.pos.fcmb.rest.FarmerResponse
import com.waxd.pos.fcmb.utils.firebase.FirebaseWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FarmersListViewModel @Inject constructor(private val firebaseWrapper: FirebaseWrapper) :
    ViewModel() {

    var isLastPage = false
    var isLoading = false
    val farmersData: MutableLiveData<ArrayList<FarmerResponse>> = MutableLiveData()

    fun getFarmers(query: String, lastVisibleDocument: DocumentSnapshot?) {
        firebaseWrapper.getFarmers(query, lastVisibleDocument) { list ->
            farmersData.value = list
        }
    }
}