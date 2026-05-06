// Guides Management Module
let guidesList = [];
let currentEditId = null;
let selectedColor = "#e94560";
let currentColorFilter = "all";
let currentGuidePhoto = "";
let currentBusPhoto = "";
let unsubscribeGuides = null;

async function compressAndUploadImage(file, type) {
    return new Promise((resolve) => {
        let reader = new FileReader();
        reader.onload = e => {
            let img = new Image();
            img.onload = async () => {
                let canvas = document.createElement('canvas');
                let w = img.width, h = img.height;
                let maxW = 400;
                if (w > maxW) { h *= maxW / w; w = maxW; }
                canvas.width = w; canvas.height = h;
                canvas.getContext('2d').drawImage(img, 0, 0, w, h);
                let blob = await new Promise(r => canvas.toBlob(r, 'image/jpeg', 0.7));
                let url = await uploadToCloudinary(blob, type === 'guide' ? 'guides' : (type === 'bus' ? 'vehicles' : 'avatars'));
                if (type === 'guide') {
                    currentGuidePhoto = url;
                    document.getElementById('guidePhotoImg').src = url;
                    document.getElementById('guidePhotoPreview').style.display = 'flex';
                } else if (type === 'bus') {
                    currentBusPhoto = url;
                    document.getElementById('busPhotoImg').src = url;
                    document.getElementById('busPhotoPreview').style.display = 'flex';
                }
                showToast("✓ تم رفع الصورة");
                resolve(url);
            };
            img.src = e.target.result;
        };
        reader.readAsDataURL(file);
    });
}

function triggerImageUpload(type) {
    let inp = document.createElement('input');
    inp.type = 'file';
    inp.accept = 'image/*';
    inp.onchange = async e => { if (e.target.files[0]) await compressAndUploadImage(e.target.files[0], type); };
    inp.click();
}

document.getElementById('uploadGuidePhoto').onclick = () => triggerImageUpload('guide');
document.getElementById('uploadBusPhoto').onclick = () => triggerImageUpload('bus');

async function saveGuide() {
    let name = document.getElementById('guideName').value.trim();
    let phone = document.getElementById('guidePhone').value.trim();
    let agency = document.getElementById('guideAgency').value.trim();
    let vehicle = document.getElementById('guideVehicle').value.trim();
    if (!name || !phone || !agency || !vehicle) return showToast("جميع الحقول مطلوبة", true);
    if (!validatePhone(phone)) {
        document.getElementById('phoneError').style.display = 'block';
        return showToast("رقم هاتف غير صالح", true);
    }
    document.getElementById('phoneError').style.display = 'none';
    let data = {
        name, phone, agency, vehicle,
        plate: document.getElementById('guidePlate').value.trim(),
        notes: document.getElementById('guideNotes').value.trim(),
        visibility: document.getElementById('guideVisibility').value,
        cardColor: selectedColor,
        guidePhoto: currentGuidePhoto,
        busPhoto: currentBusPhoto,
        userId: currentUser.uid,
        userDisplayName: currentDisplayName,
        userAvatar: userAvatarUrl,
        updatedAt: firebase.firestore.FieldValue.serverTimestamp()
    };
    try {
        if (currentEditId) await db.collection('guides').doc(currentEditId).update(data);
        else { data.createdAt = firebase.firestore.FieldValue.serverTimestamp(); await db.collection('guides').add(data); }
        showToast(currentEditId ? "تم التعديل" : "تمت الإضافة");
        document.getElementById('guideModal').classList.remove('show');
        resetGuideForm();
    } catch (e) { showToast("خطأ: " + e.message, true); }
}

function resetGuideForm() {
    currentEditId = null;
    currentGuidePhoto = "";
    currentBusPhoto = "";
    document.getElementById('guidePhotoPreview').style.display = 'none';
    document.getElementById('busPhotoPreview').style.display = 'none';
    document.getElementById('guideName').value = '';
    document.getElementById('guidePhone').value = '';
    document.getElementById('guideAgency').value = '';
    document.getElementById('guideVehicle').value = '';
    document.getElementById('guidePlate').value = '';
    document.getElementById('guideNotes').value = '';
    document.getElementById('guideVisibility').value = 'private';
    selectedColor = '#e94560';
    document.querySelectorAll('#guideColorSelector .color-option').forEach(c => c.classList.remove('selected'));
    document.querySelector('#guideColorSelector .color-option[data-color="#e94560"]')?.classList.add('selected');
    document.getElementById('modalTitle').innerHTML = '<i class="fas fa-user-plus"></i> إضافة مرشد';
}

