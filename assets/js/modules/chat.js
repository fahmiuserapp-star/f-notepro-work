// متغيرات الدردشة
let chatLastDoc = null;
let chatLoadingMore = false;
let chatHasMore = true;
let recordingMediaRecorder = null;
let audioChunks = [];
let isRecordingVoice = false;
let recordingStartTime = 0;
let recordingTimerInterval = null;
let currentAudioPlayer = null;

// تهيئة عناصر الدردشة
function initChatElements() {
    const attachBtn = document.getElementById('attachFileBtn');
    const locationBtn = document.getElementById('sendLocationBtn');
    const walkieBtn = document.getElementById('walkieTalkieBtn');
    const sendBtn = document.getElementById('sendMessageBtn');
    const chatInput = document.getElementById('chatTextInput');
    const backBtn = document.getElementById('chatBackBtn');
    const settingsBtn = document.getElementById('chatSettingsBtn');
    const clearBtn = document.getElementById('clearMessagesMenuItem'); // من القائمة الجانبية

    if(attachBtn) attachBtn.onclick = () => selectFileAndSend();
    if(locationBtn) locationBtn.onclick = () => sendLocationMessage();
    if(walkieBtn) walkieBtn.onclick = () => toggleVoiceRecording();
    if(sendBtn) sendBtn.onclick = () => sendTextMessage();
    if(chatInput) chatInput.addEventListener('keypress', (e) => { if(e.key === 'Enter') sendTextMessage(); });
    if(backBtn) backBtn.onclick = () => { document.getElementById('chatPageScreen').style.display = 'none'; document.getElementById('mainApp').style.display = 'flex'; };
    if(settingsBtn) settingsBtn.onclick = () => document.getElementById('chatSettingsPanel').classList.toggle('show');
    if(clearBtn) clearBtn.onclick = () => clearAllMessages();
}

// إرسال رسالة نصية
async function sendTextMessage() {
    let text = document.getElementById('chatTextInput').value.trim();
    if(!text) return;
    await db.collection(`chat_${window.currentChatRoom}`).add({
        text, senderId: window.currentUser.uid, senderName: window.currentDisplayName,
        senderAvatar: window.userAvatarUrl, type: 'text',
        timestamp: firebase.firestore.FieldValue.serverTimestamp()
    });
    document.getElementById('chatTextInput').value = '';
    playNotificationBeep(660); // تأكيد الإرسال
}

// اختيار ملف وإرساله مع مؤشر تحميل
async function selectFileAndSend() {
    let inp = document.createElement('input');
    inp.type = 'file';
    inp.accept = 'image/*,video/*,application/pdf,application/msword,text/plain';
    inp.onchange = async e => {
        if(e.target.files[0]) {
            let file = e.target.files[0];
            window.showToast("جاري رفع الملف...");
            try {
                let url = await window.uploadToCloudinary(file, 'chat');
                await db.collection(`chat_${window.currentChatRoom}`).add({
                    mediaUrl: url, mediaType: file.type.startsWith('image/') ? 'image' : (file.type.startsWith('video/') ? 'video' : 'file'),
                    fileName: file.name, senderId: window.currentUser.uid, senderName: window.currentDisplayName,
                    senderAvatar: window.userAvatarUrl, type: 'media',
                    timestamp: firebase.firestore.FieldValue.serverTimestamp()
                });
                window.showToast("تم إرسال الملف ✅");
            } catch(err) { window.showToast("فشل الرفع: " + err.message, true); }
        }
    };
    inp.click();
}

