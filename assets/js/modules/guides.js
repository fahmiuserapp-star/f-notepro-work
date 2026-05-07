// ========== إدارة المرشدين ==========

let guidesUnsubscribe = null;

window.subscribeToGuides = function() {
    if(guidesUnsubscribe) guidesUnsubscribe();
    if(!window.currentUser) return;
    
    guidesUnsubscribe = db.collection('guides').onSnapshot(async snapshot => {
        const allGuides = [];
        snapshot.forEach(doc => {
            const data = doc.data();
            if(data.visibility === 'public' || data.userId === window.currentUser.uid) {
                allGuides.push({ id: doc.id, ...data });
            }
        });
        
        // جلب حالة الاتصال لكل مرشد
        const statusPromises = allGuides.map(g => rtdb.ref(`/status/${g.userId}`).once('value'));
        const statuses = await Promise.all(statusPromises);
        allGuides.forEach((g, i) => {
            g.online = statuses[i].val()?.state === 'online';
        });
        
        window.guidesList = allGuides;
        renderContactsList();
    }, err => {
        console.error("Guides error:", err);
        window.showToast("خطأ في تحميل المرشدين", true);
    });
};

function renderContactsList() {
    const container = document.getElementById('contactsListContainer');
    if(!container) return;
    
    const searchTerm = document.getElementById('searchInput')?.value.toLowerCase() || '';
    let filtered = window.guidesList.filter(g => 
        g.name?.toLowerCase().includes(searchTerm) || 
        g.phone?.includes(searchTerm) ||
        g.agency?.toLowerCase().includes(searchTerm)
    );
    
    if(window.currentColorFilter && window.currentColorFilter !== 'all') {
        filtered = filtered.filter(g => g.cardColor === window.currentColorFilter);
    }
    
    if(!filtered.length) {
        container.innerHTML = '<div class="empty-state"><i class="fas fa-user-slash"></i> لا توجد جهات اتصال</div>';
        return;
    }
    
    container.innerHTML = filtered.map(g => `
        <div class="contact-card" style="border-right-color:${g.cardColor || '#e94560'}">
            <div class="contact-row">
                <img class="contact-avatar" src="${g.guidePhoto || ''}" 
                    onerror="this.src='data:image/svg+xml,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20fill%3D%22%23e94560%22%3E%3Ctext%20x%3D%2228%22%20y%3D%2235%22%20text-anchor%3D%22middle%22%20fill%3D%22white%22%20font-size%3D%2224%22%3E${g.name?.charAt(0) || '?'}%3C%2Ftext%3E%3C%2Fsvg%3E'"
                    onclick="window.showFullImage('${g.guidePhoto}')">
                <div class="contact-details">
                    <div class="contact-name">
                        ${window.escapeHtml(g.name)}
                        <span class="status-dot ${g.online ? 'online' : 'offline'}" style="width:10px;height:10px;border-radius:50%;background:${g.online ? '#10b981' : '#94a3b8'};display:inline-block;"></span>
                    </div>
                    <div class="contact-info"><i class="fas fa-phone"></i> ${window.escapeHtml(g.phone)}</div>
                    <div class="contact-info"><i class="fas fa-building"></i> ${window.escapeHtml(g.agency)}</div>
                    <div class="contact-info"><i class="fas fa-bus"></i> ${window.escapeHtml(g.vehicle)}</div>
                </div>
                <button class="expand-icon" data-id="${g.id}"><i class="fas fa-chevron-down"></i></button>
            </div>
            <div id="exp-${g.id}" class="contact-expand">
                <div class="expand-inner">
                    ${g.busPhoto ? `<img src="${g.busPhoto}" style="width:50px;border-radius:12px;margin-bottom:10px;cursor:pointer;" onclick="window.showFullImage('${g.busPhoto}')">` : ''}
                    <p><i class="fas fa-car"></i> اللوحة: ${window.escapeHtml(g.plate || '-')}</p>
                    <p><i class="fas fa-sticky-note"></i> ${window.escapeHtml((g.notes || '').substring(0, 60))}</p>
                    <div class="call-buttons">
                        <button class="call-btn btn-whatsapp" onclick="window.makeCall('${g.phone}', 'whatsapp')"><i class="fab fa-whatsapp"></i> واتساب</button>
                        <button class="call-btn btn-telegram" onclick="window.makeCall('${g.phone}', 'telegram')"><i class="fab fa-telegram"></i> تلغرام</button>
                        <button class="call-btn btn-call" onclick="window.makeCall('${g.phone}', 'call')"><i class="fas fa-phone"></i> اتصل</button>
                    </div>
                    <div class="action-buttons">
                        ${g.userId === window.currentUser?.uid ? `
                            <button class="action-btn btn-edit" onclick="window.editGuide('${g.id}')"><i class="fas fa-edit"></i> تعديل</button>
                            <button class="action-btn btn-delete" onclick="window.deleteGuide('${g.id}')"><i class="fas fa-trash"></i> حذف</button>
                        ` : ''}
                        <button class="action-btn btn-share" onclick="window.shareGuide('${g.id}')"><i class="fab fa-whatsapp"></i> مشاركة</button>
                    </div>
                </div>
            </div>
        </div>
    `).join('');
    
    // ربط أحداث التوسيع
    document.querySelectorAll('.expand-icon').forEach(btn => {
        btn.onclick = () => {
            const div = document.getElementById(`exp-${btn.dataset.id}`);
            div.classList.toggle('show');
            btn.innerHTML = div.classList.contains('show') ? '<i class="fas fa-chevron-up"></i>' : '<i class="fas fa-chevron-down"></i>';
        };
    });
}

