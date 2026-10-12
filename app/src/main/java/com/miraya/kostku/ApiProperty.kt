package com.miraya.kostku

import org.json.JSONArray
import org.json.JSONObject

// Grup API untuk properti/lokasi, tipe kamar, harga tipe kamar, dan kamar.

internal suspend fun KostkuApi.createProperty(name:String,branch:String,address:String,description:String)=runCatching{obj(request("properties","POST",JSONObject().put("name",name).put("branch",branch).put("address",address).put("description",description).put("notes",description)))}
internal suspend fun KostkuApi.updateProperty(id:String,name:String,branch:String,address:String,description:String,tokenListrik:String?=null,pdamNumber:String?=null)=runCatching{obj(request("properties/$id","PATCH",JSONObject().put("name",name).put("branch",branch).put("address",address).put("description",description).put("notes",description).put("token_listrik",tokenListrik?:"").put("pdam_number",pdamNumber?:"")))}
internal suspend fun KostkuApi.deleteProperty(id:String)=runCatching{request("properties/$id","DELETE")}
internal suspend fun KostkuApi.createRoom(propertyId:String,name:String,status:String,rent:Int,roomTypeId:String)=runCatching{obj(request("properties/$propertyId/rooms","POST",JSONObject().put("name",name).put("status",status).put("monthly_rent",rent).put("room_type_id",roomTypeId)))}
internal suspend fun KostkuApi.createRoomsBatch(propertyId:String,roomTypeId:String,status:String,names:List<String>)=runCatching{val rows=JSONArray();names.forEach{rows.put(JSONObject().put("name",it))};obj(request("properties/$propertyId/rooms/batch","POST",JSONObject().put("room_type_id",roomTypeId).put("status",status).put("rooms",rows)))}
internal suspend fun KostkuApi.createRoomType(propertyId:String,name:String)=runCatching{obj(request("properties/$propertyId/room-types","POST",JSONObject().put("name",name)))}
internal suspend fun KostkuApi.updateRoomType(id:String,name:String)=runCatching{obj(request("room-types/$id","PATCH",JSONObject().put("name",name)))}
internal suspend fun KostkuApi.deleteRoomType(id:String)=runCatching{request("room-types/$id","DELETE")}
internal suspend fun KostkuApi.properties()=runCatching{arr(request("properties")).let { a -> (0 until a.length()).map { locationFrom(a.getJSONObject(it)) } }}
internal suspend fun KostkuApi.propertyRoomTypes(propertyId:String)=runCatching{arr(request("properties/$propertyId/room-types")).let { a -> (0 until a.length()).map { val t=a.getJSONObject(it); RemoteRoomType(t.optString("id"),propertyId,t.optString("name")) } }}
internal suspend fun KostkuApi.propertyRooms(propertyId:String)=runCatching{arr(request("properties/$propertyId/rooms")).let { a -> (0 until a.length()).map { roomFrom(a.getJSONObject(it),propertyId) } }}
internal suspend fun KostkuApi.roomTypePrices(typeId:String)=runCatching{arr(request("room-types/$typeId/prices")).let { a -> (0 until a.length()).map { val p=a.getJSONObject(it); RemoteRoomTypePrice(p.optString("id"),typeId,p.optInt("occupancy"),p.optInt("duration_value",1),p.optString("duration_unit","month"),p.optDouble("amount").toInt()) } }}
internal suspend fun KostkuApi.createRoomTypePrice(typeId:String,occupancy:Int,durationValue:Int,durationUnit:String,amount:Int)=runCatching{obj(request("room-types/$typeId/prices","POST",JSONObject().put("occupancy",occupancy).put("duration_value",durationValue).put("duration_unit",durationUnit).put("amount",amount)))}
internal suspend fun KostkuApi.updateRoomTypePrice(id:String,occupancy:Int,durationValue:Int,durationUnit:String,amount:Int)=runCatching{obj(request("room-type-prices/$id","PATCH",JSONObject().put("occupancy",occupancy).put("duration_value",durationValue).put("duration_unit",durationUnit).put("amount",amount)))}
internal suspend fun KostkuApi.deleteRoomTypePrice(id:String)=runCatching{request("room-type-prices/$id","DELETE")}
internal suspend fun KostkuApi.updateRoom(id:String,name:String,roomTypeId:String,status:String,tokenListrik:String?=null,pdamNumber:String?=null)=runCatching{obj(request("rooms/$id","PATCH",JSONObject().put("name",name).put("room_type_id",roomTypeId).put("status",status).put("token_listrik",tokenListrik?:"").put("pdam_number",pdamNumber?:"")))}
internal suspend fun KostkuApi.deleteRoom(id:String)=runCatching{request("rooms/$id","DELETE")}
