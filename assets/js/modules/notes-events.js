// ========== إدارة الملاحظات ==========

window.notesList = [];
window.eventsList = [];

// ------------------- الملاحظات -------------------
window.loadNotes = async function() {
    if(!window.currentUser) return;
    try {
        const snapshot = await db.collection('notes')
            .where('userId', '==', window.currentUser.uid)
            .orderBy('createdAt', 'desc')
            .get();
        window.notesList = [];
        snapshot.forEach(doc => {
            window.notesList.push({ id: doc.id, ...doc.data() });
        });
        window.renderNotesList();
    } catch(e) {
        console.error("خطأ في تحميل الملاحظات:", e);
        window.showToast("خطأ في تحميل الملاحظات: " + e.message, true);
    }
};

window.renderNotesList = function() {
    const container = document.getElementById('notesListContainer');
    if(!container) return;
    
    if(!window.notesList.length) {
        container.innerHTML = '<div class="empty-state"><i class="fas fa-sticky-note"></i> لا توجد ملاحظات</div>';
        return;
    }
    
    container.innerHTML = window.notesList.map(note => `
        <div class="contact-card" style="border-right-color: #f59e0b;">
            <div style="display:flex;justify-content:space-between;align-items:center;">
                <strong style="font-size:16px;">📝 ${window.escapeHtml(note.title)}</strong>
                <small style="color:var(--text-secondary);font-size:10px;">${note.createdAt ? new Date(note.createdAt.toDate()).toLocaleDateString('ar-EG') : ''}</small>
            </div>
            <div style="margin-top:8px;color:var(--text-secondary);">${window.escapeHtml(note.content || '')}</div>
            ${note.fileUrl ? `<div class="message-file" style="background:rgba(0,0,0,0.05);padding:8px 12px;border-radius:16px;margin:8px 0;display:inline-flex;align-items:center;gap:8px;"><i class="fas fa-paperclip"></i> <a href="${note.fileUrl}" download="${note.fileName}" style="color:var(--accent);text-decoration:none;">${window.escapeHtml(note.fileName)}</a></div>` : ''}
            <div class="action-buttons" style="margin-top:8px;display:flex;gap:8px;">
                <button class="action-btn btn-delete" onclick="window.deleteNote('${note.id}')" style="background:#dc2626;color:white;border:none;padding:5px 12px;border-radius:30px;cursor:pointer;">
                    <i class="fas fa-trash"></i> حذف
                </button>
                <button class="action-btn btn-share" onclick="window.shareNote('${note.id}')" style="background:#3b82f6;color:white;border:none;padding:5px 12px;border-radius:30px;cursor:pointer;">
                    <i class="fab fa-whatsapp"></i> مشاركة
                </button>
            </div>
        </div>
    `).join('');
};

window.deleteNote = async function(id) {
    if(confirm('هل أنت متأكد من حذف هذه الملاحظة؟')) {
        try {
            await db.collection('notes').doc(id).delete();
            await window.loadNotes();
            window.showToast("🗑️ تم حذف الملاحظة");
        } catch(e) {
            window.showToast("خطأ في الحذف: " + e.message, true);
        }
    }
};

window.shareNote = function(id) {
    const note = window.notesList.find(n => n.id === id);
    if(note) {
        const msg = `📝 *ملاحظة: ${note.title}*\n\n${note.content || ''}`;
        window.open(`https://wa.me/?text=${encodeURIComponent(msg)}`, '_blank');
    }
};

window.addNewNote = async function(title, content, file) {
    try {
        let fileData = null;
        if(file) {
            window.showToast("جاري رفع الملف...");
            const url = await window.uploadToCloudinary(file, 'notes');
            fileData = { url, name: file.name };
        }
        
        await db.collection('notes').add({
            title, content,
            fileUrl: fileData?.url || null,
            fileName: fileData?.name || null,
            userId: window.currentUser.uid,
            createdAt: firebase.firestore.FieldValue.serverTimestamp()
        });
        await window.loadNotes();
        window.showToast("✅ تم إضافة الملاحظة");
    } catch(e) {
        window.showToast("خطأ: " + e.message, true);
    }
};

// ------------------- الأحداث اليومية -------------------
window.loadEvents = async function() {
    if(!window.currentUser) return;
    try {
        const snapshot = await db.collection('events')
            .where('userId', '==', window.currentUser.uid)
            .orderBy('createdAt', 'desc')
            .get();
        window.eventsList = [];
        snapshot.forEach(doc => {
            window.eventsList.push({ id: doc.id, ...doc.data() });
        });
        window.renderEventsList();
    } catch(e) {
        console.error("خطأ في تحميل الأحداث:", e);
        window.showToast("خطأ في تحميل الأحداث: " + e.message, true);
    }
};

