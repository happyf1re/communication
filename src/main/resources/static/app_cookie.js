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

        // Подписываемся на /topic/onlineUsers
        stompClient.subscribe('/topic/onlineUsers', msg => {
            let users = JSON.parse(msg.body);
            console.log("Got /topic/onlineUsers:", users);
            updateUserList(users);
        });

        // Подписываемся на личные приглашения
        stompClient.subscribe("/user/queue/callInvites", msg => {
            let invite = JSON.parse(msg.body);
            console.log("Call invite from:", invite.from, "for room:", invite.room);
            let ok = confirm(`User ${invite.from} invites you to a call. Accept?`);
            if (ok) {
                initJitsi(invite.room);
            }
        });

        // Подписка на общий чат
        stompClient.subscribe("/topic/messages", msg => {
            let chatMsg = JSON.parse(msg.body);
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
    if (jitsiApi) jitsiApi.dispose();

    let domain = "meet.jit.si";
    let options = {
        roomName: roomName,
        parentNode: document.getElementById('jitsiContainer'),
        width: '100%',
        height: '400px',
        userInfo: { displayName: currentUser }
    };
    jitsiApi = new JitsiMeetExternalAPI(domain, options);
    console.log("Jitsi started in room:", roomName);
}
