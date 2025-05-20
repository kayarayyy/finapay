package com.example.finapay.data.models

data class CustomerDetailModel(
	val id: String? = null,
	val street: String? = null,
	val district: String? = null,
	val province: String? = null,
	val postalCode: String? = null,
	val latitude: Double? = null,
	val longitude: Double? = null,
	val gender: String? = null,
	val ttl: String? = null,
	val formattedTtl: String? = null,
	val phone: String? = null,
	val nik: String? = null,
	val mothersName: String? = null,
	val job: String? = null,
	val salary: String? = null,
	val account: String? = null,
	val houseStatus: String? = null,
	val selfie: String? = null,
	val house: String? = null,
	val ktp: String? = null,
	val ktpUrl: String? = null,
	val selfieKtpUrl: String? = null,
	val houseUrl: String? = null,
	val plafond: PlafondModel? = null,
	val availablePlafond: String? = null,
	val usedPlafond: String? = null,
	val user: UserModel? = null,
)


