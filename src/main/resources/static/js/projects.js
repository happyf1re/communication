
// projects.js

// ID текущего редактируемого проекта (если null — режим создания)
let editingProjectId = null;

document.addEventListener('DOMContentLoaded', () => {
    // Кнопка загрузки списка
    document.getElementById('btnLoadProjects')
        .addEventListener('click', loadProjects);

    // Форма создания/редактирования проекта
    document.getElementById('projectForm')
        .addEventListener('submit', onSaveProject);

    // Кнопка "Cancel" (отменить редактирование)
    document.getElementById('cancelEditBtn')
        .addEventListener('click', cancelEdit);
});

//
// 1) Загрузка и отображение списка проектов
//
async function loadProjects() {
    try {
        const resp = await fetch('/api/projects');
        if (!resp.ok) {
            throw new Error('Failed to load projects');
        }
        const projects = await resp.json();
        renderProjectsList(projects);
    } catch (err) {
        alert(err.message);
        console.error(err);
    }
}

// Отрисовка всех проектов
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

      <button class="btn btn-sm btn-warning mt-2" onclick="editProject(${proj.id})">
        Edit
      </button>
      <button class="btn btn-sm btn-danger mt-2" onclick="deleteProject(${proj.id})">
        Delete
      </button>
      
      <!-- Кнопка показать/скрыть детали (участники, задачи) -->
      <button class="btn btn-sm btn-secondary mt-2" onclick="toggleDetails(${proj.id})">
        Show/Hide Details
      </button>

      <!-- Блок деталей, изначально скрыт -->
      <div class="detailsBox" id="details-${proj.id}" style="display:none;">
        <hr/>
        <!-- Участники проекта -->
        <div>
          <button class="btn btn-sm btn-info" onclick="loadParticipants(${proj.id})">
            Load Participants
          </button>
          <button class="btn btn-sm btn-success" onclick="addParticipant(${proj.id})">
            Add Participant
          </button>
          <div id="participants-${proj.id}" class="mt-2"></div>
        </div>

        <hr/>
        <!-- Задачи проекта -->
        <div>
          <button class="btn btn-sm btn-info" onclick="loadTasks(${proj.id})">
            Load Tasks
          </button>
          <button class="btn btn-sm btn-success" onclick="createTask(${proj.id})">
            Add Task
          </button>
          <div id="tasks-${proj.id}" class="mt-2"></div>
        </div>
      </div>
    `;
        container.appendChild(div);
    });
}

// Показ/скрытие «детального» блока
function toggleDetails(projectId) {
    const block = document.getElementById(`details-${projectId}`);
    if (!block) return;
    block.style.display = (block.style.display === 'none') ? 'block' : 'none';
}

//
// 2) Создание/Редактирование проекта
//
async function onSaveProject(event) {
    event.preventDefault();

    // Считываем значения полей формы
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

    // Формируем JSON
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

    // Если editingProjectId != null => обновляем существующий
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
        // Сброс формы
        cancelEdit();
        // Обновить список
        loadProjects();
    } catch (err) {
        console.error(err);
        alert(err.message);
    }
}

// Заполнить форму для редактирования
async function editProject(projectId) {
    try {
        const resp = await fetch('/api/projects/' + projectId);
        if (!resp.ok) throw new Error("Project not found (id=" + projectId + ")");
        const project = await resp.json();

        // Устанавливаем режим редактирования
        editingProjectId = projectId;

        // Заполняем поля
        document.getElementById('projectName').value = project.name;
        document.getElementById('projectManager').value = project.managerUsername;
        document.getElementById('projectStatus').value = project.status;
        document.getElementById('projectStart').value = project.startDate || '';
        document.getElementById('projectEnd').value = project.endDate || '';
        document.getElementById('projectDesc').value = project.description || '';

        // Меняем заголовок формы, показываем кнопку Cancel
        document.getElementById('formTitle').textContent = "Edit Project #" + project.id;
        document.getElementById('cancelEditBtn').style.display = 'inline-block';

    } catch (err) {
        alert(err.message);
        console.error(err);
    }
}

async function deleteProject(projectId) {
    if(!confirm("Delete project #"+projectId+"?")) return;
    try {
        const resp = await fetch('/api/projects/'+projectId, { method: 'DELETE' });
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

// Отмена редактирования
function cancelEdit() {
    editingProjectId = null;
    document.getElementById('projectForm').reset();
    document.getElementById('formTitle').textContent = "Create Project";
    document.getElementById('cancelEditBtn').style.display = 'none';
}

//
// 3) Участники (participants) проекта
//
async function loadParticipants(projectId) {
    // Загрузим сам проект, где есть поле .participants (ManyToMany)
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

//
// 4) Задачи (tasks) проекта
//
async function loadTasks(projectId) {
    // GET /api/tasks/byProject/{projectId}
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
        Due date: ${t.dueDate || '(none)'}<br/>

        <!-- Кнопка показать/скрыть комментарии -->
        <button class="btn btn-sm btn-secondary mt-2" onclick="toggleComments(${t.id})">
          Show/Hide Comments
        </button>
        <div id="comments-${t.id}" class="commentsBox" style="display:none;"></div>

        <!-- Кнопка добавить комментарий -->
        <button class="btn btn-sm btn-warning mt-2" onclick="addComment(${t.id})">
          Add Comment
        </button>
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
    const assignee = prompt("Enter assignee username (must be participant):");
    if (!assignee) return;

    // POST /api/tasks/create?projectId=... с JSON { title, assigneeUsername, status, ... }
    const payload = {
        title: title,
        assigneeUsername: assignee,
        status: "NEW"
    };

    try {
        const resp = await fetch(`/api/tasks/create?projectId=${projectId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
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

//
// 5) Комментарии к задачам
//
function toggleComments(taskId) {
    const div = document.getElementById('comments-' + taskId);
    if (!div) return;
    if (div.style.display === 'none') {
        div.style.display = 'block';
        loadComments(taskId);
    } else {
        div.style.display = 'none';
    }
}

// Загрузить комментарии (GET /api/tasks/{taskId}/comments)
async function loadComments(taskId) {
    const div = document.getElementById('comments-' + taskId);
    if (!div) return;
    div.innerHTML = '<p>Loading comments...</p>';

    try {
        const resp = await fetch(`/api/tasks/${taskId}/comments`);
        if (!resp.ok) throw new Error("Cannot load comments for task #" + taskId);
        const comments = await resp.json();

        if (comments.length === 0) {
            div.innerHTML = '<p>No comments yet.</p>';
            return;
        }
        let html = '';
        comments.forEach(c => {
            html += `
        <div class="commentItem" style="border-bottom:1px dashed #777; margin-bottom:5px;">
          <strong>${c.authorUsername}</strong>: ${c.text}
        </div>
      `;
        });
        div.innerHTML = html;
    } catch(err) {
        div.innerHTML = `<p style="color:red">${err.message}</p>`;
    }
}

// Добавить комментарий (POST /api/tasks/{taskId}/comments)
async function addComment(taskId) {
    const text = prompt("Enter your comment text:");
    if (!text) return;

    // Узнаём текущего юзера
    let user;
    try {
        user = await whoAmI();
    } catch(e) {
        alert("Not logged in");
        return;
    }

    const payload = {
        authorUsername: user,
        text: text
    };

    try {
        const resp = await fetch(`/api/tasks/${taskId}/comments`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (!resp.ok) {
            const msg = await resp.text();
            throw new Error(msg);
        }
        // Успех => перезагрузить комментарии
        loadComments(taskId);
    } catch(err) {
        alert(err.message);
    }
}



