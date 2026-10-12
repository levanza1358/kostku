package com.miraya.kostku

import org.json.JSONObject

// Model data yang dipakai di seluruh aplikasi. Semua *Remote* adalah hasil parsing JSON
// dari API (snapshot) server.

internal data class UserProfile(val id:String="",val email:String="",val name:String="",val photoUrl:String?=null)
internal data class RemoteDashboard(val properties:Int,val rooms:Int,val tenants:Int,val payments:Int)
internal data class RemoteLocation(val id:String,val name:String,val branch:String,val address:String,val description:String,val notes:String="",val tokenListrik:String?=null,val pdamNumber:String?=null,val logoUrl:String?=null)
internal data class RemoteRoom(val id:String,val propertyId:String,val name:String,val status:String,val monthlyRent:Int=0,val roomTypeId:String?=null,val roomTypeName:String?=null,val photoUrl:String?=null,val tokenListrik:String?=null,val pdamNumber:String?=null)
internal data class RemoteRoomType(val id:String,val propertyId:String,val name:String)
internal data class RemoteRoomTypePrice(val id:String,val roomTypeId:String,val occupancy:Int,val durationValue:Int,val durationUnit:String,val amount:Int)
internal data class RemoteTenant(val id:String,val name:String,val roomId:String,val phone:String,val moveIn:String,val rent:Int,val dueDay:String,val active:Boolean,val photoUrl:String?=null,val moveOut:String?=null)
internal data class RemotePayment(val id:String,val tenantId:String,val amount:Int,val period:String,val method:String,val status:String,val category:String?=null,val photoUrl:String?=null,val paidAt:String?=null,val createdAt:String?=null)
internal data class RemoteAuditEntry(val id:String,val action:String,val details:String?,val createdAt:String)
internal data class RemoteExpense(val id:String,val kind:String,val category:String?,val tenantId:String?,val propertyId:String?,val amount:Int,val spentAt:String,val method:String,val notes:String?,val photoUrl:String?=null)
internal data class RemoteCategory(val id:String,val kind:String,val name:String)
internal data class RemoteMoveOutHistory(val id:String,val name:String,val phone:String,val roomName:String?,val moveIn:String?,val moveOut:String,val rent:Int,val photoUrl:String?=null)
internal data class RemoteSnapshot(val locations:List<RemoteLocation>,val rooms:List<RemoteRoom>,val roomTypes:List<RemoteRoomType>,val tenants:List<RemoteTenant>,val payments:List<RemotePayment>,val prices:List<RemoteRoomTypePrice> =emptyList(),val expenses:List<RemoteExpense> =emptyList(),val categories:List<RemoteCategory> =emptyList())

/** String JSON opsional yang aman: null, kosong, atau literal "null" → null. */
internal fun JSONObject.optText(key:String):String? = if(isNull(key)) null else optString(key).takeIf { it.isNotBlank() && it != "null" }

/** Bangun RemoteLocation dari JSON properti (utilitas + logo opsional). */
internal fun locationFrom(p:JSONObject):RemoteLocation=RemoteLocation(
    p.optString("id"),p.optString("name"),p.optString("branch"),p.optString("address"),
    p.optString("description",p.optString("notes")),p.optString("notes"),
    p.optText("token_listrik"),p.optText("pdam_number"),p.optText("logo_url"))

/** Bangun RemoteRoom dari JSON kamar (utilitas opsional; propertyId fallback bila tak ada di JSON). */
internal fun roomFrom(r:JSONObject,fallbackPropertyId:String?=null):RemoteRoom=RemoteRoom(
    r.optString("id"),r.optText("property_id")?:fallbackPropertyId.orEmpty(),r.optString("name"),
    r.optString("status","vacant"),r.optDouble("monthly_rent").toInt(),
    r.optText("room_type_id"),r.optText("room_type_name"),r.optText("photo_url"),
    r.optText("token_listrik"),r.optText("pdam_number"))
