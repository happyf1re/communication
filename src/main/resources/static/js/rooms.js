// rooms.js
let jitsiApi = null;
let currentUser = null;

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('createRoomBtn').addEventListener('click', createRoom);
    document.getElementById('loadRoomsBtn').addEventListener('click', loadRooms);

    whoAmI().then(user => {
        currentUser = user;
        loadRooms();
    }).catch(err => {
        console.error(err);
        // не залогинен => логин
        window.location.href = "/login.html";
    });
});

async function loadRooms() {
    try {
        let resp = await fetch("/api/rooms");
        let rooms = await resp.json();
        let listDiv = document.getElementById('roomList');
        listDiv.innerHTML = '';
        rooms.forEach(r => {
            let div = document.createElement('div');
            div.className = 'mb-2';
            div.innerHTML = `
                <span>${r}</span>
                <button class="btn btn-sm btn-success" onclick="joinRoom('${r}')">Join</button>
                <button class="btn btn-sm btn-danger" onclick="deleteRoom('${r}')">Delete</button>
            `;
            listDiv.appendChild(div);
        });
    } catch(err) {
        console.error("Failed to load rooms", err);
    }
}

async function createRoom() {
    const input = document.getElementById('roomNameInput');
    const name = input.value.trim();
    if (!name) return;
    try {
        await fetch("/api/rooms?name="+encodeURIComponent(name), { method: "POST" });
        input.value = '';
        loadRooms();
    } catch(err) {
        console.error(err);
    }
}

async function deleteRoom(name) {
    if (!confirm("Delete room " + name + "?")) return;
    try {
        await fetch("/api/rooms?name="+encodeURIComponent(name), { method: "DELETE" });
        loadRooms();
    } catch(err) {
        console.error(err);
    }
}

function joinRoom(roomName) {
    // Если уже была инициализирована jitsiApi, убираем
    if (jitsiApi) {
        jitsiApi.dispose();
    }


    const domain = "meet.jit.si";

    const options = {
        roomName: roomName,
        parentNode: document.getElementById('jitsiContainer'),
        width: '100%',
        height: '600px',
        userInfo: { displayName: currentUser },
        configOverwrite: {
            disableThirdPartyRequests: true,
            prejoinPageEnabled: false,
            disableDeepLinking: true,
        },
        interfaceConfigOverwrite: {
            SHOW_JITSI_WATERMARK: false,
            SHOW_BRAND_WATERMARK: false,
            SHOW_POWERED_BY: false,
            HIDE_DEEP_LINKING_LOGO: true,
            MOBILE_APP_PROMO: false,
            TOOLBAR_BUTTONS: [
                'microphone','camera','desktop','fullscreen',
                'fodeviceselection','hangup','chat','settings'
            ]
        }
    };
    jitsiApi = new JitsiMeetExternalAPI(domain, options);
}
