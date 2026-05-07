// ========== الدردشة المتطورة ==========

let chatMessagesListener = null;
let recordingMediaRecorder = null;
let audioChunks = [];
let isRecording = false;
let recordingStartTime = 0;
let recordingInterval = null;
let currentAudio = null;

// تهيئة الدردشة
function initChat() {
    const attachBtn = document.getElementById('attachFileBtn');
    const locationBtn = document.getElementById('sendLocationBtn');
    const walkieBtn = document.getElementById('walkieTalkieBtn');
    const sendBtn = document.getElementById('sendMessageBtn');
    const chatInput = document.getElementById('chatTextInput');
    const backBtn = document.getElementById('chatBackBtn');
    
    if(attachBtn) attachBtn.onclick = () => selectAndSendFile();
    if(locationBtn) locationBtn.onclick = () => requestAndSendLocation();
    if(walkieBtn) walkieBtn.onclick = () => toggleRecording();
    if(sendBtn) sendBtn.onclick = () => sendTextMessage();
    if(chatInput) chatInput.addEventListener('keypress', (e) => { if(e.key === 'Enter') sendTextMessage(); });
    if(backBtn) backBtn.onclick = () => {
        document.getElementById('chatPageScreen').style.display = 'none';
        document.getElementById('mainApp').style.display = 'flex';
        if(chatMessagesListener) chatMessagesListener();
    };
    
    document.getElementById('openChatBtn').onclick = () => {
        window.currentChatRoom = "general";
        document.getElementById('chatTitle').innerHTML = "المحادثة العامة 💬";
        document.getElementById('mainApp').style.display = 'none';
        document.getElementById('chatPageScreen').style.display = 'flex';
        loadChatMessages();
    };
}

// إرسال رسالة نصية
async function sendTextMessage() {
    const input = document.getElementById('chatTextInput');
    const text = input.value.trim();
    if(!text) return;
    
    try {
        await db.collection(`chat_${window.currentChatRoom}`).add({
            type: 'text',
            text: text,
            senderId: window.currentUser.uid,
            senderName: window.currentDisplayName,
            senderAvatar: window.userAvatarUrl,
            timestamp: firebase.firestore.FieldValue.serverTimestamp(),
            read: false
        });
        input.value = '';
    } catch(e) {
        window.showToast("فشل الإرسال", true);
    }
}

