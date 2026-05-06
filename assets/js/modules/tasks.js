// ========== إدارة المهام ==========

window.tasksList = [];

window.loadTasks = async function() {
    if(!window.currentUser) return;
    try {
        const snapshot = await db.collection('tasks')
            .where('userId', '==', window.currentUser.uid)
            .orderBy('createdAt', 'desc')
            .get();
        window.tasksList = [];
        snapshot.forEach(doc => {
            window.tasksList.push({ id: doc.id, ...doc.data() });
        });
        window.renderTasksList();
        window.checkTaskReminders();
    } catch(e) {
        console.error("خطأ في تحميل المهام:", e);
        window.showToast("خطأ في تحميل المهام: " + e.message, true);
    }
};

window.renderTasksList = function() {
    const container = document.getElementById('tasksListContainer');
    if(!container) return;
    
    if(!window.tasksList.length) {
        container.innerHTML = '<div class="empty-state"><i class="fas fa-tasks"></i> لا توجد مهام</div>';
        return;
    }
    
    container.innerHTML = window.tasksList.map(task => `
        <div class="task-item" style="border-right-color: ${task.completed ? '#10b981' : '#e94560'}">
            <div class="task-header">
                <strong>${window.escapeHtml(task.title)}</strong>
                <span class="task-status ${task.completed ? 'completed' : 'pending'}">
                    ${task.completed ? '✓ مكتملة' : '⏳ قيد الانتظار'}
                </span>
            </div>
            <div style="font-size:12px;color:var(--text-secondary);margin:5px 0;">
                ${task.date ? `📅 ${task.date}` : ''} ${task.time ? `⏰ ${task.time}` : ''}
                ${task.reminderMinutes ? `<span class="reminder-badge" style="background:#f59e0b;border-radius:20px;padding:2px 8px;font-size:10px;color:white;display:inline-flex;align-items:center;gap:4px;margin-right:8px;"><i class="fas fa-bell"></i> تذكير قبل ${task.reminderMinutes} دقيقة</span>` : ''}
            </div>
            <div style="font-size:13px;margin:8px 0;">${window.escapeHtml(task.details || '')}</div>
            ${task.fileUrl ? `<div class="message-file" style="background:rgba(0,0,0,0.05);padding:8px 12px;border-radius:16px;margin:8px 0;display:inline-flex;align-items:center;gap:8px;"><i class="fas fa-paperclip"></i> <a href="${task.fileUrl}" download="${task.fileName}" style="color:var(--accent);text-decoration:none;">${window.escapeHtml(task.fileName)}</a></div>` : ''}
            <div class="action-buttons" style="margin-top:8px;display:flex;gap:8px;flex-wrap:wrap;">
                <button class="action-btn" onclick="window.toggleTaskComplete('${task.id}', ${!task.completed})" style="background:#10b981;color:white;border:none;padding:5px 12px;border-radius:30px;cursor:pointer;">
                    ${task.completed ? '↺ إعادة فتح' : '✓ إكمال'}
                </button>
                <button class="action-btn btn-delete" onclick="window.deleteTask('${task.id}')" style="background:#dc2626;color:white;border:none;padding:5px 12px;border-radius:30px;cursor:pointer;">
                    <i class="fas fa-trash"></i> حذف
                </button>
                <button class="action-btn btn-share" onclick="window.shareTask('${task.id}')" style="background:#3b82f6;color:white;border:none;padding:5px 12px;border-radius:30px;cursor:pointer;">
                    <i class="fab fa-whatsapp"></i> مشاركة
                </button>
            </div>
        </div>
    `).join('');
};

window.toggleTaskComplete = async function(id, completed) {
    try {
        await db.collection('tasks').doc(id).update({ completed });
        await window.loadTasks();
        window.showToast(completed ? "✅ تم إكمال المهمة" : "🔄 تم إعادة فتح المهمة");
    } catch(e) {
        window.showToast("خطأ: " + e.message, true);
    }
};

window.deleteTask = async function(id) {
    if(confirm('هل أنت متأكد من حذف هذه المهمة؟')) {
        try {
            await db.collection('tasks').doc(id).delete();
            await window.loadTasks();
            window.showToast("🗑️ تم حذف المهمة");
        } catch(e) {
            window.showToast("خطأ في الحذف: " + e.message, true);
        }
    }
};

