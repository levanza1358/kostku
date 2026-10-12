package com.miraya.kostku

import org.json.JSONObject

// Grup API autentikasi & profil pengguna.

internal suspend fun KostkuApi.authGoogle(t:String)=runCatching{val o=obj(request("auth/google","POST",JSONObject().put("id_token",t),false));val a=o.optString("access_token").takeIf{it.isNotBlank()}?:error("Missing access token");tokens.accessToken=a;tokens.refreshToken=o.optString("refresh_token").ifBlank{null};profileFrom(o.optJSONObject("user")?:JSONObject())}
internal suspend fun KostkuApi.me():Result<UserProfile> = runCatching{profileFrom(obj(request("me")).optJSONObject("user")?:JSONObject())}.recoverCatching{ e -> cachedProfile()?.let{ throw CachedProfile(it) } ?: throw e }
internal suspend fun KostkuApi.logout(){runCatching{request("auth/logout","POST")};tokens.clear();cache.clear()}
internal fun KostkuApi.hasSession()=!tokens.accessToken.isNullOrBlank()||!tokens.refreshToken.isNullOrBlank()
internal fun KostkuApi.clearSession(){tokens.clear();cache.clear()}
private fun KostkuApi.profileFrom(o:JSONObject)=UserProfile(o.optString("id"),o.optString("email"),o.optString("display_name",o.optString("name")),o.optText("photo_url")).also{ persistProfile(it) }
private fun KostkuApi.persistProfile(p:UserProfile)=tokens.apply{profileJson=JSONObject().put("id",p.id).put("email",p.email).put("name",p.name).put("photo_url",p.photoUrl).toString()}
private fun KostkuApi.cachedProfile():UserProfile?=tokens.profileJson?.let{ raw -> runCatching{val o=JSONObject(raw);UserProfile(o.optString("id"),o.optString("email"),o.optString("name"),o.optText("photo_url"))}.getOrNull() }
/** Profil terakhir dari cache lokal (untuk pembukaan instan tanpa menunggu jaringan). */
internal fun KostkuApi.cachedUser():UserProfile?=cachedProfile()