// اختيار وإرسال ملف
async function selectAndSendFile() {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*,video/*,application/pdf,application/msword,text/plain';
    input.onchange = async (e) => {
        const file = e.target.files[0];
        if(!file) return;
        
        window.showToast("جاري رفع الملف... 📤");
        try {
            const url = await window.uploadToCloudinary(file, 'chat');
            let mediaType = 'file';
            if(file.type.startsWith('image/')) mediaType = 'image';
            else if(file.type.startsWith('video/')) mediaType = 'video';
            
            await db.collection(`chat_${window.currentChatRoom}`).add({
                type: 'media',
                mediaType: mediaType,
                mediaUrl: url,
                fileName: file.name,
                fileSize: file.size,
                senderId: window.currentUser.uid,
                senderName: window.currentDisplayName,
                senderAvatar: window.userAvatarUrl,
                timestamp: firebase.firestore.FieldValue.serverTimestamp()
            });
            window.showToast("✓ تم إرسال الملف");
        } catch(e) {
            window.showToast("فشل رفع الملف", true);
        }
    };
    input.click();
}

// طلب الموقع وإرساله
function requestAndSendLocation() {
    if(!navigator.geolocation) {
        window.showToast("الموقع غير مدعوم", true);
        return;
    }
    
    window.showToast("جاري الحصول على الموقع... 📍");
    navigator.geolocation.getCurrentPosition(async (position) => {
        const { latitude, longitude } = position.coords;
        const mapsUrl = `https://www.google.com/maps?q=${latitude},${longitude}`;
        
        try {
            await db.collection(`chat_${window.currentChatRoom}`).add({
                type: 'location',
                locationUrl: mapsUrl,
                lat: latitude,
                lng: longitude,
                senderId: window.currentUser.uid,
                senderName: window.currentDisplayName,
                senderAvatar: window.userAvatarUrl,
                timestamp: firebase.firestore.FieldValue.serverTimestamp()
            });
            window.showToast("✓ تم إرسال الموقع");
        } catch(e) {
            window.showToast("فشل إرسال الموقع", true);
        }
    }, () => {
        window.showToast("تعذر الحصول على الموقع", true);
    });
}

// التسجيل الصوتي
async function toggleRecording() {
    if(isRecording) {
        stopRecording();
        return;
    }
    
    try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
        recordingMediaRecorder = new MediaRecorder(stream);
        audioChunks = [];
        
        recordingMediaRecorder.ondataavailable = (e) => audioChunks.push(e.data);
        recordingMediaRecorder.onstop = async () => {
            if(audioChunks.length === 0) return;
            const blob = new Blob(audioChunks, { type: 'audio/webm' });
            showRecordingPreview(blob);
            stream.getTracks().forEach(t => t.stop());
            isRecording = false;
            document.getElementById('walkieTalkieBtn').style.background = '';
            if(recordingInterval) clearInterval(recordingInterval);
        };
        
        recordingMediaRecorder.start();
        isRecording = true;
        recordingStartTime = Date.now();
        document.getElementById('walkieTalkieBtn').style.background = '#ef4444';
        
        // عداد التسجيل
        recordingInterval = setInterval(() => {
            const elapsed = Math.floor((Date.now() - recordingStartTime) / 1000);
            if(elapsed >= 60) {
                stopRecording();
                window.showToast("الحد الأقصى 60 ثانية");
            }
        }, 1000);
    } catch(e) {
        window.showToast("الرجاء السماح باستخدام الميكروفون", true);
        document.getElementById('permissionPrompt').classList.add('show');
    }
}

function stopRecording() {
    if(recordingMediaRecorder && recordingMediaRecorder.state !== 'inactive') {
        recordingMediaRecorder.stop();
    }
}

function showRecordingPreview(blob) {
    const panel = document.getElementById('recorderPanel');
    const waveform = document.getElementById('recorderWaveform');
    const timeSpan = document.getElementById('recorderTime');
    
    // إنشاء شكل الموجة
    waveform.innerHTML = '';
    for(let i = 0; i < 40; i++) {
        const bar = document.createElement('div');
        bar.style.width = '3px';
        bar.style.height = Math.random() * 30 + 10 + 'px';
        bar.style.background = 'var(--accent)';
        bar.style.borderRadius = '2px';
        waveform.appendChild(bar);
    }
    
    // تشغيل معاينة
    const audio = new Audio(URL.createObjectURL(blob));
    audio.onloadedmetadata = () => {
        const duration = Math.floor(audio.duration);
        timeSpan.innerText = `${Math.floor(duration/60)}:${(duration%60).toString().padStart(2,'0')}`;
    };
    
    panel.classList.add('show');
    
    document.getElementById('recorderSendBtn').onclick = async () => {
        panel.classList.remove('show');
        window.showToast("جاري رفع الرسالة الصوتية...");
        try {
            const url = await window.uploadAudioToCloudinary(blob);
            const duration = Math.floor(audio.duration || 0);
            await db.collection(`chat_${window.currentChatRoom}`).add({
                type: 'voice',
                audioUrl: url,
                duration: duration,
                senderId: window.currentUser.uid,
                senderName: window.currentDisplayName,
                senderAvatar: window.userAvatarUrl,
                timestamp: firebase.firestore.FieldServerTimestamp()
            });
            window.showToast("✓ تم إرسال الرسالة الصوتية");
        } catch(e) {
            window.showToast("فشل رفع الصوت", true);
        }
    };
    
    document.getElementById('recorderCancelBtn').onclick = () => {
        panel.classList.remove('show');
    };
}

// تحميل وعرض الرسائل
function loadChatMessages() {
    const container = document.getElementById('chatMessagesList');
    if(!container) return;
    
    if(chatMessagesListener) chatMessagesListener();
    
    chatMessagesListener = db.collection(`chat_${window.currentChatRoom}`)
        .orderBy('timestamp', 'asc')
        .onSnapshot(snapshot => {
            let lastDate = null;
            let html = '';
            
            snapshot.forEach(doc => {
                const msg = doc.data();
                const msgDate = msg.timestamp?.toDate();
                const dateStr = msgDate ? msgDate.toLocaleDateString('ar-EG', { weekday: 'short', month: 'short', day: 'numeric' }) : '';
                
                // إضافة فاصل زمني
                if(dateStr && dateStr !== lastDate) {
                    html += `<div style="text-align:center;margin:10px 0;"><span style="background:var(--glass-bg);padding:4px 12px;border-radius:20px;font-size:11px;">${dateStr}</span></div>`;
                    lastDate = dateStr;
                }
                
                const isOwn = msg.senderId === window.currentUser.uid;
                const time = msgDate ? msgDate.toLocaleTimeString('ar-EG', { hour: '2-digit', minute: '2-digit' }) : '';
                
                html += `<div class="message-wrapper ${isOwn ? 'own' : ''}">
                    <div class="message-bubble ${isOwn ? 'own' : 'other'}">
                        ${!isOwn ? `<div class="message-sender">${window.escapeHtml(msg.senderName)}</div>` : ''}
                        ${renderMessageContent(msg, isOwn)}
                        <div class="message-meta">${time}</div>
                    </div>
                </div>`;
            });
            
            if(snapshot.empty) {
                container.innerHTML = '<div class="empty-state">✨ أرسل أول رسالة</div>';
            } else {
                container.innerHTML = html;
                container.scrollTop = container.scrollHeight;
            }
        }, error => {
            console.error("Chat error:", error);
            container.innerHTML = '<div class="empty-state">⚠️ خطأ في تحميل المحادثة</div>';
        });
}

function renderMessageContent(msg, isOwn) {
    switch(msg.type) {
        case 'text':
            return `<div class="message-text">${window.escapeHtml(msg.text)}</div>`;
            
        case 'media':
            if(msg.mediaType === 'image') {
                return `<img class="message-image" src="${msg.mediaUrl}" onclick="window.showFullImage('${msg.mediaUrl}')">`;
            } else if(msg.mediaType === 'video') {
                return `<video class="chat-video" controls src="${msg.mediaUrl}"></video>`;
            } else {
                // PDF والملفات الأخرى - تحميل مباشر
                return `<a href="${msg.mediaUrl}" download="${msg.fileName}" class="message-file" style="text-decoration:none;color:inherit;">
                    <i class="fas fa-file-pdf"></i> ${msg.fileName}
                    <i class="fas fa-download" style="margin-right:8px;"></i>
                </a>`;
            }
            
        case 'voice':
            return `<div class="voice-message">
                <button class="voice-play-btn" onclick="window.playVoice(this, '${msg.audioUrl}')">
                    <i class="fas fa-play"></i>
                </button>
                <div class="voice-waveform" id="waveform-${msg.id}">
                    ${Array(20).fill().map(() => `<div class="voice-waveform-bar"></div>`).join('')}
                </div>
                <span class="voice-duration">${Math.floor(msg.duration/60)}:${(msg.duration%60).toString().padStart(2,'0')}</span>
            </div>`;
            
        case 'location':
            return `<a href="${msg.locationUrl}" target="_blank" style="color:inherit;text-decoration:none;">
                <i class="fas fa-map-marker-alt"></i> 📍 عرض الموقع على الخريطة
            </a>`;
            
        default:
            return `<div class="message-text">رسالة غير معروفة</div>`;
    }
}

// تشغيل الرسالة الصوتية
window.playVoice = function(btn, url) {
    if(currentAudio && !currentAudio.paused) {
        currentAudio.pause();
        currentAudio = null;
        btn.innerHTML = '<i class="fas fa-play"></i>';
    }
    
    currentAudio = new Audio(url);
    currentAudio.play();
    btn.innerHTML = '<i class="fas fa-pause"></i>';
    
    currentAudio.onended = () => {
        btn.innerHTML = '<i class="fas fa-play"></i>';
        currentAudio = null;
    };
};

// تصدير PDF للمحادثة
document.getElementById('exportChatMenuItem')?.addEventListener('click', async () => {
    window.showToast("جاري تجهيز PDF...");
    const snapshot = await db.collection(`chat_${window.currentChatRoom}`).orderBy('timestamp', 'asc').get();
    let html = '<div dir="rtl"><h2 style="text-align:center;">📄 محادثة F-NOTE PRO</h2><hr>';
    snapshot.forEach(doc => {
        const msg = doc.data();
        const time = msg.timestamp?.toDate().toLocaleString('ar-EG') || '';
        html += `<div style="margin:10px 0;padding:8px;border-bottom:1px solid #ccc;">
            <strong>${window.escapeHtml(msg.senderName)}</strong> <small>${time}</small><br>
            ${msg.type === 'text' ? window.escapeHtml(msg.text) : '[' + msg.type + ']'}
        </div>`;
    });
    html += '</div>';
    html2pdf().from(html).set({ margin: 1, filename: `chat_${Date.now()}.pdf` }).save();
    window.showToast("✓ تم تصدير PDF");
});

// تشغيل الدردشة
initChat();
