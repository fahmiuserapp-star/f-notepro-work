// Chat Module
let chatUnsub = null;
let lastMsgCount = 0;
let currentChatRoom = "general";
let isMuted = false;
let mediaRecorder, audioChunks, isRecording = false, currentVoiceBlob = null, waveformAnim = null;

async function sendTextMessage() {
    let text = document.getElementById('chatTextInput').value.trim();
    if (!text) return;
    await db.collection(`chat_${currentChatRoom}`).add({
        text, senderId: currentUser.uid, senderName: currentDisplayName,
        senderAvatar: userAvatarUrl, type: 'text',
        timestamp: firebase.firestore.FieldValue.serverTimestamp()
    });
    document.getElementById('chatTextInput').value = '';
}

async function sendFileMessage(file) {
    let url = await uploadToCloudinary(file, 'chat');
    await db.collection(`chat_${currentChatRoom}`).add({
        mediaUrl: url, mediaType: file.type.startsWith('image/') ? 'image' : (file.type.startsWith('video/') ? 'video' : 'file'),
        fileName: file.name, senderId: currentUser.uid, senderName: currentDisplayName,
        senderAvatar: userAvatarUrl, type: 'media',
        timestamp: firebase.firestore.FieldValue.serverTimestamp()
    });
    showToast("تم إرسال الملف");
}

async function sendLocation() {
    navigator.geolocation.getCurrentPosition(async pos => {
        await db.collection(`chat_${currentChatRoom}`).add({
            location: { lat: pos.coords.latitude, lng: pos.coords.longitude },
            senderId: currentUser.uid, senderName: currentDisplayName,
            senderAvatar: userAvatarUrl, type: 'location',
            timestamp: firebase.firestore.FieldValue.serverTimestamp()
        });
        showToast("تم إرسال الموقع");
    });
}

async function startRecording() {
    if (isRecording) return;
    let stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    mediaRecorder = new MediaRecorder(stream);
    audioChunks = [];
    mediaRecorder.ondataavailable = e => audioChunks.push(e.data);
    mediaRecorder.onstop = async () => {
        if (audioChunks.length === 0) return;
        let blob = new Blob(audioChunks, { type: 'audio/webm' });
        currentVoiceBlob = blob;
        document.getElementById('recorderPanel').classList.add('show');
        startWaveform();
        stream.getTracks().forEach(t => t.stop());
    };
    mediaRecorder.start();
    isRecording = true;
    document.getElementById('walkieTalkieBtn').style.background = '#ef4444';
    let startTime = Date.now();
    let interval = setInterval(() => {
        let elapsed = Math.floor((Date.now() - startTime) / 1000);
        if (elapsed >= 30) { clearInterval(interval); stopRecording(); }
    }, 1000);
    window.recInterval = interval;
}

function stopRecording() {
    if (mediaRecorder && isRecording && mediaRecorder.state !== 'inactive') {
        mediaRecorder.stop();
        isRecording = false;
        if (window.recInterval) clearInterval(window.recInterval);
        document.getElementById('walkieTalkieBtn').style.background = '#25D366';
    }
}

function startWaveform() {
    let container = document.getElementById('recorderWaveform');
    container.innerHTML = '';
    for (let i = 0; i < 40; i++) {
        let bar = document.createElement('div');
        bar.className = 'recorder-bar';
        container.appendChild(bar);
    }
    if (waveformAnim) cancelAnimationFrame(waveformAnim);
    function animate() {
        document.querySelectorAll('.recorder-bar').forEach(bar => { bar.style.height = (8 + Math.random() * 40) + 'px'; });
        waveformAnim = requestAnimationFrame(animate);
    }
    animate();
    let start = Date.now();
    let timer = setInterval(() => {
        let elapsed = Math.floor((Date.now() - start) / 1000);
        document.getElementById('recorderTime').innerText = `${Math.floor(elapsed / 60)}:${(elapsed % 60).toString().padStart(2, '0')}`;
        if (elapsed >= 30) { clearInterval(timer); document.getElementById('recorderSendBtn').click(); }
    }, 1000);
    window.recTimer = timer;
}

