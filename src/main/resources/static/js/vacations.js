let editingVacationId = null;

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('btnLoadVacations').addEventListener('click', loadVacations);
    document.getElementById('btnNewVacation').addEventListener('click', newVacationUI);
    document.getElementById('saveVacationBtn').addEventListener('click', saveVacation);
});

async function loadVacations() {
    try {
        let resp = await fetch("/api/vacations");
        let vacs = await resp.json();
        let list = document.getElementById('vacationsList');
        list.innerHTML = '';
        vacs.forEach(v => {
            let div = document.createElement('div');
            div.className = 'p-2 mb-2';
            div.style.border = '1px solid #555';
            div.innerHTML = `
        <strong>Vacation #${v.id}</strong>: Employee=${v.employee?.id}, 
        ${v.startDate} to ${v.endDate}, status=${v.status}, comment=${v.comment || ''}
        <button class="btn btn-sm btn-warning" onclick="editVacation(${v.id})">Edit</button>
        <button class="btn btn-sm btn-danger" onclick="deleteVacation(${v.id})">Delete</button>
      `;
            list.appendChild(div);
        });
    } catch(err) {
        console.error(err);
        alert(err);
    }
}

function newVacationUI() {
    editingVacationId = null;
    document.getElementById('vacEmpId').value = '';
    document.getElementById('vacStart').value = '';
    document.getElementById('vacEnd').value = '';
    document.getElementById('vacStatus').value = 'REQUESTED';
    document.getElementById('vacComment').value = '';
}

async function saveVacation() {
    let empId   = document.getElementById('vacEmpId').value.trim();
    let start   = document.getElementById('vacStart').value;
    let end     = document.getElementById('vacEnd').value;
    let status  = document.getElementById('vacStatus').value;
    let comment = document.getElementById('vacComment').value.trim();

    let payload = {
        employee: { id: parseInt(empId) },
        startDate: start,
        endDate: end,
        status,
        comment
    };

    let url = '/api/vacations';
    let method = 'POST';
    if (editingVacationId) {
        url = '/api/vacations/'+editingVacationId;
        method = 'PUT';
    }
    try {
        let resp = await fetch(url, {
            method,
            headers: {'Content-Type':'application/json'},
            body: JSON.stringify(payload)
        });
        if (!resp.ok) throw new Error(await resp.text());
        loadVacations();
        newVacationUI();
    } catch(err) {
        console.error(err);
        alert(err);
    }
}

async function editVacation(id) {
    try {
        let resp = await fetch('/api/vacations/'+id);
        if(!resp.ok) throw new Error("Vacation not found");
        let vac = await resp.json();
        editingVacationId = vac.id;
        document.getElementById('vacEmpId').value = vac.employee?.id || '';
        document.getElementById('vacStart').value = vac.startDate;
        document.getElementById('vacEnd').value = vac.endDate;
        document.getElementById('vacStatus').value = vac.status;
        document.getElementById('vacComment').value = vac.comment || '';
    } catch(err) {
        console.error(err);
        alert(err);
    }
}

async function deleteVacation(id) {
    if(!confirm("Delete vacation #"+id+"?")) return;
    try {
        let resp = await fetch('/api/vacations/'+id, { method:'DELETE' });
        if(!resp.ok && resp.status!==204) throw new Error(await resp.text());
        loadVacations();
    } catch(err) {
        console.error(err);
        alert(err);
    }
}