async function renderContacts() {
    let search = document.getElementById('searchInput').value.toLowerCase();
    let filtered = guidesList.filter(g => (g.name?.toLowerCase().includes(search) || g.phone?.includes(search) || g.agency?.toLowerCase().includes(search)));
    if (currentColorFilter !== 'all') filtered = filtered.filter(g => g.cardColor === currentColorFilter);
    let container = document.getElementById('contactsListContainer');
    if (!filtered.length) {
        container.innerHTML = '<div class="empty-state"><i class="fas fa-user-slash"></i> لا توجد جهات اتصال</div>';
        return;
    }
    let html = '';
    for (let g of filtered) {
        let statusSnap = await rtdb.ref(`/status/${g.userId}`).once('value');
        let isOnline = statusSnap.val()?.state === 'online';
        html += `<div class="contact-card" style="border-right-color:${g.cardColor || '#e94560'}">
            <div class="contact-row">
                <img class="contact-avatar" src="${g.guidePhoto || ''}" onerror="this.src='data:image/svg+xml,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20fill%3D%22%23e94560%22%3E%3Ccircle%20cx%3D%2228%22%20cy%3D%2228%22%20r%3D%2228%22%20fill%3D%22%23e94560%22%2F%3E%3Ctext%20x%3D%2228%22%20y%3D%2235%22%20text-anchor%3D%22middle%22%20fill%3D%22white%22%20font-size%3D%2224%22%3E${g.name?.charAt(0) || '?'}%3C%2Ftext%3E%3C%2Fsvg%3E'" onclick="window.showFullImage('${g.guidePhoto}')">
                <div class="contact-details">
                    <div class="contact-name">${escapeHtml(g.name)} <span class="status-badge" style="background:${isOnline ? '#10b981' : '#64748b'};"></span> ${g.visibility === 'public' ? '<span style="background:#3b82f6;font-size:8px;padding:2px 8px;border-radius:20px;">عام</span>' : '<span style="background:#64748b;font-size:8px;padding:2px 8px;border-radius:20px;">خاص</span>'}</div>
                    <div class="contact-phone"><i class="fas fa-phone-alt"></i> ${escapeHtml(g.phone)}</div>
                    <div class="contact-phone"><i class="fas fa-building"></i> ${escapeHtml(g.agency)}</div>
                    <div class="contact-phone"><i class="fas fa-bus"></i> ${escapeHtml(g.vehicle)}</div>
                </div>
                <button class="expand-icon" data-id="${g.id}"><i class="fas fa-chevron-down"></i></button>
            </div>
            <div id="exp-${g.id}" class="contact-expand">
                <div class="expand-inner">
                    ${g.busPhoto ? `<img src="${g.busPhoto}" style="width:50px;border-radius:12px;cursor:pointer;margin-bottom:8px;" onclick="window.showFullImage('${g.busPhoto}')">` : ''}
                    <p><i class="fas fa-car"></i> اللوحة: ${escapeHtml(g.plate || '-')}</p>
                    <p><i class="fas fa-sticky-note"></i> ملاحظات: ${escapeHtml((g.notes || '').substring(0, 60))}</p>
                    <div class="call-buttons">
                        <button class="call-btn btn-wa" onclick="makeCall('${g.phone}', 'whatsapp')"><i class="fab fa-whatsapp"></i> واتساب</button>
                        <button class="call-btn btn-tg" onclick="makeCall('${g.phone}', 'telegram')"><i class="fab fa-telegram"></i> تلغرام</button>
                        <button class="call-btn btn-phone" onclick="makeCall('${g.phone}', 'call')"><i class="fas fa-phone-alt"></i> اتصل</button>
                    </div>
                    <div>
                        ${g.userId === currentUser?.uid ? `<button class="action-btn btn-edit" data-edit="${g.id}">تعديل</button><button class="action-btn btn-delete" data-del="${g.id}">حذف</button>` : ''}
                        <button class="action-btn btn-share" data-share="${g.id}"><i class="fas fa-share-alt"></i> نشر</button>
                    </div>
                </div>
            </div>
        </div>`;
    }
    container.innerHTML = html;
    attachGuideEventListeners();
}

