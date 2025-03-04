// mytasks.js
let currentUser = null;

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('btnLoadMyTasks').addEventListener('click', loadMyTasks);
});

async function loadMyTasks() {
    try {
        currentUser = await whoAmI(); // узнаём логин
        // GET /api/tasks/byAssignee/{username}
        const resp = await fetch(`/api/tasks/byAssignee/${encodeURIComponent(currentUser)}`);
        if (!resp.ok) {
            throw new Error("Failed to load tasks for user " + currentUser);
        }
        const tasks = await resp.json();
        renderTasks(tasks);
    } catch(err) {
        console.error(err);
        alert(err.message);
    }
}

function renderTasks(tasks) {
    const container = document.getElementById('tasksContainer');
    container.innerHTML = '';
    if (tasks.length === 0) {
        container.innerHTML = '<p>No tasks assigned to you.</p>';
        return;
    }
    tasks.forEach(t => {
        const div = document.createElement('div');
        div.className = 'taskItem';
        div.innerHTML = `
      <strong>Task #${t.id}</strong> - ${t.title} [${t.status}]<br/>
      Due: ${t.dueDate || '(none)'} <br/>
      <button class="btn btn-sm btn-secondary" onclick="loadComments(${t.id})">Comments</button>
      <button class="btn btn-sm btn-warning" onclick="markTaskDone(${t.id}, '${t.title}')">Mark Done</button>
      <div class="commentsBox" id="comments-${t.id}"></div>
    `;
        container.appendChild(div);
    });
}

// Загрузка комментариев задачи
async function loadComments(taskId) {
    try {
        const url = `/api/tasks/${taskId}/comments`;
        const resp = await fetch(url);
        if (!resp.ok) {
            throw new Error('Cannot load comments for task #' + taskId);
        }
        const comments = await resp.json();
        displayComments(taskId, comments);
    } catch(err) {
        alert(err.message);
    }
}

function displayComments(taskId, comments) {
    const div = document.getElementById('comments-'+taskId);
    if (!div) return;
    if (comments.length === 0) {
        div.innerHTML = '<em>No comments.</em>';
        return;
    }
    let html = '<ul>';
    comments.forEach(c => {
        html += `<li><strong>${c.authorUsername}</strong>: ${c.text}</li>`;
    });
    html += '</ul>';
    div.innerHTML = html;
}

// Пример — пометить задачу сделанной (PUT /api/tasks/{id})
async function markTaskDone(taskId, title) {
    if (!confirm(`Mark task "${title}" as DONE?`)) return;
    try {
        // Сначала получим текущую задачу
        const getResp = await fetch(`/api/tasks/${taskId}`);
        if (!getResp.ok) throw new Error("Task not found");
        let taskObj = await getResp.json();

        // Меняем статус
        taskObj.status = "DONE";

        // Отправляем PUT
        const putResp = await fetch(`/api/tasks/${taskId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(taskObj)
        });
        if (!putResp.ok) {
            let errText = await putResp.text();
            throw new Error(errText);
        }
        alert("Task marked as DONE");
        // Обновим список
        loadMyTasks();
    } catch(err) {
        alert("Error: " + err.message);
    }
}