// إرسال الموقع
async function sendLocationMessage() {
    if (!navigator.geolocation) return window.showToast("الموقع غير مدعوم", true);
    window.showToast("جاري الحصول على الموقع...");
    navigator.geolocation.getCurrentPosition(async pos => {
        let mapsUrl = `https://www.google.com/maps?q=${pos.coords.latitude},${pos.coords.longitude}`;
        await db.collection(`chat_${window.currentChatRoom}`).add({
            locationUrl: mapsUrl, lat: pos.coords.latitude, lng: pos.coords.longitude,
            senderId: window.currentUser.uid, senderName: window.currentDisplayName,
            senderAvatar: window.userAvatarUrl, type: 'location',
            timestamp: firebase.firestore.FieldValue.serverTimestamp()
        });
        window.showToast("📍 تم إرسال الموقع");
    }, () => window.showToast("تعذر الحصول على الموقع", true));
}

// ========== التسجيل الصوتي مع عداد ثواني ==========
async function toggleVoiceRecording() {
    if(isRecordingVoice) {
        stopVoiceRecording();
        return;
    }
    try {
        let stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        recordingMediaRecorder = new MediaRecorder(stream);
        audioChunks = [];
        recordingMediaRecorder.ondataavailable = e => audioChunks.push(e.data);
        recordingMediaRecorder.onstop = async () => {
            if(audioChunks.length === 0) return;
            let blob = new Blob(audioChunks, { type: 'audio/webm' });
            // إظهار لوحة المعاينة مع عداد الثواني
            showVoicePreviewPanel(blob);
            stream.getTracks().forEach(t => t.stop());
            isRecordingVoice = false;
            recordingMediaRecorder = null;
            document.getElementById('walkieTalkieBtn').style.background = '#25D366';
            if(recordingTimerInterval) clearInterval(recordingTimerInterval);
        };
        recordingMediaRecorder.start();
        isRecordingVoice = true;
        recordingStartTime = Date.now();
        // تغيير لون الزر إلى الأحمر
        document.getElementById('walkieTalkieBtn').style.background = '#ef4444';
        // بدء عداد الثواني يعرض في واجهة المسجل
        startRecordingTimer();
    } catch(e) {
        window.showToast("الوصول للميكروفون مرفوض", true);
    }
}

function startRecordingTimer() {
    if(recordingTimerInterval) clearInterval(recordingTimerInterval);
    recordingTimerInterval = setInterval(() => {
        let elapsed = Math.floor((Date.now() - recordingStartTime) / 1000);
        if(elapsed >= 120) {
            // حد أقصى دقيقتين
            stopVoiceRecording();
            window.showToast("تم الوصول للحد الأقصى (دقيقتين)", true);
        } else {
            // تحديث عداد في لوحة التسجيل (سنظهر لوحة تسجيل منبثقة)
            let timerSpan = document.getElementById('recordingTimerDisplay');
            if(timerSpan) timerSpan.innerText = formatTime(elapsed);
        }
    }, 1000);
}

function stopVoiceRecording() {
    if(recordingMediaRecorder && recordingMediaRecorder.state !== 'inactive') {
        recordingMediaRecorder.stop();
    }
}

