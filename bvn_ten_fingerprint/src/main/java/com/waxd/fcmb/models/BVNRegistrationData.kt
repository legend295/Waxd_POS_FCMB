package com.waxd.fcmb.models

import java.io.Serializable

data class BVNRegistrationData(
    var surname: String? = null,
    var firstName: String? = null,
    var middleName: String? = null,
    var customerId: String? = null,
    var nin: String? = null,
    var title: String? = null,
    var maritalStatus: String? = null,
    var gender: String? = null,
    var dateOfBirth: String? = null,
    var nationality: String? = null,
    var stateOfOrigin: String? = null,
    var lgaOfOrigin: String? = null,
    var lgaOfResidence: String? = null,
    var stateOfResidence: String? = null,
    var residentialAddress: String? = null,
    var landmark: String? = null,
    var phoneNumberOne: String? = null,
    var phoneNumberTwo: String? = null,
    var emailAddress: String? = null,
    var locationOfCollection: String? = null,
    var specialNeeds: Boolean? = null,
    var specialNeedsExplanation: String? = null
) : Serializable