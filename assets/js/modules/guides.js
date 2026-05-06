window.subscribeToGuides = function() {
    if(window.unsubscribeGuides) window.unsubscribeGuides();
    if(!window.currentUser) return;
    window.unsubscribeGuides = db.collection('guides').onSnapshot(async snapshot => {
        let allGuides = [];
        snapshot.forEach(doc => {
            let data = doc.data();
            if(data.visibility === 'public' || data.userId === window.currentUser.uid) {
                allGuides.push({ id: doc.id, ...data });
            }
        });
        // جلب حالة الاتصال لجميع المستخدمين مرة واحدة باستخدام Promise.all
        const statusPromises = allGuides.map(guide => rtdb.ref(`/status/${guide.userId}`).once('value'));
        const statusSnapshots = await Promise.all(statusPromises);
        const statusMap = {};
        statusSnapshots.forEach((snap, idx) => {
            statusMap[allGuides[idx].userId] = snap.val()?.state === 'online';
        });
        window.guidesList = allGuides.map(guide => ({ ...guide, online: statusMap[guide.userId] || false }));
        window.renderContactsList();
    }, err => window.showToast("خطأ في المزامنة: " + err.message, true));
};

window.renderContactsList = async function() {
    // نفس الكود السابق مع استخدام guide.online
    // ... (سيتم إعادة كتابته بشكل كامل لكن بنفس المنطق)
};