async function sendRecordedVoice() {
    if (currentVoiceBlob) {
        let url = await uploadToCloudinary(currentVoiceBlob, 'voice');
        await db.collection(`chat_${currentChatRoom}`).add({
            audioUrl: url, duration: 30, senderId: currentUser.uid,
            senderName: currentDisplayName, senderAvatar: userAvatarUrl,
            type: 'voice', timestamp: firebase.firestore.FieldValue.serverTimestamp()
        });
        showToast("تم إرسال الرسالة الصوتية");
        playBeep(660);
    }
    closeRecorder();
}

function closeRecorder() {
    document.getElementById('recorderPanel').classList.remove('show');
    if (waveformAnim) cancelAnimationFrame(waveformAnim);
    if (window.recTimer) clearInterval(window.recTimer);
    currentVoiceBlob = null;
}

function loadChatMessages() {
    if (!currentUser) return;
    let container = document.getElementById('chatMessagesList');
    if (chatUnsub) chatUnsub();
    chatUnsub = db.collection(`chat_${currentChatRoom}`).orderBy('timestamp', 'asc').onSnapshot(snapshot => {
        if (snapshot.empty) {
            container.innerHTML = '<div class="empty-state">لا توجد رسائل</div>';
            return;
        }
        let html = '';
        snapshot.forEach(doc => {
            let data = doc.data();
            let isOwn = data.senderId === currentUser.uid;
            let time = data.timestamp ? data.timestamp.toDate().toLocaleTimeString() : '';
            let avatar = data.senderAvatar || '';
            if (data.type === 'text') {
                html += `<div class="message-bubble ${isOwn ? 'own' : 'other'}">
                            <img class="message-avatar" src="${avatar}" onerror="this.src='data:image/svg+xml,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20fill%3D%22%23e94560%22%3E%3Ccircle%20cx%3D%2217%22%20cy%3D%2217%22%20r%3D%2217%22%20fill%3D%22%23e94560%22%2F%3E%3Ctext%20x%3D%2217%22%20y%3D%2222%22%20text-anchor%3D%22middle%22%20fill%3D%22white%22%20font-size%3D%2216%22%3E${data.senderName?.charAt(0) || '?'}%3C%2Ftext%3E%3C%2Fsvg%3E'">
                            <div class="message-content">
                                <div class="message-text">${escapeHtml(data.text)}</div>
                                <div class="message-meta"><span>${escapeHtml(data.senderName)}</span><span>${time}</span></div>
                            </div>
                        </div>`;
            } else if (data.type === 'voice') {
                html += `<div class="message-bubble ${isOwn ? 'own' : 'other'}">
                            <img class="message-avatar" src="${avatar}">
                            <div class="message-content">
                                <div class="message-meta"><span>${escapeHtml(data.senderName)}</span><span>${time}</span></div>
                                <div class="voice-wrapper">
                                    <button class="voice-play" onclick="playVoice('${data.audioUrl}', this)"><i class="fas fa-play"></i></button>
                                    <div class="voice-waveform"><div class="voice-progress" style="width:0%"></div><div class="voice-bars">${Array(25).fill().map(() => '<span style="height:' + (8 + Math.random() * 20) + 'px"></span>').join('')}</div></div>
                                    <span class="voice-duration">0:30</span>
                                    ${isOwn ? `<button class="voice-delete" onclick="deleteMessage('${doc.id}')" style="background:none;border:none;cursor:pointer;"><i class="fas fa-trash-alt"></i></button>` : ''}
                                </div>
                            </div>
                        </div>`;
            } else if (data.type === 'media') {
                let mediaHtml = data.mediaType === 'image' ? `<img class="message-image" src="${data.mediaUrl}" onclick="window.showFullImage('${data.mediaUrl}')">` : (data.mediaType === 'video' ? `<video controls class="message-image" src="${data.mediaUrl}"></video>` : `<div class="message-file"><i class="fas fa-file"></i> <a href="${data.mediaUrl}" download="${data.fileName}" style="color:inherit;">${escapeHtml(data.fileName)}</a></div>`);
                html += `<div class="message-bubble ${isOwn ? 'own' : 'other'}"><img class="message-avatar" src="${avatar}"><div class="message-content"><div class="message-meta"><span>${escapeHtml(data.senderName)}</span><span>${time}</span></div>${mediaHtml}</div></div>`;
            } else if (data.type === 'location') {
                html += `<div class="message-bubble ${isOwn ? 'own' : 'other'}"><img class="message-avatar" src="${avatar}"><div class="message-content"><div class="message-meta"><span>${escapeHtml(data.senderName)}</span><span>${time}</span></div><a href="https://www.google.com/maps?q=${data.location.lat},${data.location.lng}" target="_blank"><i class="fas fa-map-marker-alt"></i> عرض الموقع</a></div></div>`;
            }
        });
        container.innerHTML = html;
        if (snapshot.size > lastMsgCount && lastMsgCount !== 0 && !isMuted) {
            playBeep(800);
            showToast("🔊 رسالة جديدة");
        }
        lastMsgCount = snapshot.size;
        container.scrollTop = container.scrollHeight;
    });
}

