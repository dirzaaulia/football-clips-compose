@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.dirzaaulia.footballclips.monitor.data

import com.dirzaaulia.footballclips.monitor.presentation.ownerToken
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlin.coroutines.resume

internal fun cachePage(path: String, value: String) = writePage(ownerToken(), path, value)

internal suspend fun cachedPages(path: String): List<JsonObject> = suspendCancellableCoroutine { continuation ->
    readPages(ownerToken(), path) { value ->
        if (continuation.isActive) {
            continuation.resume(
                Json.parseToJsonElement(value).jsonArray.mapNotNull { it as? JsonObject },
            )
        }
    }
}

@JsFun(
    """(token,path,value) => {
 if(!token)return;
 let owner;try{owner=JSON.parse(atob(token.split('.')[1].replace(/-/g,'+').replace(/_/g,'/'))).sub;}catch(e){return;}
 const session=sessionStorage.fc_monitor_session||(sessionStorage.fc_monitor_session=crypto.randomUUID());
 const url=new URL(path,'https://monitor.invalid');url.searchParams.delete('since');url.searchParams.delete('until');
 const key=owner+':'+session+':'+url.pathname+url.search;
 const r=indexedDB.open('fc-monitor-pages',1);
 r.onupgradeneeded=()=>r.result.createObjectStore('pages',{keyPath:'key'});
 r.onsuccess=()=>{const db=r.result;const t=db.transaction('pages','readwrite');
 t.objectStore('pages').put({key,value,time:Date.now()});t.oncomplete=()=>db.close();};
}""",
)
private external fun writePage(token: String, path: String, value: String)

@JsFun(
    """(token,path,done) => {
 if(!token){done('[]');return;}
 let owner;try{owner=JSON.parse(atob(token.split('.')[1].replace(/-/g,'+').replace(/_/g,'/'))).sub;}catch(e){done('[]');return;}
 const session=sessionStorage.fc_monitor_session||(sessionStorage.fc_monitor_session=crypto.randomUUID());
 const url=new URL(path,'https://monitor.invalid');url.searchParams.delete('since');url.searchParams.delete('until');
 const prefix=owner+':'+session+':'+url.pathname+url.search;
 const r=indexedDB.open('fc-monitor-pages',1);r.onerror=()=>done('[]');
 r.onupgradeneeded=()=>r.result.createObjectStore('pages',{keyPath:'key'});
 r.onsuccess=()=>{const db=r.result;const t=db.transaction('pages','readonly');const q=t.objectStore('pages').getAll();
 q.onerror=()=>{done('[]');db.close();};q.onsuccess=()=>{done(JSON.stringify(q.result.filter(x=>x.key.startsWith(prefix)&&Date.now()-x.time<86400000).map(x=>JSON.parse(x.value))));db.close();};};
}""",
)
private external fun readPages(token: String, path: String, done: (String) -> Unit)
