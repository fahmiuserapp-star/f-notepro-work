document.addEventListener('DOMContentLoaded', () => {
    // ربط الأزرار الأساسية والتبديل بين الصفحات
    // بناء منتقي الألوان، إلخ.
    window.initChat();
    // إعداد الـ intervals الخاصة بالتذكيرات بشكل واحد
    if(!window.taskReminderInterval) {
        window.taskReminderInterval = setInterval(() => {
            if(window.tasksList) window.checkTaskReminders();
        }, 30000);
    }
    window.checkReminders(); // للتذكيرات العامة المخزنة محلياً
    window.showToast("✨ النظام جاهز - يرجى تسجيل الدخول");
});
