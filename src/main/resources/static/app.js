// Глобальные переменные
let stompClient = null;     // STOMP-клиент (чат + presence)
let currentUser = "Anon";    // Имя текущего пользователя (берём из localStorage)
let jitsiApi = null;        // Ссылка на Jitsi iFrame API-объект

document.addEventListener('DOMContentLoaded', () => {
    // Проверяем, есть ли имя пользователя в localStorage
    const storedName = localStorage.getItem('username');
    if (!storedName) {
        // Если нет, значит не логинились — переходим на логин-страницу
        window.location.href = "/login.html";
        return;
    }

    currentUser = storedName;

    // Подключаемся к WebSocket/STOMP
    connectWebSocket();

    // Вешаем обработчик на кнопку "Send" (чат)
    const sendBtn = document.getElementById('sendBtn');
    sendBtn.addEventListener('click', sendChatMessage);
});

/**
 * Подключаемся к SockJS + STOMP, передаём login: currentUser
 */
function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    // Отключаем детальный лог STOMP
    stompClient.debug = null;

    // Выполняем CONNECT
    stompClient.connect({ login: currentUser }, frame => {
        console.log("STOMP connected:", frame);

        // 1) Подписка на /topic/onlineUsers (список пользователей)
        stompClient.subscribe('/topic/onlineUsers', msg => {
            console.log("Got /topic/onlineUsers:", msg.body);
            const users = JSON.parse(msg.body); // Массив или Set
            updateUserList(users);
        });

        // 2) Подписка на личные вызовы ("/user/queue/callInvites")
        stompClient.subscribe('/user/queue/callInvites', msg => {
            const invite = JSON.parse(msg.body); // { from, to, room }
            console.log("Got call invite:", invite);

            const accept = confirm(`User ${invite.from} invites you to a call. Room: ${invite.room}\nAccept?`);
            if (accept) {
                initJitsi(invite.room);
            }
        });

        // 3) Подписка на общий чат "/topic/messages"
        stompClient.subscribe('/topic/messages', msg => {
            console.log("Got /topic/messages:", msg.body);
            const chatMsg = JSON.parse(msg.body);
            addMessageToUI(chatMsg.username, chatMsg.text);
        });

    }, error => {
        console.error("STOMP error:", error);
    });
}

/**
 * Отправка сообщения (чат)
 */
function sendChatMessage() {
    const input = document.getElementById('messageInput');
    const text = input.value.trim();
    if (!text) return;

    const payload = {
        username: currentUser,
        text: text
    };
    // /app/chat.send → WebSocketChatController
    stompClient.send("/app/chat.send", {}, JSON.stringify(payload));
    input.value = "";
}

/**
 * Отображаем полученное сообщение в блоке #messageList
 */
function addMessageToUI(username, text) {
    const list = document.getElementById('messageList');
    if (!list) {
        console.warn("Element #messageList not found in HTML!");
        return;
    }

    const div = document.createElement('div');
    div.classList.add('message');
    div.innerHTML = `<strong>${username}:</strong> ${text}`;
    list.appendChild(div);
    list.scrollTop = list.scrollHeight;
}

/**
 * Обновляем список пользователей (онлайн)
 */
function updateUserList(users) {
    const userListDiv = document.getElementById('userList');
    if (!userListDiv) {
        console.warn("Element #userList not found in HTML!");
        return;
    }

    userListDiv.innerHTML = '';
    users.forEach(u => {
        if (u === currentUser) {
            // Это я
            userListDiv.innerHTML += `<div>${u} (me)</div>`;
        } else {
            // Кнопка Call
            userListDiv.innerHTML += `
                <div>${u}
                  <button onclick="callUser('${u}')">Call</button>
                </div>
            `;
        }
    });
}

/**
 * Функция "позвонить" конкретному пользователю
 */
function callUser(targetUser) {
    const roomName = "CallRoom_" + Date.now();
    // Отправляем invite: /app/call.invite
    stompClient.send("/app/call.invite", {}, JSON.stringify({
        from: currentUser,
        to: targetUser,
        room: roomName
    }));

    // Сразу входим сами в ту же комнату
    initJitsi(roomName);
}

/**
 * Инициализация Jitsi iFrame API
 */
function initJitsi(roomName) {
    const domain = "meet.jit.si"; // публичный сервер Jitsi
    if (!roomName) {
        roomName = "Room_" + Date.now();
    }

    if (jitsiApi) {
        jitsiApi.dispose();
    }
    const options = {
        roomName: roomName,
        parentNode: document.getElementById('jitsiContainer'),
        width: '100%',
        height: '400px',
        userInfo: {
            displayName: currentUser
        }
    };
    jitsiApi = new JitsiMeetExternalAPI(domain, options);
    console.log("Jitsi started in room:", roomName);
}
