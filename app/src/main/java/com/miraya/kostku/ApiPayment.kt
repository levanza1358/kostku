package com.miraya.kostku

import org.json.JSONObject

// Grup API untuk pembayaran, pengeluaran, dan kategori.

internal suspend fun KostkuApi.createPayment(tenantId:String,period:String,amount:Int,paidAt:String,method:String,status:String,category:String?=null)=runCatching{obj(request("payments","POST",JSONObject().put("tenant_id",tenantId).put("period_month",period).put("amount",amount).put("paid_at",paidAt).put("method",method).put("status",status).putOpt("category",category)))}
internal suspend fun KostkuApi.updatePayment(id:String,period:String,amount:Int,paidAt:String,method:String,status:String,referenceNumber:String?,notes:String?,category:String?=null)=runCatching{obj(request("payments/$id","PATCH",JSONObject().put("period_month",period).put("amount",amount).put("paid_at",paidAt).put("method",method).put("status",status).putOpt("category",category).putOpt("reference_number",referenceNumber).putOpt("notes",notes)))}
internal suspend fun KostkuApi.createCategory(kind:String,name:String)=runCatching{obj(request("categories","POST",JSONObject().put("kind",kind).put("name",name)))}
internal suspend fun KostkuApi.renameCategory(id:String,name:String)=runCatching{obj(request("categories/$id","PATCH",JSONObject().put("name",name)))}
internal suspend fun KostkuApi.deleteCategory(id:String)=runCatching{request("categories/$id","DELETE")}
internal suspend fun KostkuApi.deletePayment(id:String)=runCatching{request("payments/$id","DELETE")}
internal suspend fun KostkuApi.createExpense(kind:String,category:String?,tenantId:String?,propertyId:String?,amount:Int,spentAt:String,method:String,notes:String?)=runCatching{obj(request("expenses","POST",JSONObject().put("kind",kind).putOpt("category",category).putOpt("tenant_id",tenantId).putOpt("property_id",propertyId).put("amount",amount).put("spent_at",spentAt).put("method",method).putOpt("notes",notes)))}
internal suspend fun KostkuApi.updateExpense(id:String,kind:String,category:String?,tenantId:String?,propertyId:String?,amount:Int,spentAt:String,method:String,notes:String?)=runCatching{obj(request("expenses/$id","PATCH",JSONObject().put("kind",kind).putOpt("category",category).putOpt("tenant_id",tenantId).putOpt("property_id",propertyId).put("amount",amount).put("spent_at",spentAt).put("method",method).putOpt("notes",notes)))}
internal suspend fun KostkuApi.deleteExpense(id:String)=runCatching{request("expenses/$id","DELETE")}
