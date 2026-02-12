const TeamReport = (() => {
    'use strict';

    let currentReportId = null;

    document.addEventListener('DOMContentLoaded', function () {
        loadDepartments();
        setDefaultDates();
    });

    async function loadDepartments() {
        const select = document.getElementById('departmentSelect');
        try {
            const departments = await API.departments.getAll();
            departments.forEach(d => {
                const option = document.createElement('option');
                option.value = d.id;
                option.textContent = d.name;
                select.appendChild(option);
            });
        } catch (e) {
            console.error('부서 목록 로드 실패:', e);
        }
    }

    function setDefaultDates() {
        const { start, end } = Common.getCurrentWeek();
        document.getElementById('weekStart').value = start;
        document.getElementById('weekEnd').value = end;
    }

    async function generate() {
        const departmentId = document.getElementById('departmentSelect').value;
        const weekStart = document.getElementById('weekStart').value;
        const weekEnd = document.getElementById('weekEnd').value;

        if (!departmentId) {
            alert('부서를 선택하세요');
            return;
        }
        if (!weekStart || !weekEnd) {
            alert('기간을 선택하세요');
            return;
        }

        showLoading(true);
        hideResult();

        try {
            const report = await API.reports.generateTeam({
                departmentId: Number(departmentId),
                weekStart,
                weekEnd
            });
            currentReportId = report.id;
            showResult(report);
        } catch (e) {
            alert('생성 실패: ' + e.message);
        } finally {
            showLoading(false);
        }
    }

    function showResult(report) {
        const resultArea = document.getElementById('resultArea');
        const content = document.getElementById('reportContent');
        const title = document.getElementById('resultTitle');

        title.innerHTML = `<i class="bi bi-file-earmark-text"></i> 팀 통합보고서 (${Common.escapeHtml(report.weekStart)} ~ ${Common.escapeHtml(report.weekEnd)})`;
        content.innerHTML = formatReport(report.rendered || '');
        resultArea.style.display = 'block';
        closeEdit();
    }

    function formatReport(text) {
        return marked.parse(text);
    }

    function hideResult() {
        document.getElementById('resultArea').style.display = 'none';
    }

    function showLoading(show) {
        document.getElementById('loadingArea').style.display = show ? 'block' : 'none';
        document.getElementById('generateBtn').disabled = show;
    }

    function openEdit() {
        document.getElementById('editArea').style.display = 'block';
        document.getElementById('reportActions').style.display = 'none';
        document.getElementById('editInstruction').focus();
    }

    function closeEdit() {
        document.getElementById('editArea').style.display = 'none';
        document.getElementById('reportActions').style.display = 'block';
        document.getElementById('editInstruction').value = '';
    }

    async function submitEdit() {
        const instruction = document.getElementById('editInstruction').value.trim();
        if (!instruction) {
            alert('수정 지시사항을 입력하세요');
            return;
        }
        if (!currentReportId) {
            alert('수정할 보고서가 없습니다');
            return;
        }

        showLoading(true);

        try {
            const report = await API.reports.update(currentReportId, instruction);
            showResult(report);
        } catch (e) {
            alert('수정 실패: ' + e.message);
        } finally {
            showLoading(false);
        }
    }

    return {
        generate,
        openEdit,
        closeEdit,
        submitEdit
    };
})();