function attachGuideEventListeners() {
    document.querySelectorAll('.expand-icon').forEach(btn => {
        btn.onclick = () => {
            let div = document.getElementById(`exp-${btn.dataset.id}`);
            div.classList.toggle('show');
            btn.innerHTML = div.classList.contains('show') ? '<i class="fas fa-chevron-up"></i>' : '<i class="fas fa-chevron-down"></i>';
        };
    });
    document.querySelectorAll('[data-edit]').forEach(btn => { btn.onclick = () => editGuideById(btn.dataset.edit); });
    document.querySelectorAll('[data-del]').forEach(btn => { btn.onclick = () => deleteGuideById(btn.dataset.del); });
    document.querySelectorAll('[data-share]').forEach(btn => { btn.onclick = () => shareGuide(btn.dataset.share); });
}

window.makeCall = (phone, type) => {
    let clean = phone.replace(/[^0-9+]/g, '');
    if (type === 'whatsapp') window.open(`https://wa.me/${clean}`, '_blank');
    else if (type === 'telegram') window.open(`https://t.me/${clean}`, '_blank');
    else window.location.href = `tel:${clean}`;
};

window.showFullImage = (url) => { if (url) { document.getElementById('fullImageView').src = url; document.getElementById('fullImageModal').classList.add('show'); } };

async function shareGuide(id) {
    let g = guidesList.find(g => g.id === id);
    if (g) window.open(`https://wa.me/?text=${encodeURIComponent(`*${g.name}*\n📞 ${g.phone}\n🚐 ${g.vehicle}\n🏢 ${g.agency}\n🚍 ${g.plate || ''}`)}`, '_blank');
}

async function editGuideById(id) {
    let g = guidesList.find(g => g.id === id);
    if (g && g.userId === currentUser?.uid) {
        currentEditId = id;
        document.getElementById('guideName').value = g.name;
        document.getElementById('guidePhone').value = g.phone;
        document.getElementById('guideAgency').value = g.agency || '';
        document.getElementById('guideVehicle').value = g.vehicle || '';
        document.getElementById('guidePlate').value = g.plate || '';
        document.getElementById('guideNotes').value = g.notes || '';
        selectedColor = g.cardColor || '#e94560';
        document.getElementById('guideVisibility').value = g.visibility || 'private';
        if (g.guidePhoto) {
            currentGuidePhoto = g.guidePhoto;
            document.getElementById('guidePhotoImg').src = g.guidePhoto;
            document.getElementById('guidePhotoPreview').style.display = 'flex';
        }
        if (g.busPhoto) {
            currentBusPhoto = g.busPhoto;
            document.getElementById('busPhotoImg').src = g.busPhoto;
            document.getElementById('busPhotoPreview').style.display = 'flex';
        }
        document.getElementById('modalTitle').innerHTML = '<i class="fas fa-user-edit"></i> تعديل مرشد';
        document.getElementById('guideModal').classList.add('show');
    } else showToast('لا يمكنك تعديل بيانات الآخرين', true);
}

async function deleteGuideById(id) {
    if (confirm('حذف المرشد نهائياً؟')) await db.collection('guides').doc(id).delete();
    showToast('تم الحذف');
}

function loadGuides() {
    if (unsubscribeGuides) unsubscribeGuides();
    if (!currentUser) return;
    let mine = db.collection('guides').where('userId', '==', currentUser.uid);
    let update = () => {
        Promise.all([mine.get(), db.collection('guides').where('visibility', '==', 'public').get()]).then(([mineSnap, pubSnap]) => {
            let all = [];
            mineSnap.forEach(d => all.push({ id: d.id, ...d.data() }));
            pubSnap.forEach(d => { if (d.data().userId !== currentUser.uid) all.push({ id: d.id, ...d.data() }); });
            guidesList = all;
            renderContacts();
        });
    };
    mine.onSnapshot(update);
    db.collection('guides').where('visibility', '==', 'public').onSnapshot(update);
    unsubscribeGuides = () => { };
}