window.shareTask = function(id) {
    const task = window.tasksList.find(t => t.id === id);
    if(task) {
        const msg = `📋 *مهمة: ${task.title}*\n📅 ${task.date || 'غير محدد'} ${task.time || ''}\n📝 ${task.details || ''}`;
        window.open(`https://wa.me/?text=${encodeURIComponent(msg)}`, '_blank');
    }
};

window.addNewTask = async function(title, date, time, details, file, reminderMinutes) {
    try {
        let fileData = null;
        if(file) {
            window.showToast("جاري رفع الملف...");
            const url = await window.uploadToCloudinary(file, 'tasks');
            fileData = { url, name: file.name };
        }
        
        await db.collection('tasks').add({
            title, date, time, details,
            reminderMinutes: reminderMinutes || null,
            fileUrl: fileData?.url || null,
            fileName: fileData?.name || null,
            userId: window.currentUser.uid,
            completed: false,
            reminded: false,
            createdAt: firebase.firestore.FieldValue.serverTimestamp()
        });
        await window.loadTasks();
        window.showToast("✅ تم إضافة المهمة");
    } catch(e) {
        window.showToast("خطأ: " + e.message, true);
    }
};

window.checkTaskReminders = function() {
    if(!window.tasksList) return;
    
    const now = new Date();
    window.tasksList.forEach(async task => {
        if(task.date && task.time && task.reminderMinutes && !task.reminded) {
            const taskDateTime = new Date(`${task.date}T${task.time}`);
            const diffMinutes = (taskDateTime - now) / 1000 / 60;
            
            if(diffMinutes <= task.reminderMinutes && diffMinutes > 0) {
                window.showToast(`🔔 تذكير بالمهمة: ${task.title}\n${task.details || ''}`);
                window.playNotificationBeep(880);
                await db.collection('tasks').doc(task.id).update({ reminded: true });
            }
        }
    });
};

// إضافة حدث إضافة مهمة من الزر
document.addEventListener('DOMContentLoaded', () => {
    const addTaskBtn = document.getElementById('addTaskBtn');
    if(addTaskBtn) {
        addTaskBtn.addEventListener('click', () => {
            document.getElementById('taskModal').classList.add('show');
        });
    }
    
    const saveTaskBtn = document.getElementById('saveTaskModalBtn');
    if(saveTaskBtn) {
        saveTaskBtn.onclick = async () => {
            const title = document.getElementById('taskTitleInput')?.value.trim();
            if(!title) {
                window.showToast("عنوان المهمة مطلوب", true);
                return;
            }
            
            const reminderVal = document.getElementById('taskReminderSelect')?.value;
            const reminderMinutes = reminderVal && reminderVal !== 'none' ? parseInt(reminderVal) : null;
            
            await window.addNewTask(
                title,
                document.getElementById('taskDateInput')?.value || '',
                document.getElementById('taskTimeInput')?.value || '',
                document.getElementById('taskDescInput')?.value || '',
                window.pendingTaskFile,
                reminderMinutes
            );
            
            // تنظيف النموذج
            document.getElementById('taskModal')?.classList.remove('show');
            document.getElementById('taskTitleInput').value = '';
            document.getElementById('taskDateInput').value = '';
            document.getElementById('taskTimeInput').value = '';
            document.getElementById('taskDescInput').value = '';
            document.getElementById('taskFileNameDisplay').innerHTML = '';
            window.pendingTaskFile = null;
            
            // التبديل إلى صفحة المهام
            document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
            document.getElementById('tasksPage')?.classList.add('active');
        };
    }
    
    const taskFileTrigger = document.getElementById('taskFileUploadTrigger');
    if(taskFileTrigger) {
        taskFileTrigger.onclick = () => {
            const inp = document.createElement('input');
            inp.type = 'file';
            inp.onchange = e => {
                if(e.target.files[0]) {
                    window.pendingTaskFile = e.target.files[0];
                    const display = document.getElementById('taskFileNameDisplay');
                    if(display) display.innerHTML = `📎 ${window.pendingTaskFile.name}`;
                }
            };
            inp.click();
        };
    }
});
