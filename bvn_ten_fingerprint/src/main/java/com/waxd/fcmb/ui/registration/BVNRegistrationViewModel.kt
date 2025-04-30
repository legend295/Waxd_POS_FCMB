package com.waxd.fcmb.ui.registration

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.waxd.fcmb.models.BVNRegistrationData
import com.waxd.fcmb.models.NigerianState

class BVNRegistrationViewModel : ViewModel() {

    val request: MutableLiveData<BVNRegistrationData> = MutableLiveData(BVNRegistrationData())

    fun getNigerianStateMap(nigerianState: NigerianState): MutableMap<String, ArrayList<String?>?> {
        val stateMap = mutableMapOf<String, ArrayList<String?>?>()
        stateMap["Abia"] = nigerianState.abia
        stateMap["Adamawa"] = nigerianState.adamawa
        stateMap["Akwa Ibom"] = nigerianState.akwaIbom
        stateMap["Anambra"] = nigerianState.anambra
        stateMap["Bauchi"] = nigerianState.bauchi
        stateMap["Bayelsa"] = nigerianState.bayelsa
        stateMap["Benue"] = nigerianState.benue
        stateMap["Borno"] = nigerianState.borno
        stateMap["Cross River"] = nigerianState.crossRiver
        stateMap["Delta"] = nigerianState.delta
        stateMap["Ebonyi"] = nigerianState.ebonyi
        stateMap["Edo"] = nigerianState.edo
        stateMap["Ekiti"] = nigerianState.ekiti
        stateMap["Enugu"] = nigerianState.enugu
        stateMap["FCT"] = nigerianState.fCT
        stateMap["Gombe"] = nigerianState.gombe
        stateMap["Imo"] = nigerianState.imo
        stateMap["Jigawa"] = nigerianState.jigawa
        stateMap["Kaduna"] = nigerianState.kaduna
        stateMap["Kano"] = nigerianState.kano
        stateMap["Katsina"] = nigerianState.katsina
        stateMap["Kebbi"] = nigerianState.kebbi
        stateMap["Kogi"] = nigerianState.kogi
        stateMap["Kwara"] = nigerianState.kwara
        stateMap["Lagos"] = nigerianState.lagos
        stateMap["Nasarawa"] = nigerianState.nasarawa
        stateMap["Niger"] = nigerianState.niger
        stateMap["Ogun"] = nigerianState.ogun
        stateMap["Ondo"] = nigerianState.ondo
        stateMap["Osun"] = nigerianState.osun
        stateMap["Oyo"] = nigerianState.oyo
        stateMap["Plateau"] = nigerianState.plateau
        stateMap["Rivers"] = nigerianState.rivers
        stateMap["Sokoto"] = nigerianState.sokoto
        stateMap["Taraba"] = nigerianState.taraba
        stateMap["Yobe"] = nigerianState.yobe
        stateMap["Zamfara"] = nigerianState.zamfara

        return stateMap
    }
}