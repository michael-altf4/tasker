document.addEventListener('DOMContentLoaded', function () {
    const API_URL = 'https://tasker-tlu7.onrender.com//api/todos';
    let currentTodos = [];

    function loadTodos() {
        fetch(API_URL)
            .then(response => response.json())
            .then(todos => {
                console.log('Загруженные задачи:', todos); // ← Добавлено
                currentTodos = [...todos];
                currentTodos.sort((a, b) => a.completed - b.completed);
                renderTodoList();
            })
            .catch(err => console.error('Ошибка загрузки задач:', err));
    }

    function renderTodoList() {
        const list = document.getElementById('todoList');
        list.innerHTML = '';

        currentTodos.forEach((todo, index) => {
            const li = document.createElement('li');

            const indexSpan = document.createElement('span');
            indexSpan.className = 'index';
            indexSpan.textContent = index + 1;

            const titleSpan = document.createElement('span');
            titleSpan.className = 'title';
            titleSpan.innerHTML = `<a href="/todo/${todo.id}" style="color: inherit; text-decoration: none;">${todo.title}</a>`;
            if (todo.completed) {
                titleSpan.classList.add('completed-line');
            }

            const statusSpan = document.createElement('span');
            statusSpan.className = 'status ' + (todo.completed ? 'closed' : 'active');
            statusSpan.textContent = todo.completed ? 'ЗАКРЫТО' : 'АКТИВНО';

            const controlsDiv = document.createElement('div');
            controlsDiv.className = 'controls';

            const toggleBtn = document.createElement('button');
            toggleBtn.className = 'action-btn';
            toggleBtn.textContent = todo.completed ? 'Активировать' : 'Закрыть';
            toggleBtn.onclick = () => toggleCompleted(todo.id, !todo.completed);
            controlsDiv.appendChild(toggleBtn);

            if (!todo.completed && index > 0 && !currentTodos[index - 1].completed) {
                const upBtn = document.createElement('button');
                upBtn.className = 'action-btn up';
                upBtn.innerHTML = '↑';
                upBtn.title = 'Вверх';
                upBtn.onclick = () => moveTodoUp(index);
                controlsDiv.appendChild(upBtn);
            }

            const activeTasks = currentTodos.filter(t => !t.completed);
            const isActive = !todo.completed;
            const isLastActive = isActive && (index === activeTasks.length - 1);
            if (isActive && !isLastActive) {
                const downBtn = document.createElement('button');
                downBtn.className = 'action-btn down';
                downBtn.innerHTML = '↓';
                downBtn.title = 'Вниз';
                downBtn.onclick = () => moveTodoDown(index);
                controlsDiv.appendChild(downBtn);
            }

            const deleteBtn = document.createElement('button');
            deleteBtn.className = 'action-btn delete';
            deleteBtn.textContent = '×';
            deleteBtn.title = 'Удалить';
            deleteBtn.onclick = () => deleteTodo(todo.id);
            controlsDiv.appendChild(deleteBtn);

            li.appendChild(indexSpan);
            li.appendChild(titleSpan);
            li.appendChild(statusSpan);
            li.appendChild(controlsDiv);
            list.appendChild(li);
        });
    }

    function addTodo() {
        const input = document.getElementById('title');
        const title = input.value.trim();
        if (!title) return;

        fetch(API_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ title, completed: false })
        })
            .then(response => response.json())
            .then(newTodo => {
                console.log('Новая задача:', newTodo); // ← Проверь, есть ли id

                // Убедись, что newTodo.id существует
                if (!newTodo.id) {
                    console.error('Задача не имеет ID!', newTodo);
                    alert('Ошибка: задача не была создана');
                    return;
                }

                currentTodos.push(newTodo);
                currentTodos.sort((a, b) => a.completed - b.completed);
                renderTodoList();
                input.value = '';
            })
            .catch(err => {
                console.error('Ошибка добавления задачи:', err);
                alert('Не удалось добавить задачу');
            });
    }

    function toggleCompleted(id, completed) {
        fetch(`${API_URL}/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ completed }) // ← Только статус!
        })
            .then(response => response.json())
            .then(updatedTodo => {
                const index = currentTodos.findIndex(t => t.id === id);
                if (index !== -1) {
                    // Сохраняем актуальные данные: title, description, priority
                    currentTodos[index] = updatedTodo;
                }
                // Пересортируем: активные сверху
                currentTodos.sort((a, b) => a.completed - b.completed);
                renderTodoList();
            })
            .catch(err => console.error('Ошибка обновления статуса:', err));
    }

    function deleteTodo(id) {
        fetch(`${API_URL}/${id}`, { method: 'DELETE' })
            .then(() => {
                currentTodos = currentTodos.filter(t => t.id !== id);
                renderTodoList();
            })
            .catch(err => console.error('Ошибка удаления:', err));
    }

    function moveTodoUp(index) {
        if (index > 0 && !currentTodos[index].completed && !currentTodos[index - 1].completed) {
            [currentTodos[index - 1], currentTodos[index]] = [currentTodos[index], currentTodos[index - 1]];
            renderTodoList();
        }
    }

    function moveTodoDown(index) {
        const activeTasks = currentTodos.filter(t => !t.completed);
        const isActive = !currentTodos[index].completed;
        const isLastActive = isActive && (index === activeTasks.length - 1);
        if (isActive && !isLastActive && index < currentTodos.length - 1) {
            [currentTodos[index], currentTodos[index + 1]] = [currentTodos[index + 1], currentTodos[index]];
            renderTodoList();
        }
    }

    // Инициализация
    loadTodos();

    // Экспортируем функции в глобальную область, чтобы кнопки могли их вызывать
    window.addTodo = addTodo;
});