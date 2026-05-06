// ========== دوال مساعدة آمنة ==========

// عرض إشعارات بدون تفاصيل حساسة
window.showToast = function(msg, isErr = false) {
    // تنظيف الرسالة من أي معلومات حساسة
    let safeMsg = msg;
    
    // إخفاء أي معلومات عن Firebase أو Cloudinary
    const sensitivePatterns = [
        /firebase/i, /cloudinary/i, /api[_-]?key/i, /secret/i, /token/i,
        /AIzaSy[A-Za-z0-9_-]{33}/, /dmhtx93nj/i, /fcloud_preset/i
    ];
    
    sensitivePatterns.forEach(pattern => {
        if(pattern.test(safeMsg)) {
            safeMsg = "حدث خطأ داخلي. يرجى المحاولة لاحقاً.";
        }
    });
    
    // إخفاء مسارات الملفات
    if(safeMsg.includes('assets/') || safeMsg.includes('.js') || safeMsg.includes('.html')) {
        safeMsg = "حدث خطأ في التطبيق.";
    }
    
    let t = document.createElement('div');
    t.className = 'toast';
    t.innerHTML = safeMsg;
    t.style.background = isErr ? '#dc2626' : '#1e293b';
    document.body.appendChild(t);
    setTimeout(() => t.remove(), 2800);
    
    // تسجيل الأخطاء الحقيقية في console فقط للمطور
    if(isErr && msg !== safeMsg) {
        console.warn("[DEV] خطأ تم تنظيفه:", msg);
    }
};

// دوال مساعدة عامة - آمنة
window.escapeHtml = function(s) {
    if(!s) return '';
    return s.replace(/[&<>]/g, m => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;' })[m]);
};

window.validatePhone = function(p) {
    return /^[\+]?[0-9]{8,15}$/.test(p.replace(/\s/g, ''));
};

// تنبيه صوتي مع حد أقصى للتكرار
let lastBeepTime = 0;
window.playNotificationBeep = function(freq = 880) {
    if(window.isSoundMuted) return;
    
    // منع تكرار التنبيه أكثر من مرة كل 3 ثوانٍ
    const now = Date.now();
    if(now - lastBeepTime < 3000) return;
    lastBeepTime = now;
    
    try {
        let ctx = new (AudioContext || webkitAudioContext)();
        let osc = ctx.createOscillator();
        let gain = ctx.createGain();
        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.frequency.value = freq;
        gain.gain.value = 0.15; // صوت أقل
        osc.start();
        gain.gain.exponentialRampToValueAtTime(0.0001, ctx.currentTime + 0.3);
        osc.stop(ctx.currentTime + 0.3);
        
        // إغلاق الـ context بعد انتهاء الصوت لتوفير الموارد
        setTimeout(() => ctx.close(), 500);
    } catch(e) {
        console.log("[DEV] Audio context error");
    }
};

// متغيرات عامة آمنة
window.currentUser = null;
window.guidesList = [];
window.tasksList = [];
window.notesList = [];
window.eventsList = [];
window.userAvatarUrl = "";
window.currentDisplayName = "";
window.isSoundMuted = false;
window.currentChatRoom = "general";
window.chatUnsub = null;
window.taskReminderInterval = null;

// دوال آمنة للتعامل مع الأخطاء
window.safeAsync = async function(fn, errorMsg = "حدث خطأ") {
    try {
        return await fn();
    } catch(e) {
        console.error("[DEV]", e);
        // عرض رسالة عامة بدون تفاصيل
        window.showToast(errorMsg, true);
        return null;
    }
};
