// Authentication Module

let currentUser = null;
let currentDisplayName = "";
let userAvatarUrl = "";

function updateProfileUI() {
    document.getElementById('displayNameSpan').innerText = currentDisplayName;
    document.getElementById('headerAvatar').src = userAvatarUrl || '';
    document.getElementById('profileAvatar').src = userAvatarUrl || '';
    document.getElementById('avatarPreview').src = userAvatarUrl || '';
    document.getElementById('profileNameInput').value = currentDisplayName;
}

async function saveProfile() {
    let newName = document.getElementById('profileNameInput').value.trim();
    if (newName && newName !== currentDisplayName) {
        await db.collection('user_profiles').doc(currentUser.uid).set({ displayName: newName }, { merge: true });
        currentDisplayName = newName;
        updateProfileUI();
        showToast("تم حفظ الاسم");
    }
}

async function checkUserProfile() {
    let doc = await db.collection('user_profiles').doc(currentUser.uid).get();
    if (doc.exists) {
        currentDisplayName = doc.data().displayName;
        userAvatarUrl = doc.data().avatarUrl || '';
    } else {
        currentDisplayName = document.getElementById('loginDisplayName').value.trim();
        await db.collection('user_profiles').doc(currentUser.uid).set({ displayName: currentDisplayName });
    }
    updateProfileUI();
    // Set presence
    rtdb.ref(`/status/${currentUser.uid}`).set({ state: 'online', name: currentDisplayName });
    rtdb.ref(`/status/${currentUser.uid}`).onDisconnect().set({ state: 'offline', lastSeen: firebase.database.ServerValue.TIMESTAMP });
}

// Login handler
document.getElementById('doLoginBtn').onclick = async () => {
    let email = document.getElementById('loginEmail').value.trim();
    let pass = document.getElementById('loginPassword').value;
    let name = document.getElementById('loginDisplayName').value.trim();
    if (!email || !pass || !name) {
        document.getElementById('loginError').innerText = 'جميع الحقول مطلوبة';
        return;
    }
    try {
        await auth.signInWithEmailAndPassword(email, pass);
        currentUser = auth.currentUser;
        await checkUserProfile();
        document.getElementById('loginScreen').classList.add('hide');
        document.getElementById('mainApp').classList.add('show');
        showToast(`مرحباً ${currentDisplayName}`);
        // Load data after login
        if (typeof loadGuides === 'function') loadGuides();
        if (typeof loadChatMessages === 'function') loadChatMessages();
        if (typeof loadNotes === 'function') loadNotes();
    } catch (e) {
        document.getElementById('loginError').innerText = e.message;
        showToast(e.message, true);
    }
};

// Logout
document.getElementById('logoutSidebar').onclick = () => {
    auth.signOut();
    document.getElementById('mainApp').classList.remove('show');
    document.getElementById('loginScreen').classList.remove('hide');
    if (typeof chatUnsub === 'function') chatUnsub();
    if (typeof unsubscribeGuides === 'function') unsubscribeGuides();
    showToast("تم تسجيل الخروج");
};

// Auth state observer
auth.onAuthStateChanged(user => {
    if (user && !currentUser) {
        currentUser = user;
        checkUserProfile().then(() => {
            if (typeof loadGuides === 'function') loadGuides();
            if (typeof loadChatMessages === 'function') loadChatMessages();
            if (typeof loadNotes === 'function') loadNotes();
        });
    }
});
