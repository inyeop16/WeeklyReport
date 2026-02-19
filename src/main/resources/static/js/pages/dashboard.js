const Dashboard = (() => {

    const STATUS_MAP = {
        'DONE':        { label: '완료',   css: 'done' },
        'IN_PROGRESS': { label: '진행 중', css: 'in-progress' },
        'TODO':        { label: '예정',   css: 'todo' }
    };

    async function init() {
        const week = Common.getCurrentWeek();
        document.getElementById('weekStart').value = week.start;
        document.getElementById('weekEnd').value = week.end;
        document.getElementById('btnSearch').addEventListener('click', loadData);

        await loadDepartments();
        await loadData();
    }

    async function loadDepartments() {
        try {
            const departments = await API.departments.getAll();
            const select = document.getElementById('departmentSelect');
            departments.forEach(dept => {
                const option = document.createElement('option');
                option.value = dept.id;
                option.textContent = dept.name;
                select.appendChild(option);
            });
            if (departments.length > 0) {
                select.value = departments[0].id;
            }
        } catch (e) {
            console.error('부서 목록 로딩 실패:', e);
        }
    }

    async function loadData() {
        const departmentId = document.getElementById('departmentSelect').value;
        const weekStart = document.getElementById('weekStart').value;
        const weekEnd = document.getElementById('weekEnd').value;

        if (!departmentId || !weekStart || !weekEnd) {
            return;
        }

        const tbody = document.getElementById('tableBody');
        tbody.innerHTML = '<tr><td colspan="4" class="loading-cell">로딩 중...</td></tr>';

        try {
            const data = await API.dashboard.get(departmentId, weekStart, weekEnd);
            document.getElementById('departmentTitle').textContent =
                data.department + ' 업무 진행 현황';
            renderTable(data.teams);
        } catch (e) {
            console.error('대시보드 로딩 실패:', e);
            tbody.innerHTML = '<tr><td colspan="4" class="empty-cell">데이터를 불러올 수 없습니다</td></tr>';
        }
    }

    function renderTable(teams) {
        const tbody = document.getElementById('tableBody');

        if (!teams || !teams.length) {
            tbody.innerHTML = '<tr><td colspan="4" class="empty-cell">등록된 데이터가 없습니다</td></tr>';
            return;
        }

        tbody.innerHTML = teams.map(member => {
            const groupHeader = '<tr class="group-header">' +
                '<td colspan="4">' +
                    '<i class="bi bi-person-fill"></i> ' +
                    Common.escapeHtml(member.username) +
                    '<span class="task-count">' + member.tasks.length + '건</span>' +
                '</td>' +
            '</tr>';

            const taskRows = member.tasks.map(task =>
                '<tr>' +
                    '<td>' + renderText(task.project) + '</td>' +
                    '<td>' + renderText(task.description) + '</td>' +
                    '<td>' + renderStatus(task.status) + '</td>' +
                    '<td>' + renderText(task.date) + '</td>' +
                    '<td>' + renderProgress(task.progress) + '</td>' +
                '</tr>'
            ).join('');

            return groupHeader + taskRows;
        }).join('');
    }

    function renderText(value) {
        if (value == null || value === '') {
            return '<span class="muted-dash">-</span>';
        }
        return Common.escapeHtml(String(value));
    }

    function renderStatus(status) {
        if (status == null || status === '') {
            return '<span class="muted-dash">-</span>';
        }
        const info = STATUS_MAP[status];
        if (!info) {
            return Common.escapeHtml(status);
        }
        return '<span class="status-label">' +
                   '<span class="status-dot ' + info.css + '"></span>' +
                   Common.escapeHtml(info.label) +
               '</span>';
    }

    function renderProgress(progress) {
        if (progress == null) {
            return '<span class="muted-dash">-</span>';
        }
        const pct = Math.max(0, Math.min(100, progress));
        const color = pct === 100 ? 'var(--success)'
                    : pct >= 50  ? 'var(--warning)'
                    :              'var(--text-muted)';
        return '<div class="progress-cell">' +
                   '<div class="progress-track">' +
                       '<div class="progress-fill" style="width:' + pct + '%;background:' + color + '"></div>' +
                   '</div>' +
                   '<span class="progress-pct">' + pct + '%</span>' +
               '</div>';
    }

    document.addEventListener('DOMContentLoaded', init);

    return { loadData };
})();
