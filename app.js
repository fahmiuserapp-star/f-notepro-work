// Main Application Initialization
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
document.getElementById('syncDataBtn').onclick = () => { loadGuides(); showToast("تمت المزامنة"); };
document.getElementById('openProfileBtn').onclick = () => { document.getElementById('sidebar').classList.add('open'); document.getElementById('overlay').classList.add('show'); };

// Voice search
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

showToast("✨ النظام جاهز - يرجى تسجيل الدخول");