function showVoicePreviewPanel(blob) {
    // إخفاء لوحة التسجيل القديمة إن وجدت، ثم إنشاء لوحة جديدة
    let existing = document.getElementById('dynamicVoicePreview');
    if(existing) existing.remove();
    let panel = document.createElement('div');
    panel.id = 'dynamicVoicePreview';
    panel.className = 'recorder-panel show';
    panel.style.position = 'fixed'; panel.style.bottom = '80px'; panel.style.left = '20px'; panel.style.right = '20px';
    panel.style.background = 'var(--card-bg)'; panel.style.borderRadius = '60px'; panel.style.padding = '12px 20px';
    panel.style.display = 'flex'; panel.style.alignItems = 'center'; panel.style.gap = '15px';
    panel.style.zIndex = '200'; panel.style.backdropFilter = 'blur(12px)';
    
    // أيقونة تشغيل مؤقتة
    const playBtn = document.createElement('button');
    playBtn.className = 'voice-play-btn';
    playBtn.innerHTML = '<i class="fas fa-play"></i>';
    playBtn.style.background = 'none'; playBtn.style.border = 'none'; playBtn.style.fontSize = '24px'; playBtn.style.cursor = 'pointer';
    let audio = new Audio(URL.createObjectURL(blob));
    playBtn.onclick = () => {
        if(audio.paused) { audio.play(); playBtn.innerHTML = '<i class="fas fa-pause"></i>'; }
        else { audio.pause(); playBtn.innerHTML = '<i class="fas fa-play"></i>'; }
    };
    audio.onended = () => { playBtn.innerHTML = '<i class="fas fa-play"></i>'; };
    
    const timerSpan = document.createElement('span');
    timerSpan.id = 'previewTimer';
    timerSpan.style.fontSize = '16px'; timerSpan.style.fontWeight = 'bold';
    timerSpan.style.minWidth = '50px';
    timerSpan.innerText = '0:00';
    // تحديث الوقت أثناء التشغيل
    audio.ontimeupdate = () => { timerSpan.innerText = formatTime(Math.floor(audio.currentTime)); };
    audio.onloadedmetadata = () => { timerSpan.innerText = formatTime(Math.floor(audio.duration)); };
    
    const cancelBtn = document.createElement('button');
    cancelBtn.innerHTML = '<i class="fas fa-times"></i>';
    cancelBtn.style.background = 'none'; cancelBtn.style.border = 'none'; cancelBtn.style.fontSize = '28px'; cancelBtn.style.cursor = 'pointer'; cancelBtn.style.color = '#dc2626';
    cancelBtn.onclick = () => panel.remove();
    
    const sendBtn = document.createElement('button');
    sendBtn.innerHTML = '<i class="fas fa-check-circle"></i>';
    sendBtn.style.background = 'none'; sendBtn.style.border = 'none'; sendBtn.style.fontSize = '28px'; sendBtn.style.cursor = 'pointer'; sendBtn.style.color = '#10b981';
    sendBtn.onclick = async () => {
        panel.remove();
        window.showToast("جاري رفع الرسالة الصوتية...");
        try {
            let url = await window.uploadAudioToCloudinary(blob);
            let duration = Math.floor(audio.duration || 0);
            await db.collection(`chat_${window.currentChatRoom}`).add({
                audioUrl: url, duration: duration, senderId: window.currentUser.uid,
                senderName: window.currentDisplayName, senderAvatar: window.userAvatarUrl,
                type: 'voice',
                timestamp: firebase.firestore.FieldValue.serverTimestamp()
            });
            window.showToast("تم إرسال الرسالة الصوتية");
            playNotificationBeep(660);
        } catch(e) { window.showToast("فشل رفع الصوت", true); }
    };
    
    panel.appendChild(playBtn);
    panel.appendChild(timerSpan);
    panel.appendChild(cancelBtn);
    panel.appendChild(sendBtn);
    document.body.appendChild(panel);
}

function formatTime(sec) {
    let minutes = Math.floor(sec / 60);
    let seconds = sec % 60;
    return `${minutes}:${seconds.toString().padStart(2,'0')}`;
}

