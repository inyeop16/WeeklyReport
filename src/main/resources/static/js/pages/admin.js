const Admin = (() => {
    'use strict';

    let templateModal;
    let deptModal;

    document.addEventListener('DOMContentLoaded', function () {
        templateModal = new bootstrap.Modal(document.getElementById('templateModal'));
        deptModal = new bootstrap.Modal(document.getElementById('deptModal'));
        loadTemplates();
        loadDepartments();
    });

    // ========== 템플릿 ==========

    async function loadTemplates() {
        const tbody = document.getElementById('templateTableBody');
        try {
            const templates = await API.templates.getAll();
            if (templates.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted py-4">등록된 템플릿이 없습니다</td></tr>';
                return;
            }
            tbody.innerHTML = templates.map(t => `
                <tr>
                    <td>${Common.escapeHtml(t.name)}</td>
                    <td>${t.department ? Common.escapeHtml(t.department) : '<span class="text-muted">전체</span>'}</td>
                    <td>${t.active
                        ? '<span class="badge bg-success">활성</span>'
                        : '<span class="badge bg-secondary">비활성</span>'}</td>
                    <td class="prompt-cell">${Common.escapeHtml(t.systemPrompt)}</td>
                    <td>
                        <div class="action-buttons">
                            <button class="btn btn-sm btn-outline-primary" onclick="Admin.editTemplate(${t.id})">
                                <i class="bi bi-pencil"></i>
                            </button>
                            <button class="btn btn-sm btn-outline-danger" onclick="Admin.deleteTemplate(${t.id}, '${Common.escapeHtml(t.name)}')">
                                <i class="bi bi-trash"></i>
                            </button>
                        </div>
                    </td>
                </tr>
            `).join('');
        } catch (e) {
            tbody.innerHTML = `<tr><td colspan="5" class="text-center text-danger py-4">${Common.escapeHtml(e.message)}</td></tr>`;
        }
    }

    function openTemplateModal(template) {
        document.getElementById('templateId').value = template ? template.id : '';
        document.getElementById('templateName').value = template ? template.name : '';
        document.getElementById('templateDept').value = template ? (template.department || '') : '';
        document.getElementById('templatePrompt').value = template ? template.systemPrompt : '';
        document.getElementById('templateActive').checked = template ? template.active : true;
        document.getElementById('templateModalTitle').textContent = template ? '템플릿 수정' : '템플릿 추가';
        templateModal.show();
    }

    async function editTemplate(id) {
        try {
            const template = await API.templates.getById(id);
            openTemplateModal(template);
        } catch (e) {
            alert('템플릿 조회 실패: ' + e.message);
        }
    }

    async function saveTemplate() {
        const id = document.getElementById('templateId').value;
        const name = document.getElementById('templateName').value.trim();
        const department = document.getElementById('templateDept').value.trim() || null;
        const systemPrompt = document.getElementById('templatePrompt').value.trim();
        const active = document.getElementById('templateActive').checked;

        if (!name || !systemPrompt) {
            alert('이름과 시스템 프롬프트는 필수입니다');
            return;
        }

        try {
            if (id) {
                await API.templates.update(Number(id), { name, department, systemPrompt, active });
            } else {
                await API.templates.create({ name, department, systemPrompt });
            }
            templateModal.hide();
            loadTemplates();
        } catch (e) {
            alert('저장 실패: ' + e.message);
        }
    }

    async function deleteTemplate(id, name) {
        if (!confirm(`"${name}" 템플릿을 삭제하시겠습니까?`)) return;
        try {
            await API.templates.delete(id);
            loadTemplates();
        } catch (e) {
            alert('삭제 실패: ' + e.message);
        }
    }

    // ========== 부서 ==========

    async function loadDepartments() {
        const tbody = document.getElementById('deptTableBody');
        try {
            const departments = await API.departments.getAll();
            if (departments.length === 0) {
                tbody.innerHTML = '<tr><td colspan="3" class="text-center text-muted py-4">등록된 부서가 없습니다</td></tr>';
                return;
            }
            tbody.innerHTML = departments.map(d => `
                <tr>
                    <td>${d.id}</td>
                    <td>${Common.escapeHtml(d.name)}</td>
                    <td>
                        <div class="action-buttons">
                            <button class="btn btn-sm btn-outline-primary" onclick="Admin.editDept(${d.id}, '${Common.escapeHtml(d.name)}')">
                                <i class="bi bi-pencil"></i>
                            </button>
                            <button class="btn btn-sm btn-outline-danger" onclick="Admin.deleteDept(${d.id}, '${Common.escapeHtml(d.name)}')">
                                <i class="bi bi-trash"></i>
                            </button>
                        </div>
                    </td>
                </tr>
            `).join('');
        } catch (e) {
            tbody.innerHTML = `<tr><td colspan="3" class="text-center text-danger py-4">${Common.escapeHtml(e.message)}</td></tr>`;
        }
    }

    function openDeptModal(dept) {
        document.getElementById('deptId').value = dept ? dept.id : '';
        document.getElementById('deptName').value = dept ? dept.name : '';
        document.getElementById('deptModalTitle').textContent = dept ? '부서 수정' : '부서 추가';
        deptModal.show();
    }

    function editDept(id, name) {
        openDeptModal({ id, name });
    }

    async function saveDept() {
        const id = document.getElementById('deptId').value;
        const name = document.getElementById('deptName').value.trim();
        if (!name) {
            alert('부서명은 필수입니다');
            return;
        }
        try {
            if (id) {
                await API.departments.update(Number(id), { name });
            } else {
                await API.departments.create({ name });
            }
            deptModal.hide();
            loadDepartments();
        } catch (e) {
            alert('저장 실패: ' + e.message);
        }
    }

    async function deleteDept(id, name) {
        if (!confirm(`"${name}" 부서를 삭제하시겠습니까?`)) return;
        try {
            await API.departments.delete(id);
            loadDepartments();
        } catch (e) {
            alert('삭제 실패: ' + e.message);
        }
    }

    return {
        openTemplateModal: () => openTemplateModal(null),
        editTemplate,
        saveTemplate,
        deleteTemplate,
        openDeptModal: () => openDeptModal(null),
        editDept,
        saveDept,
        deleteDept
    };
})();