// دوال المرشدين
window.makeCall = (phone, type) => {
    const clean = phone.replace(/[^0-9+]/g, '');
    if(type === 'whatsapp') window.open(`https://wa.me/${clean}`, '_blank');
    else if(type === 'telegram') window.open(`https://t.me/${clean}`, '_blank');
    else window.location.href = `tel:${clean}`;
};

window.shareGuide = (id) => {
    const g = window.guidesList.find(g => g.id === id);
    if(g) {
        const msg = `*${g.name}*\n📞 ${g.phone}\n🚐 ${g.vehicle}\n🏢 ${g.agency}`;
        window.open(`https://wa.me/?text=${encodeURIComponent(msg)}`, '_blank');
    }
};

window.editGuide = async (id) => {
    const g = window.guidesList.find(g => g.id === id);
    if(g && g.userId === window.currentUser?.uid) {
        window.currentEditGuideId = id;
        document.getElementById('guideName').value = g.name;
        document.getElementById('guidePhone').value = g.phone;
        document.getElementById('guideAgency').value = g.agency || '';
        document.getElementById('guideVehicle').value = g.vehicle || '';
        document.getElementById('guidePlate').value = g.plate || '';
        document.getElementById('guideNotes').value = g.notes || '';
        document.getElementById('guideVisibilitySelect').value = g.visibility || 'private';
        if(g.guidePhoto) {
            window.currentGuidePhoto = g.guidePhoto;
            document.getElementById('guidePhotoImg').src = g.guidePhoto;
            document.getElementById('guidePhotoPreview').style.display = 'flex';
        }
        document.getElementById('modalTitle').innerHTML = '<i class="fas fa-edit"></i> تعديل مرشد';
        document.getElementById('guideModal').classList.add('show');
    }
};

window.deleteGuide = async (id) => {
    if(confirm('هل أنت متأكد من حذف هذا المرشد؟')) {
        await db.collection('guides').doc(id).delete();
        window.showToast("✓ تم الحذف");
    }
};

window.saveGuideToDB = async function() {
    const name = document.getElementById('guideName').value.trim();
    const phone = document.getElementById('guidePhone').value.trim();
    const agency = document.getElementById('guideAgency').value.trim();
    const vehicle = document.getElementById('guideVehicle').value.trim();
    
    if(!name || !phone || !agency || !vehicle) {
        window.showToast("جميع الحقول المطلوبة", true);
        return;
    }
    if(!window.validatePhone(phone)) {
        window.showToast("رقم هاتف غير صالح", true);
        return;
    }
    
    const data = {
        name, phone, agency, vehicle,
        plate: document.getElementById('guidePlate').value.trim(),
        notes: document.getElementById('guideNotes').value.trim(),
        visibility: document.getElementById('guideVisibilitySelect').value,
        cardColor: window.selectedCardColor || '#e94560',
        guidePhoto: window.currentGuidePhoto || '',
        busPhoto: window.currentBusPhoto || '',
        userId: window.currentUser.uid,
        userDisplayName: window.currentDisplayName,
        updatedAt: firebase.firestore.FieldValue.serverTimestamp()
    };
    
    try {
        if(window.currentEditGuideId) {
            await db.collection('guides').doc(window.currentEditGuideId).update(data);
            window.showToast("✓ تم التعديل");
        } else {
            data.createdAt = firebase.firestore.FieldValue.serverTimestamp();
            await db.collection('guides').add(data);
            window.showToast("✓ تمت الإضافة");
        }
        document.getElementById('guideModal').classList.remove('show');
        resetGuideForm();
    } catch(e) {
        window.showToast("خطأ: " + e.message, true);
    }
};

function resetGuideForm() {
    window.currentEditGuideId = null;
    window.currentGuidePhoto = '';
    window.currentBusPhoto = '';
    document.getElementById('guidePhotoPreview').style.display = 'none';
    document.getElementById('busPhotoPreview').style.display = 'none';
    document.getElementById('guideName').value = '';
    document.getElementById('guidePhone').value = '';
    document.getElementById('guideAgency').value = '';
    document.getElementById('guideVehicle').value = '';
    document.getElementById('guidePlate').value = '';
    document.getElementById('guideNotes').value = '';
    document.getElementById('guideVisibilitySelect').value = 'private';
    document.getElementById('modalTitle').innerHTML = '<i class="fas fa-user-plus"></i> إضافة مرشد';
}

// ربط الأحداث
document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('addGuideBtn')?.addEventListener('click', () => {
        resetGuideForm();
        document.getElementById('guideModal').classList.add('show');
    });
    
    document.getElementById('saveGuideBtn')?.addEventListener('click', window.saveGuideToDB);
    document.getElementById('closeGuideModalBtn')?.addEventListener('click', () => {
        document.getElementById('guideModal').classList.remove('show');
    });
    
    document.getElementById('uploadGuidePhotoTrigger')?.addEventListener('click', () => window.triggerImageUpload('guide'));
    document.getElementById('uploadBusPhotoTrigger')?.addEventListener('click', () => window.triggerImageUpload('bus'));
    
    // ألوان البطاقات
    const colors = ['#e94560', '#2c7da0', '#e76f51', '#2a9d8f', '#e9c46a', '#9b5de5'];
    const colorContainer = document.getElementById('guideColorSelector');
    if(colorContainer) {
        colorContainer.innerHTML = colors.map(c => `<div class="color-option" style="width:40px;height:40px;border-radius:50%;background:${c};cursor:pointer;border:2px solid var(--border);" data-color="${c}"></div>`).join('');
        colorContainer.querySelectorAll('.color-option').forEach(opt => {
            opt.onclick = () => {
                window.selectedCardColor = opt.dataset.color;
                colorContainer.querySelectorAll('.color-option').forEach(o => o.style.border = '2px solid var(--border)');
                opt.style.border = '3px solid var(--accent)';
            };
        });
    }
});