// ========== تحميل وعرض الرسائل مع Pagination صحيحة ==========
function loadChatMessages() {
    if(!window.currentUser) return;
    const container = document.getElementById('chatMessagesList');
    const loadMoreBtn = document.getElementById('chatLoadMoreBtn');
    if(!container) return;
    
    let isLoading = false;
    let lastDoc = null;
    let hasMore = true;
    
    const fetchMore = async () => {
        if(isLoading || !hasMore) return;
        isLoading = true;
        let query = db.collection(`chat_${window.currentChatRoom}`).orderBy('timestamp', 'desc').limit(20);
        if(lastDoc) query = query.startAfter(lastDoc);
        const snapshot = await query.get();
        if(snapshot.empty) { hasMore = false; loadMoreBtn.style.display = 'none'; isLoading=false; return; }
        lastDoc = snapshot.docs[snapshot.docs.length-1];
        if(snapshot.docs.length < 20) hasMore = false;
        const messages = snapshot.docs.map(doc => ({ id: doc.id, ...doc.data() })).reverse();
        appendMessages(messages);
        if(hasMore) loadMoreBtn.style.display = 'block';
        else loadMoreBtn.style.display = 'none';
        isLoading = false;
    };
    
    const appendMessages = (messages) => {
        let html = '';
        messages.forEach(msg => {
            html += renderMessageHtml(msg);
        });
        if(container.querySelector('.empty-state')) container.innerHTML = html;
        else container.innerHTML = html + container.innerHTML;
        container.scrollTop = container.scrollHeight;
    };
    
    // الاستماع للرسائل الجديدة فقط (real-time)
    if(window.chatUnsub) window.chatUnsub();
    window.chatUnsub = db.collection(`chat_${window.currentChatRoom}`).orderBy('timestamp', 'desc').limit(1).onSnapshot(snapshot => {
        if(!snapshot.empty && (!lastDoc || snapshot.docs[0].id !== lastDoc.id)) {
            // رسالة جديدة: نضيفها في الأسفل بدون إعادة تحميل كل شيء
            const newMsg = { id: snapshot.docs[0].id, ...snapshot.docs[0].data() };
            const msgHtml = renderMessageHtml(newMsg);
            // إضافة إلى الأسفل (الحاوية تعرض الأقدم أولاً)
            container.insertAdjacentHTML('beforeend', msgHtml);
            container.scrollTop = container.scrollHeight;
            if(!window.isSoundMuted) window.playNotificationBeep(800);
        } else if(snapshot.empty && container.innerHTML === '') {
            container.innerHTML = '<div class="empty-state"><i class="fas fa-comments"></i> لا توجد رسائل</div>';
        }
    });
    
    loadMoreBtn.onclick = fetchMore;
    // التحميل الأولي
    fetchMore();
}

function renderMessageHtml(data) {
    let isOwn = data.senderId === window.currentUser.uid;
    let time = data.timestamp ? data.timestamp.toDate().toLocaleTimeString() : '';
    let avatar = data.senderAvatar || '';
    let senderName = window.escapeHtml(data.senderName);
    if(data.type === 'text') {
        return `<div class="message-bubble ${isOwn ? 'own' : 'other'}">
                    <img class="message-avatar" src="${avatar}" onerror="this.src='data:image/svg+xml,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20fill%3D%22%23e94560%22%3E%3Ccircle%20cx%3D%2217%22%20cy%3D%2217%22%20r%3D%2217%22%20fill%3D%22%23e94560%22%2F%3E%3Ctext%20x%3D%2217%22%20y%3D%2222%22%20text-anchor%3D%22middle%22%20fill%3D%22white%22%20font-size%3D%2216%22%3E${senderName.charAt(0)}%3C%2Ftext%3E%3C%2Fsvg%3E'">
                    <div class="message-content">
                        <div class="message-text">${window.escapeHtml(data.text)}</div>
                        <div class="message-meta"><span>${senderName}</span><span>${time}</span></div>
                    </div>
                </div>`;
    } else if(data.type === 'voice') {
        return `<div class="message-bubble ${isOwn ? 'own' : 'other'}">
                    <img class="message-avatar" src="${avatar}">
                    <div class="message-content">
                        <div class="message-meta"><span>${senderName}</span><span>${time}</span></div>
                        <div class="voice-message-wrapper" data-url="${data.audioUrl}">
                            <button class="voice-play-btn" onclick="window.playVoiceMessage(this)"><i class="fas fa-play"></i></button>
                            <div class="voice-waveform-container">
                                <div class="voice-waveform-progress" style="width:0%"></div>
                                <div class="voice-waveform-bars">${Array(25).fill().map(() => '<span style="height:' + (8 + Math.random() * 25) + 'px"></span>').join('')}</div>
                            </div>
                            <span class="voice-duration">${formatTime(data.duration || 0)}</span>
                        </div>
                    </div>
                </div>`;
    } else if(data.type === 'media') {
        let mediaHtml = '';
        if(data.mediaType === 'image') mediaHtml = `<img class="message-image" src="${data.mediaUrl}" onclick="window.showFullImage('${data.mediaUrl}')">`;
        else if(data.mediaType === 'video') mediaHtml = `<video controls class="message-image" src="${data.mediaUrl}"></video>`;
        else mediaHtml = `<div class="message-file"><i class="fas fa-file"></i> <a href="${data.mediaUrl}" download="${data.fileName}" style="color:inherit;">${window.escapeHtml(data.fileName)}</a></div>`;
        return `<div class="message-bubble ${isOwn ? 'own' : 'other'}">
                    <img class="message-avatar" src="${avatar}">
                    <div class="message-content">
                        <div class="message-meta"><span>${senderName}</span><span>${time}</span></div>
                        ${mediaHtml}
                    </div>
                </div>`;
    } else if(data.type === 'location') {
        return `<div class="message-bubble ${isOwn ? 'own' : 'other'}">
                    <img class="message-avatar" src="${avatar}">
                    <div class="message-content">
                        <div class="message-meta"><span>${senderName}</span><span>${time}</span></div>
                        <a href="${data.locationUrl}" target="_blank" style="color:inherit;"><i class="fas fa-map-marker-alt"></i> 📍 عرض الموقع على الخريطة</a>
                    </div>
                </div>`;
    }
    return '';
}

