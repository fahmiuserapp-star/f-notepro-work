// ==================== chat.js ====================
// يعتمد على وجود Firebase و currentUser وغيرها في النطاق العام
// إذا لم تكن موجودة، يتم تعريف دوال افتراضية

(function() {
    // التأكد من وجود التوابع الأساسية
    if (typeof showToast !== 'function') {
        window.showToast = function(msg, isErr) {
            alert(msg);
        };
    }
    if (typeof escapeHtml !== 'function') {
        window.escapeHtml = function(s) {
            if (!s) return '';
            return s.replace(/[&<>]/g, function(m) {
                if (m === '&') return '&amp;';
                if (m === '<') return '&lt;';
                if (m === '>') return '&gt;';
                return m;
            });
        };
    }
    if (typeof playNotificationBeep !== 'function') {
        window.playNotificationBeep = function() {};
    }
    if (typeof uploadToCloudinary !== 'function') {
        window.uploadToCloudinary = async function(file, folder) {
            // عليك تعريف هذه الدالة في الـ HTML الرئيسي
            console.warn("uploadToCloudinary غير معرفة");
            return "#";
        };
    }
    if (typeof uploadAudioToCloudinary !== 'function') {
        window.uploadAudioToCloudinary = async function(blob) {
            console.warn("uploadAudioToCloudinary غير معرفة");
            return "#";
        };
    }

    // متغيرات خاصة بالدردشة
    let currentChatRoom = "general";       // اسم الغرفة الحالية (عامة أو خاصة)
    let chatUnsub = null;                  // لإلغاء الاستماع للرسائل
    let lastMsgCount = 0;
    let chatLastDoc = null;                // آخر وثيقة للـ pagination
    let chatLoadingMore = false;
    let chatHasMore = true;
    let typingTimeout = null;
    let mediaRecorder = null;
    let audioChunks = [];
    let isRecording = false;
    let currentVoiceBlob = null;
    let waveformAnimation = null;
    let recordingInterval = null;
    let recorderTimer = null;

    // عناصر DOM
    let chatPageScreen, mainApp, chatMessagesList, chatLoadMoreBtn, chatTextInput, sendMessageBtn, attachFileBtn, sendLocationBtn, walkieTalkieBtn, chatBackBtn, chatTitle, chatSettingsBtn;

    // تهيئة المراجع بعد تحميل الصفحة
    function initElements() {
        chatPageScreen = document.getElementById('chatPageScreen');
        mainApp = document.getElementById('mainApp');
        chatMessagesList = document.getElementById('chatMessagesList');
        chatLoadMoreBtn = document.getElementById('chatLoadMoreBtn');
        chatTextInput = document.getElementById('chatTextInput');
        sendMessageBtn = document.getElementById('sendMessageBtn');
        attachFileBtn = document.getElementById('attachFileBtn');
        sendLocationBtn = document.getElementById('sendLocationBtn');
        walkieTalkieBtn = document.getElementById('walkieTalkieBtn');
        chatBackBtn = document.getElementById('chatBackBtn');
        chatTitle = document.getElementById('chatTitle');
        chatSettingsBtn = document.getElementById('chatSettingsBtn');
    }

    // ========== دوال الصوت المتقدم ==========
    async function startVoiceRecording() {
        if (isRecording) return;
        try {
            const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
            mediaRecorder = new MediaRecorder(stream);
            audioChunks = [];
            mediaRecorder.ondataavailable = e => audioChunks.push(e.data);
            mediaRecorder.onstop = async () => {
                if (audioChunks.length === 0) return;
                const blob = new Blob(audioChunks, { type: 'audio/webm' });
                currentVoiceBlob = blob;
                // إظهار لوحة المعاينة
                const recorderPanel = document.getElementById('recorderPanel');
                if (recorderPanel) recorderPanel.classList.add('show');
                startWaveformAnimation();
                stream.getTracks().forEach(t => t.stop());
            };
            mediaRecorder.start();
            isRecording = true;
            if (walkieTalkieBtn) walkieTalkieBtn.style.background = '#ef4444';
            // حد أقصى 120 ثانية (دقيقتين)
            const startTime = Date.now();
            recordingInterval = setInterval(() => {
                const elapsed = Math.floor((Date.now() - startTime) / 1000);
                if (elapsed >= 120) {
                    clearInterval(recordingInterval);
                    stopVoiceRecording();
                    showToast("تم الوصول للحد الأقصى (دقيقتين)", true);
                }
            }, 1000);
        } catch (e) {
            showToast("الوصول للميكروفون مرفوض", true);
        }
    }

    function stopVoiceRecording() {
        if (mediaRecorder && isRecording && mediaRecorder.state !== 'inactive') {
            mediaRecorder.stop();
            isRecording = false;
            if (recordingInterval) clearInterval(recordingInterval);
            if (walkieTalkieBtn) walkieTalkieBtn.style.background = '#25D366';
        }
    }

    function startWaveformAnimation() {
        const container = document.getElementById('recorderWaveform');
        if (!container) return;
        container.innerHTML = '';
        for (let i = 0; i < 40; i++) {
            const bar = document.createElement('div');
            bar.className = 'recorder-bar';
            container.appendChild(bar);
        }
        if (waveformAnimation) cancelAnimationFrame(waveformAnimation);
        function animate() {
            const bars = document.querySelectorAll('.recorder-bar');
            bars.forEach(bar => {
                bar.style.height = (8 + Math.random() * 40) + 'px';
            });
            waveformAnimation = requestAnimationFrame(animate);
        }
        animate();
        const startTime = Date.now();
        recorderTimer = setInterval(() => {
            const elapsed = Math.floor((Date.now() - startTime) / 1000);
            const timeEl = document.getElementById('recorderTime');
            if (timeEl) timeEl.innerText = `${Math.floor(elapsed/60)}:${(elapsed%60).toString().padStart(2,'0')}`;
            if (elapsed >= 120) {
                clearInterval(recorderTimer);
                const sendBtn = document.getElementById('recorderSendBtn');
                if (sendBtn) sendBtn.click();
            }
        }, 1000);
    }

    async function sendRecordedVoice() {
        if (currentVoiceBlob) {
            const url = await uploadAudioToCloudinary(currentVoiceBlob);
            if (!url) {
                showToast("فشل رفع الصوت", true);
                return;
            }
            await db.collection(`chat_${currentChatRoom}`).add({
                audioUrl: url,
                duration: 120,
                senderId: currentUser.uid,
                senderName: currentDisplayName,
                senderAvatar: userAvatarUrl,
                type: 'voice',
                timestamp: firebase.firestore.FieldValue.serverTimestamp()
            });
            showToast("تم إرسال الرسالة الصوتية");
            playNotificationBeep(660);
        }
        closeRecorderPanel();
    }

    function closeRecorderPanel() {
        const panel = document.getElementById('recorderPanel');
        if (panel) panel.classList.remove('show');
        if (waveformAnimation) cancelAnimationFrame(waveformAnimation);
        if (recorderTimer) clearInterval(recorderTimer);
        currentVoiceBlob = null;
    }

    // ========== دوال إرسال الرسائل ==========
    async function sendTextMessage() {
        const text = chatTextInput ? chatTextInput.value.trim() : '';
        if (!text) return;
        // إزالة مؤشر الكتابة
        if (currentUser && currentChatRoom) {
            rtdb.ref(`/typing/${currentChatRoom}/${currentUser.uid}`).remove();
        }
        await db.collection(`chat_${currentChatRoom}`).add({
            text: text,
            senderId: currentUser.uid,
            senderName: currentDisplayName,
            senderAvatar: userAvatarUrl,
            type: 'text',
            timestamp: firebase.firestore.FieldValue.serverTimestamp()
        });
        if (chatTextInput) chatTextInput.value = '';
    }

    async function sendFileMessage(file) {
        try {
            const url = await uploadToCloudinary(file, 'chat');
            if (!url) throw new Error("رفع فاشل");
            await db.collection(`chat_${currentChatRoom}`).add({
                mediaUrl: url,
                mediaType: file.type.startsWith('image/') ? 'image' : (file.type.startsWith('video/') ? 'video' : 'file'),
                fileName: file.name,
                senderId: currentUser.uid,
                senderName: currentDisplayName,
                senderAvatar: userAvatarUrl,
                type: 'media',
                timestamp: firebase.firestore.FieldValue.serverTimestamp()
            });
            showToast("تم إرسال الملف");
        } catch (e) {
            showToast("فشل الرفع: " + e.message, true);
        }
    }

    async function sendLocation() {
        if (!navigator.geolocation) {
            showToast("الموقع غير مدعوم في هذا المتصفح", true);
            return;
        }
        navigator.geolocation.getCurrentPosition(async (pos) => {
            const mapsUrl = `https://www.google.com/maps?q=${pos.coords.latitude},${pos.coords.longitude}`;
            await db.collection(`chat_${currentChatRoom}`).add({
                locationUrl: mapsUrl,
                lat: pos.coords.latitude,
                lng: pos.coords.longitude,
                senderId: currentUser.uid,
                senderName: currentDisplayName,
                senderAvatar: userAvatarUrl,
                type: 'location',
                timestamp: firebase.firestore.FieldValue.serverTimestamp()
            });
            showToast("📍 تم إرسال الموقع");
        }, () => showToast("تعذر الحصول على الموقع", true));
    }

    // ========== عرض الرسائل مع Pagination ==========
    function loadChatMessages() {
        if (!currentUser || !currentChatRoom) return;
        if (chatUnsub) chatUnsub();
        // إعادة تعيين حالة التحميل
        chatLastDoc = null;
        chatHasMore = true;
        chatLoadingMore = false;

        // دالة تحميل الدفعات (oldest first)
        const loadMore = async (append = false) => {
            if (chatLoadingMore || !chatHasMore) return;
            chatLoadingMore = true;
            let query = db.collection(`chat_${currentChatRoom}`)
                          .orderBy('timestamp', 'desc')
                          .limit(20);
            if (chatLastDoc) query = query.startAfter(chatLastDoc);
            
            try {
                const snapshot = await query.get();
                if (snapshot.empty) {
                    chatHasMore = false;
                    if (chatLoadMoreBtn) chatLoadMoreBtn.style.display = 'none';
                } else {
                    chatLastDoc = snapshot.docs[snapshot.docs.length - 1];
                    const messages = [];
                    snapshot.forEach(doc => messages.unshift({ id: doc.id, ...doc.data() }));
                    
                    let html = '';
                    for (const data of messages) {
                        html += renderMessageHtml(data);
                    }
                    
                    if (append) {
                        if (chatMessagesList) {
                            chatMessagesList.innerHTML = html + chatMessagesList.innerHTML;
                        }
                    } else {
                        if (chatMessagesList) chatMessagesList.innerHTML = html;
                    }
                    
                    if (snapshot.docs.length < 20) chatHasMore = false;
                    if (chatLoadMoreBtn) {
                        chatLoadMoreBtn.style.display = chatHasMore ? 'block' : 'none';
                        if (chatHasMore) {
                            chatLoadMoreBtn.onclick = () => loadMore(true);
                        }
                    }
                    // التمرير إلى الأسفل عند التحميل الأول أو عند إضافة رسائل جديدة
                    if (!append && chatMessagesList) chatMessagesList.scrollTop = chatMessagesList.scrollHeight;
                }
            } catch (err) {
                console.error(err);
                showToast("خطأ في تحميل الرسائل", true);
            } finally {
                chatLoadingMore = false;
            }
        };

        // الاستماع للرسائل الجديدة فقط (آخر رسالة)
        chatUnsub = db.collection(`chat_${currentChatRoom}`)
                     .orderBy('timestamp', 'desc')
                     .limit(1)
                     .onSnapshot(snapshot => {
                         if (snapshot.empty) {
                             if (chatMessagesList) chatMessagesList.innerHTML = '<div class="empty-state">لا توجد رسائل</div>';
                             return;
                         }
                         const newDoc = snapshot.docs[0];
                         if (chatLastDoc && newDoc.id !== chatLastDoc.id) {
                             // توجد رسالة جديدة نضيفها أعلى القائمة
                             const newData = newDoc.data();
                             const newHtml = renderMessageHtml(newData);
                             if (chatMessagesList) {
                                 chatMessagesList.innerHTML = newHtml + chatMessagesList.innerHTML;
                             }
                             if (!isSoundMuted && window.playNotificationBeep) {
                                 playNotificationBeep(800);
                                 showToast("🔊 رسالة جديدة");
                             }
                         } else if (!chatLastDoc) {
                             // أول تحميل
                             loadMore();
                         }
                     }, err => {
                         console.error(err);
                         showToast("خطأ في الاستماع للرسائل", true);
                     });

        // تحميل أول دفعة
        loadMore();

        // إعداد مؤشر الكتابة
        setupTypingIndicator();
    }

    // دالة مساعدة لإنشاء HTML للرسالة حسب نوعها
    function renderMessageHtml(data) {
        const isOwn = data.senderId === currentUser.uid;
        const time = data.timestamp ? data.timestamp.toDate().toLocaleTimeString() : '';
        const avatar = data.senderAvatar || '';
        const senderName = escapeHtml(data.senderName || 'مجهول');
        const bubbleClass = isOwn ? 'own' : 'other';
        
        if (data.type === 'text') {
            return `<div class="message-bubble ${bubbleClass}">
                        <img class="message-avatar" src="${avatar}" onerror="this.src='data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='%23e94560'%3E%3Ccircle cx='16' cy='16' r='16' fill='%23e94560'/%3E%3Ctext x='16' y='22' text-anchor='middle' fill='white' font-size='16'%3E${senderName.charAt(0)}%3C/text%3E%3C/svg%3E'">
                        <div class="message-content">
                            <div class="message-text">${escapeHtml(data.text)}</div>
                            <div class="message-meta"><span>${senderName}</span><span>${time}</span></div>
                        </div>
                    </div>`;
        } 
        else if (data.type === 'voice') {
            return `<div class="message-bubble ${bubbleClass}">
                        <img class="message-avatar" src="${avatar}" onerror="this.src='data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='%23e94560'%3E%3Ccircle cx='16' cy='16' r='16' fill='%23e94560'/%3E%3Ctext x='16' y='22' text-anchor='middle' fill='white' font-size='16'%3E${senderName.charAt(0)}%3C/text%3E%3C/svg%3E'">
                        <div class="message-content">
                            <div class="message-meta"><span>${senderName}</span><span>${time}</span></div>
                            <div class="voice-message-wrapper">
                                <button class="voice-play-btn" onclick="window.playVoiceMessage('${data.audioUrl}', this)"><i class="fas fa-play"></i></button>
                                <div class="voice-waveform-container">
                                    <div class="voice-waveform-progress" style="width:0%"></div>
                                    <div class="voice-waveform-bars">${Array(20).fill().map(() => '<span style="height:' + (8 + Math.random() * 20) + 'px"></span>').join('')}</div>
                                </div>
                                <span class="voice-duration">${Math.floor(data.duration/60)}:${(data.duration%60).toString().padStart(2,'0')}</span>
                            </div>
                        </div>
                    </div>`;
        }
        else if (data.type === 'media') {
            let mediaHtml = '';
            if (data.mediaType === 'image') {
                mediaHtml = `<img class="message-image" src="${data.mediaUrl}" onclick="window.showFullImage('${data.mediaUrl}')">`;
            } else if (data.mediaType === 'video') {
                mediaHtml = `<video controls class="message-image" src="${data.mediaUrl}"></video>`;
            } else {
                mediaHtml = `<div class="message-file"><i class="fas fa-file"></i> <a href="${data.mediaUrl}" download="${escapeHtml(data.fileName)}">${escapeHtml(data.fileName)}</a></div>`;
            }
            return `<div class="message-bubble ${bubbleClass}">
                        <img class="message-avatar" src="${avatar}">
                        <div class="message-content">
                            <div class="message-meta"><span>${senderName}</span><span>${time}</span></div>
                            ${mediaHtml}
                        </div>
                    </div>`;
        }
        else if (data.type === 'location') {
            return `<div class="message-bubble ${bubbleClass}">
                        <img class="message-avatar" src="${avatar}">
                        <div class="message-content">
                            <div class="message-meta"><span>${senderName}</span><span>${time}</span></div>
                            <a href="${data.locationUrl}" target="_blank" style="color:inherit;"><i class="fas fa-map-marker-alt"></i> عرض الموقع على الخريطة</a>
                        </div>
                    </div>`;
        }
        return '';
    }

    // إعداد مؤشر الكتابة
    function setupTypingIndicator() {
        if (!currentChatRoom || !rtdb) return;
        const typingRef = rtdb.ref(`/typing/${currentChatRoom}`);
        typingRef.off();
        typingRef.on('value', (snap) => {
            const typingUsers = snap.val() || {};
            const typingList = Object.keys(typingUsers)
                .filter(uid => uid !== currentUser.uid)
                .map(uid => typingUsers[uid]?.name);
            let indicator = document.getElementById('typingIndicator');
            if (!indicator && chatMessagesList && chatMessagesList.parentNode) {
                const div = document.createElement('div');
                div.id = 'typingIndicator';
                div.className = 'typing-indicator';
                chatMessagesList.parentNode.insertBefore(div, chatMessagesList.nextSibling);
                indicator = div;
            }
            if (indicator) {
                if (typingList.length) {
                    indicator.innerText = `${typingList.join('، ')} يكتب...`;
                } else {
                    indicator.innerText = '';
                }
            }
        });

        // إرسال حدث الكتابة عند الكتابة في حقل الإدخال
        if (chatTextInput) {
            chatTextInput.addEventListener('input', () => {
                if (typingTimeout) clearTimeout(typingTimeout);
                rtdb.ref(`/typing/${currentChatRoom}/${currentUser.uid}`).set({ name: currentDisplayName });
                typingTimeout = setTimeout(() => {
                    rtdb.ref(`/typing/${currentChatRoom}/${currentUser.uid}`).remove();
                }, 1500);
            });
        }
    }

    // تبديل الغرفة (عامة / خاصة)
    function switchChatRoom(roomId, roomTitle) {
        if (chatUnsub) chatUnsub();
        currentChatRoom = roomId;
        if (chatTitle) chatTitle.innerHTML = roomTitle;
        // إعادة تعيين التحميل
        chatLastDoc = null;
        chatHasMore = true;
        chatLoadingMore = false;
        if (chatMessagesList) chatMessagesList.innerHTML = '<div class="empty-state"><i class="fas fa-spinner fa-pulse"></i> جاري التحميل...</div>';
        loadChatMessages();
    }

    // فتح محادثة خاصة
    function openPrivateChat(otherUserId, otherUserName) {
        const roomId = `private_${[currentUser.uid, otherUserId].sort().join('_')}`;
        const title = `<i class="fas fa-lock"></i> محادثة خاصة مع ${escapeHtml(otherUserName)}`;
        if (mainApp && chatPageScreen) {
            mainApp.style.display = 'none';
            chatPageScreen.style.display = 'flex';
        }
        switchChatRoom(roomId, title);
    }

    // العودة للصفحة الرئيسية
    function backToMain() {
        if (mainApp && chatPageScreen) {
            mainApp.style.display = 'flex';
            chatPageScreen.style.display = 'none';
        }
        if (chatUnsub) chatUnsub();
    }

    // دالة تشغيل الرسالة الصوتية (تعريف عام)
    window.playVoiceMessage = function(url, btn) {
        const audio = new Audio(url);
        const wrapper = btn.closest('.voice-message-wrapper');
        if (!wrapper) return;
        const progress = wrapper.querySelector('.voice-waveform-progress');
        audio.play();
        audio.ontimeupdate = () => {
            if (audio.duration) {
                const percent = (audio.currentTime / audio.duration) * 100;
                if (progress) progress.style.width = percent + '%';
            }
        };
        btn.innerHTML = '<i class="fas fa-pause"></i>';
        audio.onended = () => {
            btn.innerHTML = '<i class="fas fa-play"></i>';
            if (progress) progress.style.width = '0%';
        };
    };

    // ربط الأحداث
    function bindEvents() {
        if (sendMessageBtn) sendMessageBtn.onclick = sendTextMessage;
        if (attachFileBtn) {
            attachFileBtn.onclick = () => {
                const input = document.createElement('input');
                input.type = 'file';
                input.accept = 'image/*,video/*,application/pdf,application/msword,text/plain';
                input.onchange = async (e) => {
                    if (e.target.files[0]) await sendFileMessage(e.target.files[0]);
                };
                input.click();
            };
        }
        if (sendLocationBtn) sendLocationBtn.onclick = sendLocation;
        if (walkieTalkieBtn) {
            walkieTalkieBtn.onclick = () => {
                if (!isRecording) startVoiceRecording();
                else stopVoiceRecording();
            };
        }
        if (chatBackBtn) chatBackBtn.onclick = backToMain;
        
        // أزرار لوحة التسجيل الصوتي
        const recorderCancel = document.getElementById('recorderCancelBtn');
        if (recorderCancel) recorderCancel.onclick = closeRecorderPanel;
        const recorderSend = document.getElementById('recorderSendBtn');
        if (recorderSend) recorderSend.onclick = sendRecordedVoice;
    }

    // التهيئة العلنية
    window.initChat = function() {
        initElements();
        bindEvents();
        // استماع لفتح المحادثة العامة من الـ bottom nav
        const openChatBtn = document.getElementById('openChatBtn');
        if (openChatBtn) {
            openChatBtn.onclick = () => {
                if (mainApp && chatPageScreen) {
                    mainApp.style.display = 'none';
                    chatPageScreen.style.display = 'flex';
                }
                switchChatRoom('general', 'المحادثة العامة');
            };
        }
        // إذا كانت الصفحة معروضة أصلاً (مثلاً بعد تسجيل الدخول) يمكن تفعيلها لاحقاً
    };

    // تصدير دوال للمكالمات الخارجية (مثل فتح محادثة خاصة)
    window.openPrivateChat = openPrivateChat;
    window.switchChatRoom = switchChatRoom;
    window.loadChatMessages = loadChatMessages;
})();
