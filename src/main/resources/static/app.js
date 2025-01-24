// Глобальная переменная, где будет STOMP-клиент
let stompClient = null;

// Глобальная переменная, где будет храниться Jitsi объект
let jitsiApi = null;

// Функция, которая вызывает SockJS + STOMP connect
function connectToChat() {
    // 1. Создаём SockJS-сокет на '/ws' (должно совпадать с серверной WebSocketConfig)
    let socket = new SockJS('/ws');

    // 2. Оборачиваем STOMP поверх SockJS
    stompClient = Stomp.over(socket);

    // 3. Опционально можно включить/выключить логирование STOMP
    // stompClient.debug = (msg) => console.log(msg); // включить
    stompClient.debug = null; // отключить лог для чистоты

    // 4. Выполняем STOMP CONNECT
    stompClient.connect({}, function(frame) {
        console.log('Connected to STOMP: ' + frame);

        // 5. Подписываемся на '/topic/messages'
        stompClient.subscribe('/topic/messages', function (message) {
            let msgBody = JSON.parse(message.body);
            addMessageToUI(msgBody.username, msgBody.text);
        });

    }, function(error) {
        console.error('STOMP error: ', error);
    });
}

// Функция для отправки сообщения (по клику кнопки)
function sendMessage() {
    let input = document.getElementById('messageInput');
    let text = input.value.trim();
    if (!text) return; // если пусто, не отправляем

    let msg = {
        username: "Me",  // или возьмите реальное имя пользователя из вашего кода
        text: text
    };

    // Отправляем на "/app/chat.send" (учитывая setApplicationDestinationPrefixes("/app"))
    stompClient.send("/app/chat.send", {}, JSON.stringify(msg));

    input.value = "";
}

// Добавляем сообщение в DOM
function addMessageToUI(user, text) {
    let list = document.getElementById('messageList');
    let div = document.createElement('div');
    div.classList.add('message');
    div.innerHTML = `<strong>${user}:</strong> ${text}`;
    list.appendChild(div);
    list.scrollTop = list.scrollHeight; // прокрутить вниз
}

// Инициализация Jitsi (iFrame)
function initJitsi() {
    // Если у вас свой сервер, замените meet.jit.si на свой домен
    const domain = "meet.jit.si";
    const roomName = "DemoRoom_" + Date.now(); // уникальное имя комнаты

    let options = {
        roomName: roomName,
        parentNode: document.getElementById('jitsiContainer'),
        width: '100%',
        height: '100%',
        userInfo: {
            displayName: "WebUser"  // отображаемое имя
        }
    };

    jitsiApi = new JitsiMeetExternalAPI(domain, options);
    console.log("Jitsi started in room:", roomName);
}

// Подключаем обработчики, когда DOM готов
document.addEventListener('DOMContentLoaded', () => {
    // Подключаемся к чату
    connectToChat();

    // Навешиваем обработчик на кнопку "Send"
    let sendBtn = document.getElementById('sendBtn');
    sendBtn.addEventListener('click', sendMessage);

    // Инициализируем Jitsi
    initJitsi();
});
