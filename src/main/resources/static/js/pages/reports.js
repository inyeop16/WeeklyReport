/**
 * Reports Page Logic - Migrated from chat.html
 */

(function() {
    'use strict';

    const chat = document.getElementById('chat');
    const input = document.getElementById('input');

    // Initialize on page load
    document.addEventListener('DOMContentLoaded', function() {
        input.addEventListener('input', function() {
            this.style.height = '48px';
            this.style.height = Math.min(this.scrollHeight, 120) + 'px';
        });

        addMsg(
            '안녕하세요! 주간보고 생성기입니다.\n\n' +
            '1. 위에서 사용자와 템플릿을 선택하세요\n' +
            '2. 아래 입력창에 오늘 업무 내용을 입력하세요\n' +
            '3. "주간보고 생성" 버튼으로 AI 보고서를 만들 수 있습니다',
            'bot',
            '시스템'
        );
    });

    /**
     * Add message to chat
     */
    function addMsg(text, type = 'bot', label = '') {
        const div = document.createElement('div');
        div.className = `msg ${type}`;
        if (label && type === 'bot') {
            div.innerHTML = `<div class="label">${label}</div>${Common.escapeHtml(text)}`;
        } else {
            div.textContent = text;
        }
        chat.appendChild(div);
        chat.scrollTop = chat.scrollHeight;
        return div;
    }

    /**
     * Add HTML message to chat
     */
    function addHtmlMsg(html, label = '') {
        const div = document.createElement('div');
        div.className = 'msg bot';
        div.innerHTML = (label ? `<div class="label">${label}</div>` : '') + html;
        chat.appendChild(div);
        chat.scrollTop = chat.scrollHeight;
    }

    /**
     * Show typing indicator
     */
    function showTyping() {
        const div = document.createElement('div');
        div.className = 'typing';
        div.id = 'typing';
        div.textContent = '생성 중';
        chat.appendChild(div);
        chat.scrollTop = chat.scrollHeight;
    }

    /**
     * Hide typing indicator
     */
    function hideTyping() {
        document.getElementById('typing')?.remove();
    }

    /**
     * Handle action buttons
     */
    window.doAction = async function(action) {
        if (action === 'generate') {
            const { start, end } = Common.getCurrentWeek();

            addMsg(`주간보고 생성 요청 (${start} ~ ${end})`, 'user');
            showTyping();

            try {
                const report = await API.reports.generate({
                    weekStart: start,
                    weekEnd: end
                });
                hideTyping();
                showReport(report);
            } catch (e) {
                hideTyping();
                addMsg(e.message, 'bot error');
            }

        }
    };

    /**
     * Show report in chat
     */
    function showReport(report) {
        const id = report.id;
        addHtmlMsg(
            `<div class="report-text" id="report-text-${id}">${Common.escapeHtml(report.rendered || '')}</div>` +
            `<div class="report-actions" id="report-actions-${id}">` +
            `<button onclick="editReport(${id})"><i class="bi bi-pencil"></i> 수정</button>` +
            `</div>`,
            `주간보고 #${id} (${report.weekStart} ~ ${report.weekEnd})`
        );
    }

    /**
     * View existing report
     */
    window.viewReport = async function(id) {
        try {
            const report = await API.reports.getById(id);
            showReport(report);
        } catch (e) {
            addMsg(e.message, 'bot error');
        }
    };

    /**
     * Edit report
     */
    window.editReport = function(id) {
        const actionsEl = document.getElementById(`report-actions-${id}`);
        if (!actionsEl) return;

        actionsEl.innerHTML =
            `<div class="edit-area">` +
            `<textarea id="edit-input-${id}" placeholder="수정 지시사항을 입력하세요 (예: 좀 더 간결하게 해줘, 성과 부분을 강조해줘)"></textarea>` +
            `</div>` +
            `<div class="report-actions" style="margin-top:6px">` +
            `<button onclick="saveReport(${id})"><i class="bi bi-check"></i> AI 수정 요청</button>` +
            `<button onclick="cancelEdit(${id})"><i class="bi bi-x"></i> 취소</button>` +
            `</div>`;

        document.getElementById(`edit-input-${id}`).focus();
    };

    /**
     * Cancel edit
     */
    window.cancelEdit = function(id) {
        const actionsEl = document.getElementById(`report-actions-${id}`);
        if (!actionsEl) return;
        actionsEl.innerHTML = `<button onclick="editReport(${id})"><i class="bi bi-pencil"></i> 수정</button>`;
    };

    /**
     * Save report with AI modification
     */
    window.saveReport = async function(id) {
        const ta = document.getElementById(`edit-input-${id}`);
        if (!ta) return;

        const instruction = ta.value.trim();
        if (!instruction) {
            return addMsg('수정 지시사항을 입력하세요.', 'bot');
        }

        addMsg(instruction, 'user');
        cancelEdit(id);
        showTyping();

        try {
            const report = await API.reports.update(id, instruction);
            hideTyping();
            showReport(report);
        } catch (e) {
            hideTyping();
            addMsg(e.message, 'bot error');
        }
    };

    /**
     * Send daily entry
     */
    window.sendEntry = async function() {
        const text = input.value.trim();
        if (!text) return;

        addMsg(text, 'user');
        input.value = '';
        input.style.height = '48px';

        try {
            const entry = await API.entries.create({
                entryDate: Common.getToday(),
                content: text
            });
            addMsg(`업무기록 저장 완료 (${entry.entryDate})`, 'bot', '기록됨');
        } catch (e) {
            addMsg(e.message, 'bot error');
        }
    };

    /**
     * Handle keyboard input
     */
    window.handleKey = function(e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendEntry();
        }
    };
})();
