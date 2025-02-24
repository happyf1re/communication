// Глобальные переменные
let stompClient = null;
let currentUser = null; // будем узнавать у бэкенда
let jitsiApi = null;

document.addEventListener('DOMContentLoaded', () => {
    // Сначала спрашиваем бэкенд: "Кто я?"
    whoAmI()
        .then(name => {
            currentUser = name;
            console.log("Identified as:", currentUser);
            // Теперь подключаемся к STOMP
            connectWebSocket();
        })
        .catch(err => {
            console.log("Not logged in:", err);
            // Отправляем на login.html
            window.location.href = "/login.html";
        });

    // Кнопка "Send" для чата
    const sendBtn = document.getElementById('sendBtn');
    sendBtn.addEventListener('click', sendChatMessage);
});

/**
 * Запрашивает /api/employees/cookie/whoami
 * Если 200 OK -> возвращает имя, иначе бросаем ошибку
 */
async function whoAmI() {
    const resp = await fetch("/api/employees/cookie/whoami", {
        method: "GET",
        credentials: "include"
    });
    if (resp.ok) {
        return await resp.text(); // строка с именем
    } else {
        throw new Error("Not authorized");
    }
}

/**
 * Подключаемся к /ws через SockJS, передаём login=currentUser
 */
function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    // Отключаем лишний лог
    stompClient.debug = null;

    // Выполняем CONNECT
    stompClient.connect({ login: currentUser }, frame => {
        console.log("STOMP connected as", currentUser, frame);
        console.log("STOMP connected", frame);

        // Подписываемся на /topic/onlineUsers
        stompClient.subscribe('/topic/onlineUsers', msg => {
            let users = JSON.parse(msg.body);
            console.log("Got /topic/onlineUsers:", users);
            updateUserList(users);
        });

        // Подписываемся на личные приглашения
        stompClient.subscribe("/user/queue/callInvites", msg => {
            let invite = JSON.parse(msg.body);
            console.log("[STOMP] Incoming call:", invite);
            let ok = confirm(`User ${invite.from} invites you to a call. Accept?`);
            if (ok) {
                initJitsi(invite.room);
            }
        });

        // Подписка на общий чат
        stompClient.subscribe("/topic/messages", msg => {
            let chatMsg = JSON.parse(msg.body);
            console.log("[STOMP] /topic/messages =>", chatMsg);
            addMessageToUI(chatMsg.username, chatMsg.text);
        });

    }, err => {
        console.error("STOMP error:", err);
    });
}

/**
 * Отправка сообщения (чат)
 */
function sendChatMessage() {
    const input = document.getElementById('messageInput');
    const text = input.value.trim();
    if (!text) return;

    // /app/chat.send
    stompClient.send("/app/chat.send", {}, JSON.stringify({
        username: currentUser,
        text: text
    }));
    input.value = "";
}

/**
 * Выводим сообщение в #messageList
 */
function addMessageToUI(user, text) {
    let list = document.getElementById('messageList');
    if (!list) return;
    let div = document.createElement('div');
    div.classList.add('message');
    div.innerHTML = `<strong>${user}:</strong> ${text}`;
    list.appendChild(div);
    list.scrollTop = list.scrollHeight;
}

/**
 * Обновляем список онлайн-пользователей
 */
function updateUserList(users) {
    console.log("[UPDATE] userList =>", users);
    let userListDiv = document.getElementById('userList');
    if (!userListDiv) return;
    userListDiv.innerHTML = '';

    users.forEach(u => {
        if (u === currentUser) {
            userListDiv.innerHTML += `<div>${u} (me)</div>`;
        } else {
            userListDiv.innerHTML += `<div>${u}
                <button onclick="callUser('${u}')">Call</button>
            </div>`;
        }
    });
}

function callUser(targetUser) {
    const roomName = "Room_" + Date.now();
    console.log("[CALL] callUser => from:", currentUser, "to:", targetUser, "room:", roomName);
    stompClient.send("/app/call.invite", {}, JSON.stringify({
        from: currentUser,
        to: targetUser,
        room: roomName
    }));
    // сам тоже заходим
    initJitsi(roomName);
}

/**
 * Инициализация Jitsi
 */
function initJitsi(roomName) {
    console.log("[JITSI] init => room:", roomName);
    // Если roomName не передали, генерируем
    if (!roomName) roomName = "Room_" + Date.now();

    // Если уже есть открыт Jitsi, "сносим" его
    if (jitsiApi) {
        jitsiApi.dispose();
    }

    const domain = "meet.jit.si";
    const options = {
        roomName: roomName,
        parentNode: document.getElementById('jitsiContainer'),
        width: '100%',
        height: '600px', // Или 400px, как удобнее
        userInfo: { displayName: currentUser },

        // ВАЖНО: отключаем сторонние запросы (Google Drive и т.д.)
        configOverwrite: {
            disableThirdPartyRequests: true,
            // Убираем пред-join пэйдж
            prejoinPageEnabled: false,
            // Не предлагаем открыть мобильное приложение
            disableDeepLinking: true,
            // Дополнительно отключаем "lobby" и "Are you the host?"
            enableLobbyChat: false,
            enableUserRolesBasedOnToken: false
        },
        interfaceConfigOverwrite: {
            // Убираем логотипы и "powered by"
            SHOW_JITSI_WATERMARK: false,
            SHOW_BRAND_WATERMARK: false,
            SHOW_POWERED_BY: false,
            HIDE_DEEP_LINKING_LOGO: true,
            MOBILE_APP_PROMO: false,

            // Минимально нужные кнопки
            TOOLBAR_BUTTONS: [
                'microphone','camera','desktop','fullscreen',
                'fodeviceselection','hangup','chat','settings'
            ]
        }
    };

    // Создаём «встроенную» конфу Jitsi
    jitsiApi = new JitsiMeetExternalAPI(domain, options);

    console.log("[JITSI] Started in room:", roomName);
}
