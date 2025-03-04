
let editingProjectId = null;

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('btnLoadProjects').addEventListener('click', loadProjects);
    // Форма создания/редактирования
    document.getElementById('projectForm').addEventListener('submit', onSaveProject);
    document.getElementById('cancelEditBtn').addEventListener('click', cancelEdit);
});

// Загрузка всех проектов
async function loadProjects() {
    try {
        const resp = await fetch('/api/projects');
        if (!resp.ok) throw new Error('Failed to load projects');
        const projects = await resp.json();
        renderProjectsList(projects);
    } catch (err) {
        alert(err.message);
        console.error(err);
    }
}

// Отрисовка списка
function renderProjectsList(projects) {
    const container = document.getElementById('projectsList');
    container.innerHTML = '';

    if (projects.length === 0) {
        container.innerHTML = '<p>No projects found.</p>';
        return;
    }

    projects.forEach(proj => {
        const div = document.createElement('div');
        div.className = 'project-item';
        div.innerHTML = `
      <h5>Project #${proj.id} – ${proj.name}</h5>
      <div>Manager: ${proj.managerUsername}</div>
      <div>Status: ${proj.status}</div>
      <div>Start: ${proj.startDate || '(none)'} | End: ${proj.endDate || '(none)'}</div>
      <div>Description: ${proj.description || ''}</div>

      <button class="btn btn-sm btn-warning mt-2" onclick="editProject(${proj.id})">Edit</button>
      <button class="btn btn-sm btn-danger mt-2" onclick="deleteProject(${proj.id})">Delete</button>
      
      <!-- Новый блок -->
      <button class="btn btn-sm btn-secondary mt-2" onclick="toggleDetails(${proj.id})">
        Show/Hide Details
      </button>

      <!-- Контейнер, куда загрузим "детальную" информацию (участники, задачи) -->
      <div class="detailsBox" id="details-${proj.id}" style="display:none;">
        <!-- Кнопки для участников -->
        <div>
          <button class="btn btn-sm btn-info" onclick="loadParticipants(${proj.id})">Load Participants</button>
          <button class="btn btn-sm btn-success" onclick="addParticipant(${proj.id})">Add Participant</button>
          <div id="participants-${proj.id}" class="mt-2"></div>
        </div>
        <hr/>
        <!-- Кнопки для задач -->
        <div>
          <button class="btn btn-sm btn-info" onclick="loadTasks(${proj.id})">Load Tasks</button>
          <button class="btn btn-sm btn-success" onclick="createTask(${proj.id})">Add Task</button>
          <div id="tasks-${proj.id}" class="mt-2"></div>
        </div>
      </div>
    `;
        container.appendChild(div);
    });
}

// Показ/скрытие блока деталей
function toggleDetails(projectId) {
    const block = document.getElementById(`details-${projectId}`);
    if (!block) return;
    block.style.display = (block.style.display === 'none') ? 'block' : 'none';
}

// ====== Участники проекта ======

async function loadParticipants(projectId) {
    // Предположим, у вас в контроллере нет метода "GET /api/projects/{id}/participants"
    // Но есть поле .participants в самом Project, и при GET /api/projects/{id} оно уже приходит.
    // Или вы можете сделать отдельный эндпоинт.
    // Ниже - вариант "берём полный объект Project" еще раз:
    try {
        const resp = await fetch(`/api/projects/${projectId}`);
        if (!resp.ok) throw new Error("Project not found");
        const project = await resp.json();
        const participantsDiv = document.getElementById(`participants-${projectId}`);
        participantsDiv.innerHTML = '';

        if (!project.participants || project.participants.length === 0) {
            participantsDiv.innerHTML = '<p>No participants yet.</p>';
            return;
        }

        project.participants.forEach(emp => {
            const pDiv = document.createElement('div');
            pDiv.className = 'participantItem';
            pDiv.textContent = `#${emp.id} ${emp.username} [${emp.role}]`;
            participantsDiv.appendChild(pDiv);
        });
    } catch(err) {
        alert(err.message);
    }
}

async function addParticipant(projectId) {
    const empId = prompt("Enter Employee ID to add:");
    if (!empId) return;
    try {
        // Ваш контроллер: POST /api/projects/{projectId}/addParticipant/{employeeId}
        const url = `/api/projects/${projectId}/addParticipant/${empId}`;
        const resp = await fetch(url, { method: 'POST' });
        if (!resp.ok) {
            const text = await resp.text();
            throw new Error(text);
        }
        alert("Participant added!");
        loadParticipants(projectId);
    } catch(err) {
        alert(err.message);
    }
}