window.renderEventsList = function() {
    const container = document.getElementById('eventsListContainer');
    if(!container) return;
    
    if(!window.eventsList.length) {
        container.innerHTML = '<div class="empty-state"><i class="fas fa-calendar-alt"></i> لا توجد أحداث مسجلة</div>';
        return;
    }
    
    container.innerHTML = window.eventsList.map(event => `
        <div class="contact-card" style="border-right-color: #3b82f6;">
            <div style="display:flex;justify-content:space-between;align-items:center;">
                <strong>📅 ${window.escapeHtml(event.title)}</strong>
                <small style="color:var(--text-secondary);font-size:10px;">${event.createdAt ? new Date(event.createdAt.toDate()).toLocaleString('ar-EG') : ''}</small>
            </div>
            <div style="margin-top:8px;color:var(--text-secondary);">${window.escapeHtml(event.description || '')}</div>
        </div>
    `).join('');
};

window.addNewEvent = async function(title, description) {
    try {
        await db.collection('events').add({
            title, description,
            userId: window.currentUser.uid,
            createdAt: firebase.firestore.FieldValue.serverTimestamp()
        });
        await window.loadEvents();
        window.showToast("✅ تم تسجيل الحدث");
    } catch(e) {
        window.showToast("خطأ: " + e.message, true);
    }
};

// ------------------- ربط الأحداث بالأزرار -------------------
document.addEventListener('DOMContentLoaded', () => {
    // إضافة ملاحظة
    const addNoteBtn = document.getElementById('addNoteBtn');
    if(addNoteBtn) {
        addNoteBtn.addEventListener('click', () => {
            document.getElementById('noteModal').classList.add('show');
        });
    }
    
    // حفظ الملاحظة
    const saveNoteBtn = document.getElementById('saveNoteModalBtn');
    if(saveNoteBtn) {
        saveNoteBtn.onclick = async () => {
            const title = document.getElementById('noteTitleInput')?.value.trim();
            if(!title) {
                window.showToast("عنوان الملاحظة مطلوب", true);
                return;
            }
            
            await window.addNewNote(
                title,
                document.getElementById('noteContentInput')?.value || '',
                window.pendingNoteFile
            );
            
            document.getElementById('noteModal')?.classList.remove('show');
            document.getElementById('noteTitleInput').value = '';
            document.getElementById('noteContentInput').value = '';
            document.getElementById('noteFileNameDisplay').innerHTML = '';
            window.pendingNoteFile = null;
            
            // التبديل إلى صفحة الملاحظات
            document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
            document.getElementById('notesPage')?.classList.add('active');
        };
    }
    
    // رفع ملف للملاحظة
    const noteFileTrigger = document.getElementById('noteFileUploadTrigger');
    if(noteFileTrigger) {
        noteFileTrigger.onclick = () => {
            const inp = document.createElement('input');
            inp.type = 'file';
            inp.onchange = e => {
                if(e.target.files[0]) {
                    window.pendingNoteFile = e.target.files[0];
                    const display = document.getElementById('noteFileNameDisplay');
                    if(display) display.innerHTML = `📎 ${window.pendingNoteFile.name}`;
                }
            };
            inp.click();
        };
    }
    
    // إضافة حدث
    const addEventBtn = document.getElementById('addEventBtn');
    if(addEventBtn) {
        addEventBtn.addEventListener('click', () => {
            document.getElementById('eventModal').classList.add('show');
        });
    }
    
    // حفظ الحدث
    const saveEventBtn = document.getElementById('saveEventModalBtn');
    if(saveEventBtn) {
        saveEventBtn.onclick = async () => {
            const title = document.getElementById('eventTitleInput')?.value.trim();
            if(!title) {
                window.showToast("عنوان الحدث مطلوب", true);
                return;
            }
            
            await window.addNewEvent(
                title,
                document.getElementById('eventDescInput')?.value || ''
            );
            
            document.getElementById('eventModal')?.classList.remove('show');
            document.getElementById('eventTitleInput').value = '';
            document.getElementById('eventDescInput').value = '';
            
            // التبديل إلى صفحة الأحداث
            document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
            document.getElementById('eventsPage')?.classList.add('active');
        };
    }
});

// متغيرات عامة للملفات المرفقة
window.pendingTaskFile = null;
window.pendingNoteFile = null;
