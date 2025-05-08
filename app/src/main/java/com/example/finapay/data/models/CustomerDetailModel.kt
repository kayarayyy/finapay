package com.example.finapay.data.models

data class CustomerDetailModel(
	val province: String? = null,
	val plafond: PlafondModel? = null,
	val street: String? = null,
	val district: String? = null,
	val postalCode: String? = null,
	val latitude: Any? = null,
	val id: String? = null,
	val availablePlafond: String? = null,
	val usedPlafond: String? = null,
	val user: UserModel? = null,
	val longitude: Any? = null
)


