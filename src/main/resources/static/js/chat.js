// chat.js
let stompClient = null;
let currentUser = null;

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('sendBtn').addEventListener('click', sendChat);

    // 1) Who am I
    whoAmI().then(user => {
        currentUser = user;
        // 2) Connect STOMP
        connectStomp();
    }).catch(err => {
        console.error(err);
        // редирект на логин
        window.location.href = "/login.html";
    });
});

function connectStomp() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    // Disable debug log for clarity
    stompClient.debug = null;

    // Передаём login=currentUser (у вас в UserInterceptor прописано).
    stompClient.connect({ login: currentUser }, frame => {
        console.log("STOMP connected", frame);

        // 1) Подписка на общие сообщения
        stompClient.subscribe('/topic/messages', msg => {
            const body = JSON.parse(msg.body);
            addMessage(body.username, body.text);
        });

        // 2) Подписка на список онлайн-пользователей
        stompClient.subscribe('/topic/onlineUsers', msg => {
            const users = JSON.parse(msg.body);
            updateOnlineUsers(users);
        });

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

// Функция для обновления списка онлайн
function updateOnlineUsers(usersArray) {
    const div = document.getElementById('onlineUsers');
    div.innerHTML = '';
    if (usersArray.length === 0) {
        div.textContent = 'No one is online.';
        return;
    }
    const ul = document.createElement('ul');
    usersArray.forEach(u => {
        let li = document.createElement('li');
        li.textContent = u;
        ul.appendChild(li);
    });
    div.appendChild(ul);
}
