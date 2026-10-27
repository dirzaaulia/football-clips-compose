@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.dirzaaulia.footballclips.monitor.presentation

@JsFun(
    """() => {
    try {
        const qp = new URL(location.href).searchParams.get('ownerToken');
        if (qp) return qp;
    } catch(e) {}
    try {
        const ot = localStorage.getItem('fc-owner-token');
        if (ot) return ot;
    } catch(e) {}
    try {
        const raw = localStorage.getItem('fc-auth-user');
        if (raw) {
            const s = JSON.parse(raw);
            if (s.access_token) return s.access_token;
            if (s.token) return s.token;
            if (s.jwt) return s.jwt;
        }
    } catch(e) {}
    try {
        for (const key of Object.keys(localStorage)) {
            if (key.startsWith('sb-') && key.endsWith('-auth-token')) {
                try {
                    const s = JSON.parse(localStorage.getItem(key));
                    if (s.access_token) return s.access_token;
                } catch(e) {}
            }
        }
    } catch(e) {}
    return '';
}""",
)
external fun ownerToken(): String

@JsFun("() => document.visibilityState === 'visible'")
external fun tabVisible(): Boolean

@JsFun("() => new Date().toISOString()")
external fun browserTime(): String

@JsFun("(value) => Date.parse(value) || 0")
external fun epochMillis(value: String): Double

@JsFun("(minutes) => new Date(Date.now() - minutes * 60000).toISOString()")
external fun sinceTime(minutes: Int): String

@JsFun("(name, value) => localStorage.setItem('fc-ops-' + name, value)")
external fun savePreference(name: String, value: String)

@JsFun("(name) => localStorage.getItem('fc-ops-' + name) || ''")
external fun preference(name: String): String

@JsFun("(name) => new URL(location.href).searchParams.get(name) || ''")
external fun filterParameter(name: String): String

@JsFun(
    """(value) => {
    const d = new Date(value);
    return !value || isNaN(d) ? 'Unknown' : new Intl.DateTimeFormat('en-GB', {
        timeZone: 'Asia/Jakarta',
        day: '2-digit',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hourCycle: 'h23'
    }).format(d) + ' WIB';
}""",
)
external fun displayTime(value: String): String

@JsFun("(value) => {const n=Date.parse(value+'+07:00'); return Number.isNaN(n) ? '' : new Date(n).toISOString();}")
external fun wibInput(value: String): String

@JsFun(
    """(text) => {
    const parsed=JSON.parse(text);
    const fmt=new Intl.DateTimeFormat('en-GB',{timeZone:'Asia/Jakarta',dateStyle:'medium',timeStyle:'medium',hourCycle:'h23'});
    (parsed.events || []).forEach(r=>{r.time_wib=fmt.format(new Date(r.created_at))+' WIB';});
    const blob = new Blob([JSON.stringify(parsed,null,2)], {type:'application/json'});
    const url = URL.createObjectURL(blob); const a = document.createElement('a');
    a.href=url; a.download='footballclips-operations.json'; a.click(); URL.revokeObjectURL(url);
}""",
)
external fun exportJson(text: String)

@JsFun(
    """(text) => {
    const rows=JSON.parse(text).events || [];
    const fmt=new Intl.DateTimeFormat('en-GB',{timeZone:'Asia/Jakarta',dateStyle:'medium',timeStyle:'medium',hourCycle:'h23'});
    rows.forEach(r=>{r.time_wib=fmt.format(new Date(r.created_at))+' WIB';});
    const fields=['created_at','time_wib','pipeline','service','level','match_id','job_id','trace_id','message'];
    const cell=value => { let s=String(value ?? ''); if(/^[=+@-]/.test(s)) s="'"+s;
        return '"'+s.replaceAll('"','""')+'"'; };
    const csv=[fields.map(cell).join(','),...rows.map(r=>fields.map(k=>cell(r[k])).join(','))].join('\r\n');
    const url=URL.createObjectURL(new Blob([csv],{type:'text/csv;charset=utf-8'}));
    const a=document.createElement('a'); a.href=url; a.download='footballclips-events.csv'; a.click(); URL.revokeObjectURL(url);
}""",
)
external fun exportCsv(text: String)

@JsFun("(code) => {try{return new Intl.DisplayNames(['en'],{type:'region'}).of(code)||code;}catch(e){return code;}}")
external fun countryName(code: String): String

@JsFun("(text) => { try { navigator.clipboard.writeText(text); } catch (e) {} }")
external fun copyToClipboard(text: String)
