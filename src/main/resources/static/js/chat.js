// chat.js
let stompClient = null;
let currentUser = null;

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('sendBtn').addEventListener('click', sendChat);

    // 1) Узнаём, кто залогинен
    whoAmI().then(user => {
        currentUser = user;
        connectStomp();
    }).catch(err => {
        console.error(err);
        // если не залогинен — на login
        window.location.href = "/login.html";
    });
});

function connectStomp() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    // Отключим подробный лог в консоль:
    stompClient.debug = null;

    // Передаём login=currentUser (используется в UserInterceptor на бэке).
    stompClient.connect({ login: currentUser }, frame => {
        console.log("STOMP connected", frame);

        // 1) Подписка на общие чат-сообщения
        stompClient.subscribe('/topic/messages', msg => {
            const body = JSON.parse(msg.body);
            addMessage(body.username, body.text);
        });

        // 2) Подписка на список (username+status) пользователей
        stompClient.subscribe('/topic/onlineUsers', msg => {
            const list = JSON.parse(msg.body);
            updateOnlineUsers(list);
        });

        // 3) Сразу же запрашиваем актуальный список онлайн
        stompClient.send('/app/presence.getOnlineUsers', {}, {});

    }, err => {
        console.error("STOMP error:", err);
    });
}

function sendChat() {
    if (!stompClient) return;
    const input = document.getElementById('messageInput');
    const txt = input.value.trim();
    if (!txt) return;

    stompClient.send('/app/chat.send', {}, JSON.stringify({
        username: currentUser,
        text: txt
    }));
    input.value = '';
}

function addMessage(user, text) {
    const div = document.createElement('div');
    div.className = 'message';
    div.innerHTML = `<strong>${user}:</strong> ${text}`;
    const list = document.getElementById('messageList');
    list.appendChild(div);
    list.scrollTop = list.scrollHeight;
}

// Обновлённая функция: usersArray — массив объектов { username, status }
function updateOnlineUsers(usersArray) {
    const div = document.getElementById('onlineUsers');
    div.innerHTML = '';

    if (!usersArray || usersArray.length === 0) {
        div.textContent = 'No one is online.';
        return;
    }

    const ul = document.createElement('ul');
    usersArray.forEach(u => {
        let li = document.createElement('li');
        // Покажем: username [ONLINE / SLEEP]
        li.textContent = `${u.username} [${u.status}]`;
        ul.appendChild(li);
    });
    div.appendChild(ul);
}
