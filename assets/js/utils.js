window.showToast = function(msg, isErr=false) {
    let t = document.createElement('div'); t.className = 'toast'; t.innerHTML = msg;
    t.style.background = isErr ? '#dc2626' : '#1e293b';
    document.body.appendChild(t);
    setTimeout(() => t.remove(), 2800);
};
window.escapeHtml = function(s) { return s ? s.replace(/[&<>]/g, m => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;' })[m]) : ''; };
window.validatePhone = function(p) { return /^[\+]?[0-9]{8,15}$/.test(p.replace(/\s/g, '')); };

window.playNotificationBeep = function(freq=880) {
    if(window.isSoundMuted) return;
    try {
        let ctx = new (AudioContext || webkitAudioContext)();
        let osc = ctx.createOscillator();
        let gain = ctx.createGain();
        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.frequency.value = freq;
        gain.gain.value = 0.2;
        osc.start();
        gain.gain.exponentialRampToValueAtTime(0.0001, ctx.currentTime + 0.4);
        osc.stop(ctx.currentTime + 0.4);
    } catch(e){}
};

// متغيرات عالمية نظيفة
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
