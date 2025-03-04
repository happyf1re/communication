
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
    return username;
}

// Логаут
async function logout() {
    await fetch("/api/employees/cookie/logout", {
        method: "POST",
        credentials: "include"
    });
    window.location.href = "/login.html";
}