window.playVoiceMessage = function(btn) {
    const wrapper = btn.closest('.voice-message-wrapper');
    const audioUrl = wrapper.dataset.url;
    const progress = wrapper.querySelector('.voice-waveform-progress');
    if(window.currentAudioPlayer && !window.currentAudioPlayer.paused) {
        window.currentAudioPlayer.pause();
        window.currentAudioPlayer = null;
    }
    let audio = new Audio(audioUrl);
    window.currentAudioPlayer = audio;
    audio.play();
    audio.ontimeupdate = () => {
        if(audio.duration) progress.style.width = (audio.currentTime / audio.duration * 100) + '%';
    };
    btn.innerHTML = '<i class="fas fa-pause"></i>';
    audio.onended = () => {
        btn.innerHTML = '<i class="fas fa-play"></i>';
        progress.style.width = '0%';
        window.currentAudioPlayer = null;
    };
    btn.onclick = () => {
        if(audio.paused) {
            audio.play();
            btn.innerHTML = '<i class="fas fa-pause"></i>';
        } else {
            audio.pause();
            btn.innerHTML = '<i class="fas fa-play"></i>';
        }
    };
};

async function clearAllMessages() {
    const isAdmin = window.currentUser.uid === "ADMIN_UID_HERE"; // ضع uid المدير
    if(!isAdmin && !confirm("أنت لست المدير! هل تريد مسح رسائلك فقط؟")) return;
    if(confirm("⚠️ تحذير: سيتم مسح جميع الرسائل بشكل نهائي. هل أنت متأكد؟")) {
        let snapshot = await db.collection(`chat_${window.currentChatRoom}`).get();
        let batch = db.batch();
        snapshot.forEach(doc => batch.delete(doc.ref));
        await batch.commit();
        window.showToast("تم مسح جميع الرسائل");
        document.getElementById('chatMessagesList').innerHTML = '<div class="empty-state"><i class="fas fa-comments"></i> تم مسح المحادثة</div>';
    }
}

// تصدير الدوال العامة
window.initChat = function() {
    initChatElements();
    document.getElementById('openChatBtn').onclick = () => {
        window.currentChatRoom = "general";
        document.getElementById('chatTitle').innerHTML = "المحادثة العامة";
        document.getElementById('mainApp').style.display = 'none';
        document.getElementById('chatPageScreen').style.display = 'flex';
        loadChatMessages();
    };
};
