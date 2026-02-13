const Dashboard = (() => {

    const MOCK_DATA = {
        department: '플럭시티 개발팀',
        weekStart: '2026-02-09',
        weekEnd: '2026-02-13',
        teams: [
            {
                username: '김민수',
                tasks: [
                    { description: 'API 서버 인증 모듈 리팩토링', status: 'DONE', date: '2026-02-10', progress: 100 },
                    { description: 'JWT 토큰 갱신 로직 구현', status: 'DONE', date: '2026-02-11', progress: 100 },
                    { description: '사용자 권한 관리 API 설계', status: 'IN_PROGRESS', date: '2026-02-12', progress: 60 }
                ]
            },
            {
                username: '이서연',
                tasks: [
                    { description: '대시보드 UI 컴포넌트 개발', status: 'IN_PROGRESS', date: '2026-02-10', progress: 70 },
                    { description: '차트 라이브러리 연동 및 테스트', status: 'TODO', date: null, progress: null },
                    { description: '반응형 레이아웃 적용', status: 'TODO', date: null, progress: 0 }
                ]
            },
            {
                username: '박지훈',
                tasks: [
                    { description: 'PostgreSQL 쿼리 성능 튜닝', status: 'DONE', date: '2026-02-09', progress: 100 },
                    { description: 'DB 마이그레이션 스크립트 작성', status: 'IN_PROGRESS', date: '2026-02-11', progress: 40 },
                    { description: 'Redis 캐시 레이어 설계', status: 'TODO', date: null, progress: null }
                ]
            },
            {
                username: '최은지',
                tasks: [
                    { description: 'Teams 알림 연동 테스트', status: 'DONE', date: '2026-02-10', progress: 100 },
                    { description: '이메일 템플릿 디자인 수정', status: 'DONE', date: '2026-02-12', progress: 100 },
                    { description: 'CI/CD 파이프라인 개선', status: 'IN_PROGRESS', date: '2026-02-13', progress: 30 },
                    { description: null, status: null, date: null, progress: null }
                ]
            }
        ]
    };

    const STATUS_MAP = {
        'DONE':        { label: '완료',   css: 'done' },
        'IN_PROGRESS': { label: '진행 중', css: 'in-progress' },
        'TODO':        { label: '예정',   css: 'todo' }
    };

    function init() {
        const week = Common.getCurrentWeek();
        document.getElementById('weekStart').value = week.start;
        document.getElementById('weekEnd').value = week.end;
        document.getElementById('btnSearch').addEventListener('click', loadData);
        loadData();
    }

    function loadData() {
        const data = MOCK_DATA;
        document.getElementById('departmentTitle').textContent =
            data.department + ' 업무 진행 현황';

        renderTable(data.teams);
    }

    function renderTable(teams) {
        const tbody = document.getElementById('tableBody');

        if (!teams.length) {
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
