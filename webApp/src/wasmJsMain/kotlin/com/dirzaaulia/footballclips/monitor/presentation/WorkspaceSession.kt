@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.dirzaaulia.footballclips.monitor.presentation

internal fun sessionGet(key: String): String = readSession(ownerToken(), key)
internal fun sessionPut(key: String, value: String) = writeSession(ownerToken(), key, value)
internal fun contextKey(state: MonitorState): String =
    state.page + ":" + state.filter.toString() + ":" + state.catalogMode

@JsFun(
    """(token,key)=>{
 let sub;try{sub=JSON.parse(atob(token.split('.')[1].replace(/-/g,'+').replace(/_/g,'/'))).sub;}catch(e){return '';}
 return sessionStorage.getItem('fc-monitor:'+sub+':'+key)||'';
}""",
)
private external fun readSession(token: String, key: String): String

@JsFun(
    """(token,key,value)=>{
 let sub;try{sub=JSON.parse(atob(token.split('.')[1].replace(/-/g,'+').replace(/_/g,'/'))).sub;}catch(e){return;}
 try{sessionStorage.setItem('fc-monitor:'+sub+':'+key,value);}catch(e){}
}""",
)
private external fun writeSession(token: String, key: String, value: String)
