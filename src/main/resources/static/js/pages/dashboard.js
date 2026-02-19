const Dashboard = (() => {

    const STATUS_MAP = {
        'DONE':        { label: '완료',   css: 'done' },
        'IN_PROGRESS': { label: '진행 중', css: 'in-progress' },
        'TODO':        { label: '예정',   css: 'todo' }
    };

    let currentMonday = null;

    function getMonday(date) {
        const d = new Date(date);
        const day = d.getDay();
        d.setDate(d.getDate() - (day === 0 ? 6 : day - 1));
        return d;
    }

    function getFriday(monday) {
        const d = new Date(monday);
        d.setDate(d.getDate() + 4);
        return d;
    }

    function formatDate(date) {
        return date.toISOString().split('T')[0];
    }

    function formatLabel(monday) {
        const friday = getFriday(monday);
        const m = monday.getMonth() + 1;
        const md = monday.getDate();
        const f = friday.getMonth() + 1;
        const fd = friday.getDate();
        return monday.getFullYear() + '.' +
            String(m).padStart(2, '0') + '.' + String(md).padStart(2, '0') +
            ' ~ ' +
            friday.getFullYear() + '.' +
            String(f).padStart(2, '0') + '.' + String(fd).padStart(2, '0');
    }

    function shiftWeek(offset) {
        currentMonday.setDate(currentMonday.getDate() + (offset * 7));
        updateLabel();
        loadData();
    }

    function goCurrentWeek() {
        currentMonday = getMonday(new Date());
        updateLabel();
        loadData();
    }

    function updateLabel() {
        document.getElementById('weekLabel').textContent = formatLabel(currentMonday);
    }

    async function init() {
        currentMonday = getMonday(new Date());
        updateLabel();

        document.getElementById('btnPrevWeek').addEventListener('click', () => shiftWeek(-1));
        document.getElementById('btnNextWeek').addEventListener('click', () => shiftWeek(1));
        document.getElementById('btnCurrentWeek').addEventListener('click', goCurrentWeek);

        await loadData();
    }

    async function loadData() {
        const weekStart = formatDate(currentMonday);
        const weekEnd = formatDate(getFriday(currentMonday));

        const tbody = document.getElementById('tableBody');
        tbody.innerHTML = '<tr><td colspan="5" class="loading-cell">로딩 중...</td></tr>';

        try {
            const data = await API.dashboard.get(weekStart, weekEnd);
            document.getElementById('departmentTitle').textContent =
                data.department + ' 업무 진행 현황';
            renderTable(data.teams);
        } catch (e) {
            console.error('대시보드 로딩 실패:', e);
            tbody.innerHTML = '<tr><td colspan="5" class="empty-cell">데이터를 불러올 수 없습니다</td></tr>';
        }
    }

    function renderTable(teams) {
        const tbody = document.getElementById('tableBody');

        if (!teams || !teams.length) {
            tbody.innerHTML = '<tr><td colspan="5" class="empty-cell">등록된 데이터가 없습니다</td></tr>';
            return;
        }

        tbody.innerHTML = teams.map(member => {
            const groupHeader = '<tr class="group-header">' +
                '<td colspan="5">' +
                    '<i class="bi bi-person-fill"></i> ' +
                    Common.escapeHtml(member.username) +
                    '<span class="task-count">' + member.tasks.length + '건</span>' +
                '</td>' +
            '</tr>';

            console.log(member.tasks)

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
