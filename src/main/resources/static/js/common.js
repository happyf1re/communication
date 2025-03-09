// common.js
// Проверяем cookie-based логин
async function whoAmI() {
    const resp = await fetch("/api/employees/cookie/whoami", {
        method: "GET",
        credentials: "include"
    });
    if (!resp.ok) {
        throw new Error("Not logged in");
    }
    let username = await resp.text();
    // Сохраним в глобальной области, чтобы logout() мог знать текущего юзера
    window.currentUser = username;
    return username;
}

// Логаут
async function logout() {
    // 1) Стираем куку на бэке
    await fetch("/api/employees/cookie/logout", {
        method: "POST",
        credentials: "include"
    });

    // 2) Вызываем наш (условный) REST для разлогина в PresenceService,
    //    чтобы удалить юзера из списка совсем.
    if (window.currentUser) {
        try {
            await fetch(`/api/presence/logout/${encodeURIComponent(window.currentUser)}`, {
                method: "POST"
            });
        } catch(e) {
            console.warn("Failed to logout from presence:", e);
        }
    }

    // 3) Переходим на login.html
    window.location.href = "/login.html";
}

