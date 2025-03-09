//teams.js

let editingTeamId = null;

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('btnLoadTeams').addEventListener('click', loadTeams);
    document.getElementById('btnNewTeam').addEventListener('click', newTeamUI);
    document.getElementById('saveTeamBtn').addEventListener('click', saveTeam);
});

async function loadTeams() {
    try {
        let resp = await fetch("/api/teams");
        let teams = await resp.json();
        let listDiv = document.getElementById('teamsList');
        listDiv.innerHTML = '';
        teams.forEach(t => {
            let div = document.createElement('div');
            div.className = 'p-2 mb-2';
            div.style.border = '1px solid #555';
            div.innerHTML = `
        <strong>Team #${t.id}</strong> - ${t.name}, Manager=${t.managerUsername}
        <button class="btn btn-sm btn-warning" onclick="editTeam(${t.id})">Edit</button>
        <button class="btn btn-sm btn-danger" onclick="deleteTeam(${t.id})">Delete</button>
        <br/>
        Members: ${ (t.members || []).map(m => m.username).join(', ') }
      `;
            listDiv.appendChild(div);
        });
    } catch(err) {
        console.error(err);
        alert(err);
    }
}

function newTeamUI() {
    editingTeamId = null;
    document.getElementById('teamName').value = '';
    document.getElementById('teamManager').value = '';
}

async function saveTeam() {
    let name = document.getElementById('teamName').value.trim();
    let manager = document.getElementById('teamManager').value.trim();

    let payload = { name, managerUsername: manager };

    let url = '/api/teams';
    let method = 'POST';
    if (editingTeamId) {
        url = '/api/teams/' + editingTeamId;
        method = 'PUT';
    }

    try {
        let resp = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (!resp.ok) throw new Error(await resp.text());
        loadTeams();
        newTeamUI();
    } catch(err) {
        console.error(err);
        alert(err);
    }
}

async function editTeam(id) {
    try {
        let resp = await fetch('/api/teams/' + id);
        if (!resp.ok) throw new Error("Team not found");
        let t = await resp.json();
        editingTeamId = t.id;
        document.getElementById('teamName').value = t.name;
        document.getElementById('teamManager').value = t.managerUsername || '';
    } catch(err) {
        console.error(err);
        alert(err);
    }
}

async function deleteTeam(id) {
    if(!confirm("Delete team #"+id+"?")) return;
    try {
        let resp = await fetch('/api/teams/'+id, {method:'DELETE'});
        if (!resp.ok && resp.status!==204) throw new Error(await resp.text());
        loadTeams();
    } catch(err) {
        console.error(err);
        alert(err);
    }
}
