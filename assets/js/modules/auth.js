document.addEventListener('DOMContentLoaded', () => {
    const loginBtn = document.getElementById('doLoginBtn');
    if(loginBtn) {
        loginBtn.onclick = async () => {
            let email = document.getElementById('loginEmail').value.trim();
            let pass = document.getElementById('loginPassword').value;
            let name = document.getElementById('loginDisplayName').value.trim();
            if(!email || !pass || !name) { document.getElementById('loginError').innerText = 'جميع الحقول مطلوبة'; return; }
            try {
                await auth.signInWithEmailAndPassword(email, pass);
                window.currentUser = auth.currentUser;
                await window.checkUserProfile();
                document.getElementById('loginScreen').classList.add('hide');
                document.getElementById('mainApp').classList.add('show');
                window.showToast(`مرحباً ${window.currentDisplayName}`);
            } catch(e) { document.getElementById('loginError').innerText = e.message; window.showToast(e.message, true); }
        };
    }
    document.getElementById('logoutMenuItem').onclick = () => {
        auth.signOut();
        document.getElementById('mainApp').classList.remove('show');
        document.getElementById('loginScreen').classList.remove('hide');
        if(window.chatUnsub) window.chatUnsub();
        if(window.unsubscribeGuides) window.unsubscribeGuides();
        window.showToast("تم تسجيل الخروج");
    };
    auth.onAuthStateChanged(user => { if(user && !window.currentUser) { window.currentUser = user; window.checkUserProfile(); } });
});

window.checkUserProfile = async function() {
    let doc = await db.collection('user_profiles').doc(window.currentUser.uid).get();
    if(doc.exists) {
        window.currentDisplayName = doc.data().displayName;
        window.userAvatarUrl = doc.data().avatarUrl || '';
    } else {
        window.currentDisplayName = document.getElementById('loginDisplayName').value.trim() || "مستخدم جديد";
        await db.collection('user_profiles').doc(window.currentUser.uid).set({ displayName: window.currentDisplayName });
    }
    window.updateProfileUI();
    window.subscribeToGuides();
    window.loadTasks();
    window.loadNotes();
    window.loadEvents();
    rtdb.ref(`/status/${window.currentUser.uid}`).set({ state: 'online', name: window.currentDisplayName });
    rtdb.ref(`/status/${window.currentUser.uid}`).onDisconnect().set({ state: 'offline', lastSeen: firebase.database.ServerValue.TIMESTAMP });
};

window.updateProfileUI = function() {
    document.getElementById('displayNameSpan').innerText = window.currentDisplayName;
    document.getElementById('headerAvatar').src = window.userAvatarUrl || '';
    document.getElementById('profileAvatarLarge').src = window.userAvatarUrl || '';
    document.getElementById('profileNameInput').value = window.currentDisplayName;
    document.getElementById('avatarPreviewImg').src = window.userAvatarUrl || '';
};

window.saveProfileChanges = async function() {
    let newName = document.getElementById('profileNameInput').value.trim();
    if(newName && newName !== window.currentDisplayName) {
        await db.collection('user_profiles').doc(window.currentUser.uid).set({ displayName: newName }, { merge: true });
        window.currentDisplayName = newName;
    }
    window.updateProfileUI();
    window.showToast("تم حفظ التغييرات");
    document.getElementById('sidebarNew').classList.remove('open');
    document.getElementById('overlayNew').style.display = 'none';
};
