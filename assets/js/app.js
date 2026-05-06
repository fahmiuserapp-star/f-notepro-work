// Main Application Initialization

// Notes functions
async function loadNotes() {
    if (!currentUser) return;
    let snap = await db.collection('notes').where('userId', '==', currentUser.uid).orderBy('createdAt', 'desc').get();
    notesList = [];
    snap.forEach(d => notesList.push({ id: d.id, ...d.data() }));
    renderNotes();
}

function renderNotes() {
    let container = document.getElementById('notesListContainer');
    if (!notesList.length) { container.innerHTML = '<div class="empty-state">لا توجد ملاحظات</div>'; return; }
    container.innerHTML = notesList.map(n => `
        <div class="note-card">
            <div class="note-title">${escapeHtml(n.title)}</div>
            <div class="note-content">${escapeHtml(n.content || '')}</div>
            <div class="note-date">${n.createdAt ? new Date(n.createdAt.toDate()).toLocaleString() : ''}</div>
            <div><button class="action-btn btn-delete" onclick="deleteNote('${n.id}')">حذف</button></div>
        </div>
    `).join('');
}

window.deleteNote = async (id) => { await db.collection('notes').doc(id).delete(); loadNotes(); };

async function addNote(title, content) {
    await db.collection('notes').add({ title, content, userId: currentUser.uid, createdAt: firebase.firestore.FieldValue.serverTimestamp() });
    loadNotes();
    showToast("تم إضافة الملاحظة");
}

document.getElementById('addNoteFab').onclick = () => document.getElementById('noteModal').classList.add('show');
document.getElementById('saveNoteBtn').onclick = async () => {
    let title = document.getElementById('noteTitle').value.trim();
    if (!title) return showToast("عنوان الملاحظة مطلوب", true);
    await addNote(title, document.getElementById('noteContent').value);
    document.getElementById('noteModal').classList.remove('show');
    document.getElementById('noteTitle').value = '';
    document.getElementById('noteContent').value = '';
};
document.getElementById('closeNoteModalBtn').onclick = () => document.getElementById('noteModal').classList.remove('show');

// Page switching
function switchPage(page) {
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    document.getElementById(`${page}Page`).classList.add('active');
    document.querySelectorAll('.nav-item[data-page]').forEach(item => {
        if (item.dataset.page === page) item.classList.add('active');
        else item.classList.remove('active');
    });
    if (page === 'notes') loadNotes();
}

document.querySelectorAll('.nav-item[data-page]').forEach(item => { item.onclick = () => switchPage(item.dataset.page); });
document.getElementById('syncDataBtn').onclick = () => { loadGuides(); showToast("تمت المزامنة"); };
document.getElementById('colorToggleBtn').onclick = () => document.getElementById('colorStrip').classList.toggle('show');
document.getElementById('menuBtn').onclick = () => { document.getElementById('sidebar').classList.add('open'); document.getElementById('overlay').classList.add('show'); };
document.getElementById('closeSidebarBtn').onclick = () => { document.getElementById('sidebar').classList.remove('open'); document.getElementById('overlay').classList.remove('show'); };
document.getElementById('overlay').onclick = () => { document.getElementById('sidebar').classList.remove('open'); document.getElementById('overlay').classList.remove('show'); };
document.getElementById('saveProfileBtn').onclick = saveProfile;
document.getElementById('changeAvatarSidebar').onclick = () => document.getElementById('avatarUploadModal').classList.add('show');
document.getElementById('darkModeSidebar').onclick = () => { document.body.classList.toggle('dark'); localStorage.setItem('darkMode', document.body.classList.contains('dark')); };
if (localStorage.getItem('darkMode') === 'true') document.body.classList.add('dark');
document.getElementById('muteNotificationsSidebar').onclick = () => { isMuted = !isMuted; showToast(isMuted ? "تم كتم الإشعارات" : "تم إلغاء الكتم"); };
document.getElementById('exportChatSidebar').onclick = exportChatToPDF;
document.getElementById('clearChatSidebar').onclick = clearChat;
document.getElementById('openProfileBtn').onclick = () => { document.getElementById('sidebar').classList.add('open'); document.getElementById('overlay').classList.add('show'); };
document.getElementById('closeGuideModalBtn').onclick = () => document.getElementById('guideModal').classList.remove('show');
document.getElementById('saveGuideBtn').onclick = saveGuide;
document.getElementById('closeFullImageBtn').onclick = () => document.getElementById('fullImageModal').classList.remove('show');
document.getElementById('closeAvatarModal').onclick = () => document.getElementById('avatarUploadModal').classList.remove('show');
document.getElementById('selectAvatarBtn').onclick = () => {
    let inp = document.createElement('input');
    inp.type = 'file';
    inp.accept = 'image/*';
    inp.onchange = async e => {
        if (e.target.files[0]) {
            await compressAndUploadImage(e.target.files[0], 'avatar');
            await db.collection('user_profiles').doc(currentUser.uid).update({ avatarUrl: userAvatarUrl });
            updateProfileUI();
            showToast("تم تحديث الصورة");
            document.getElementById('avatarUploadModal').classList.remove('show');
        }
    };
    inp.click();
};
document.getElementById('openModalBtn')?.addEventListener('click', () => { resetGuideForm(); document.getElementById('guideModal').classList.add('show'); });
document.getElementById('searchInput').addEventListener('input', renderContacts);
let speech = new (window.webkitSpeechRecognition || window.SpeechRecognition)();
if (speech) speech.lang = 'ar';
document.getElementById('voiceSearchBtn').onclick = () => {
    if (speech) {
        speech.start();
        speech.onresult = e => {
            document.getElementById('searchInput').value = e.results[0][0].transcript;
            renderContacts();
        };
    }
};

// Color pickers
let colorArray = ['#e94560', '#2c7da0', '#e76f51', '#2a9d8f', '#e9c46a', '#9b5de5', '#06d6a0', '#ff006e'];
function buildColorPicker(containerId, isFilter) {
    let container = document.getElementById(containerId);
    if (!container) return;
    let html = '';
    if (isFilter) html += `<div class="color-chip" data-color="all" style="background:linear-gradient(135deg,#e94560,#f59e0b);"></div>`;
    colorArray.forEach(c => { html += `<div class="color-chip" style="background:${c};" data-color="${c}"></div>`; });
    container.innerHTML = html;
    container.querySelectorAll('.color-chip').forEach(chip => {
        chip.onclick = () => {
            if (isFilter) currentColorFilter = chip.dataset.color;
            else selectedColor = chip.dataset.color;
            document.querySelectorAll(`#${containerId} .color-chip`).forEach(c => c.classList.remove('active'));
            chip.classList.add('active');
            if (isFilter) renderContacts();
        };
    });
    if (isFilter) container.querySelector('.color-chip[data-color="all"]')?.classList.add('active');
    else container.querySelector('.color-chip[data-color="#e94560"]')?.classList.add('active');
}
buildColorPicker('colorStrip', true);
buildColorPicker('guideColorSelector', false);

// Bind upload buttons after DOM ready
document.getElementById('uploadGuidePhoto').onclick = () => triggerImageUpload('guide');
document.getElementById('uploadBusPhoto').onclick = () => triggerImageUpload('bus');

showToast("✨ النظام جاهز - يرجى تسجيل الدخول");