// ====== Задачи проекта ======

async function loadTasks(projectId) {
    // Предположим, есть эндпоинт GET /api/tasks/byProject/{projectId}
    // (см. TaskController)
    try {
        const resp = await fetch(`/api/tasks/byProject/${projectId}`);
        if (!resp.ok) throw new Error("Cannot load tasks for project #" + projectId);
        const tasks = await resp.json();

        const tasksDiv = document.getElementById(`tasks-${projectId}`);
        tasksDiv.innerHTML = '';

        if (tasks.length === 0) {
            tasksDiv.innerHTML = '<p>No tasks yet.</p>';
            return;
        }

        tasks.forEach(t => {
            const taskDiv = document.createElement('div');
            taskDiv.className = 'taskItem';
            taskDiv.innerHTML = `
        <strong>Task #${t.id}</strong> - ${t.title} [${t.status}]<br/>
        Assignee: ${t.assigneeUsername || '(none)'}<br/>
        Due date: ${t.dueDate || '(none)'}
      `;
            tasksDiv.appendChild(taskDiv);
        });
    } catch(err) {
        alert(err.message);
    }
}

async function createTask(projectId) {
    const title = prompt("Enter task title:");
    if (!title) return;
    const assignee = prompt("Enter assignee username (must be project participant):");
    if (!assignee) return;

    // POST /api/tasks/create?projectId=...  + JSON { title, assigneeUsername, status, etc. }
    const payload = {
        title: title,
        assigneeUsername: assignee,
        status: "NEW"
    };

    try {
        const resp = await fetch(`/api/tasks/create?projectId=${projectId}`, {
            method: 'POST',
            headers: {'Content-Type':'application/json'},
            body: JSON.stringify(payload)
        });
        if (!resp.ok) {
            const text = await resp.text();
            throw new Error(text);
        }
        alert("Task created!");
        loadTasks(projectId);
    } catch(err) {
        alert(err.message);
    }
}

// ====== CRUD Projects (как раньше) ======
async function onSaveProject(event) {
    event.preventDefault();

    const name = document.getElementById('projectName').value.trim();
    const managerUsername = document.getElementById('projectManager').value.trim();
    const status = document.getElementById('projectStatus').value;
    const startDate = document.getElementById('projectStart').value || null;
    const endDate = document.getElementById('projectEnd').value || null;
    const description = document.getElementById('projectDesc').value.trim();

    if (!name || !managerUsername) {
        alert("Name and ManagerUsername are required!");
        return;
    }

    const payload = {
        name,
        managerUsername,
        status,
        startDate,
        endDate,
        description
    };

    let url = '/api/projects';
    let method = 'POST';
    if (editingProjectId) {
        url += '/' + editingProjectId;
        method = 'PUT';
    }

    try {
        const resp = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (!resp.ok) {
            const text = await resp.text();
            throw new Error(text);
        }
        alert('Project saved successfully!');
        cancelEdit();
        loadProjects();
    } catch (err) {
        console.error(err);
        alert(err.message);
    }
}

async function editProject(id) {
    try {
        const resp = await fetch('/api/projects/' + id);
        if (!resp.ok) throw new Error("Project not found (id=" + id + ")");
        const project = await resp.json();

        editingProjectId = id;
        document.getElementById('projectName').value = project.name;
        document.getElementById('projectManager').value = project.managerUsername;
        document.getElementById('projectStatus').value = project.status;
        document.getElementById('projectStart').value = project.startDate || '';
        document.getElementById('projectEnd').value = project.endDate || '';
        document.getElementById('projectDesc').value = project.description || '';

        document.getElementById('formTitle').textContent = "Edit Project #" + project.id;
        document.getElementById('cancelEditBtn').style.display = 'inline-block';
    } catch (err) {
        alert(err.message);
        console.error(err);
    }
}

async function deleteProject(id) {
    if(!confirm("Delete project #"+id+"?")) return;
    try {
        const resp = await fetch('/api/projects/'+id, { method: 'DELETE' });
        if(!resp.ok && resp.status!==204) {
            const text = await resp.text();
            throw new Error(text);
        }
        alert("Project deleted");
        loadProjects();
    } catch (err) {
        alert(err.message);
        console.error(err);
    }
}

function cancelEdit() {
    editingProjectId = null;
    document.getElementById('projectForm').reset();
    document.getElementById('formTitle').textContent = "Create Project";
    document.getElementById('cancelEditBtn').style.display = 'none';
}