window.playVoice = (url, btn) => {
    let audio = new Audio(url);
    let wrapper = btn.closest('.voice-wrapper');
    let progress = wrapper.querySelector('.voice-progress');
    audio.play();
    audio.ontimeupdate = () => { progress.style.width = (audio.currentTime / 30 * 100) + '%'; };
    btn.innerHTML = '<i class="fas fa-pause"></i>';
    audio.onended = () => { btn.innerHTML = '<i class="fas fa-play"></i>'; progress.style.width = '0%'; };
};

window.deleteMessage = async (id) => { if (confirm("حذف الرسالة؟")) await db.collection(`chat_${currentChatRoom}`).doc(id).delete(); };

async function exportChatToPDF() {
    let snap = await db.collection(`chat_${currentChatRoom}`).orderBy('timestamp', 'asc').get();
    let el = document.createElement('div');
    el.dir = 'rtl';
    el.innerHTML = '<h2>تقرير المحادثة</h2><hr>';
    snap.forEach(d => {
        let data = d.data();
        let time = data.timestamp ? data.timestamp.toDate().toLocaleString() : '';
        if (data.type === 'text') el.innerHTML += `<div><strong>${escapeHtml(data.senderName)}</strong> (${time}):<br>${escapeHtml(data.text)}</div><hr>`;
        else if (data.type === 'voice') el.innerHTML += `<div><strong>${escapeHtml(data.senderName)}</strong> (${time}): [رسالة صوتية]</div><hr>`;
    });
    html2pdf().from(el).set({ margin: 1, filename: `chat_${Date.now()}.pdf` }).save();
}

async function clearChat() {
    if (confirm("مسح جميع الرسائل نهائياً؟")) {
        let msgs = await db.collection(`chat_${currentChatRoom}`).get();
        msgs.forEach(async d => await d.ref.delete());
        showToast("تم مسح المحادثة");
    }
}

// Event listeners for chat
document.getElementById('sendMessageBtn').onclick = sendTextMessage;
document.getElementById('attachFileBtn').onclick = () => {
    let inp = document.createElement('input');
    inp.type = 'file';
    inp.accept = 'image/*,video/*,application/pdf';
    inp.onchange = async e => { if (e.target.files[0]) await sendFileMessage(e.target.files[0]); };
    inp.click();
};
document.getElementById('sendLocationBtn').onclick = sendLocation;
document.getElementById('walkieTalkieBtn').onclick = () => { if (!isRecording) startRecording(); else stopRecording(); };
document.getElementById('recorderCancelBtn').onclick = closeRecorder;
document.getElementById('recorderSendBtn').onclick = sendRecordedVoice;
document.getElementById('chatSettingsBtn').onclick = () => document.getElementById('chatSettingsPanel').classList.toggle('show');
document.querySelectorAll('.bg-option').forEach(opt => {
    opt.onclick = () => {
        document.getElementById('chatMessagesList').style.background = opt.dataset.bg;
        document.getElementById('chatSettingsPanel').classList.remove('show');
    };
});
document.getElementById('openChatBtn').onclick = () => {
    document.getElementById('mainApp').style.display = 'none';
    document.getElementById('chatPageScreen').style.display = 'flex';
    loadChatMessages();
};
document.getElementById('chatBackBtn').onclick = () => {
    document.getElementById('chatPageScreen').style.display = 'none';
    document.getElementById('mainApp').style.display = 'flex';
};