/**
 * Reports Page Logic
 */

(function() {
    'use strict';

    const chat = document.getElementById('chat');
    const input = document.getElementById('input');

    // Current week context
    let currentWeek = Common.getCurrentWeek();

    // Unique DOM ID counter (avoids duplicate IDs when same report is shown multiple times)
    let msgSeq = 0;

    // Initialize on page load
    document.addEventListener('DOMContentLoaded', function() {
        input.addEventListener('input', function() {
            this.style.height = '48px';
            this.style.height = Math.min(this.scrollHeight, 120) + 'px';
        });

        addMsg(
            '안녕하세요! 주간보고 생성기입니다.\n\n' +
            '1. 아래 입력창에 오늘 업무 내용을 입력하세요\n' +
            '2. "주간보고 생성" 버튼으로 AI 보고서를 만들 수 있습니다\n' +
            '3. 생성된 내용을 확인하고, "수정" 버튼으로 보완할 점을 입력해 AI에게 다시 요청하세요.\n' +
            '4. "재생성" 버튼으로 완전히 새로운 보고서를 생성할 수 있습니다.',
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
        const { start, end } = currentWeek;

        if (action === 'generate') {
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

        } else if (action === 'regenerate') {
            if (!confirm('현재 보고서를 폐기하고 새로 생성합니다. 계속하시겠습니까?')) return;

            addMsg(`주간보고 재생성 요청 (${start} ~ ${end})`, 'user');
            showTyping();

            try {
                const report = await API.reports.regenerate({
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
        const seq = ++msgSeq;
        const sentBadge = report.isSent ? ' <span class="sent-badge">전송됨</span>' : '';
        addHtmlMsg(
            `<div class="report-text" id="report-text-${seq}">${Common.escapeHtml(report.rendered || '')}</div>` +
            `<div class="report-actions" id="report-actions-${seq}">` +
            `<button onclick="editReport(${seq})"><i class="bi bi-pencil"></i> 수정</button>` +
            `<button onclick="sendReport()"><i class="bi bi-send"></i> 전송</button>` +
            `</div>`,
            `주간보고 #${report.id} (${report.weekStart} ~ ${report.weekEnd})${sentBadge}`
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
     * Edit report (modify via AI)
     */
    window.editReport = function(seq) {
        const actionsEl = document.getElementById(`report-actions-${seq}`);
        if (!actionsEl) return;

        actionsEl.innerHTML =
            `<div class="edit-area">` +
            `<textarea id="edit-input-${seq}" placeholder="수정 지시사항을 입력하세요 (예: 좀 더 간결하게 해줘, 성과 부분을 강조해줘)"></textarea>` +
            `</div>` +
            `<div class="report-actions" style="margin-top:6px">` +
            `<button onclick="submitModification(${seq})"><i class="bi bi-check"></i> AI 수정 요청</button>` +
            `<button onclick="cancelEdit(${seq})"><i class="bi bi-x"></i> 취소</button>` +
            `</div>`;

        document.getElementById(`edit-input-${seq}`).focus();
    };

    /**
     * Cancel edit
     */
    window.cancelEdit = function(seq) {
        const actionsEl = document.getElementById(`report-actions-${seq}`);
        if (!actionsEl) return;
        actionsEl.innerHTML =
            `<button onclick="editReport(${seq})"><i class="bi bi-pencil"></i> 수정</button>` +
            `<button onclick="sendReport()"><i class="bi bi-send"></i> 전송</button>`;
    };

    /**
     * Submit modification via AI
     */
    window.submitModification = async function(seq) {
        const ta = document.getElementById(`edit-input-${seq}`);
        if (!ta) return;

        const instruction = ta.value.trim();
        if (!instruction) {
            return addMsg('수정 지시사항을 입력하세요.', 'bot');
        }

        const { start, end } = currentWeek;
        addMsg(instruction, 'user');
        cancelEdit(seq);
        showTyping();

        try {
            const report = await API.reports.modify({
                weekStart: start,
                weekEnd: end,
                instruction: instruction
            });
            hideTyping();
            showReport(report);
        } catch (e) {
            hideTyping();
            addMsg(e.message, 'bot error');
        }
    };

    /**
     * Send report - show version picker
     */
    window.sendReport = async function() {
        const { start, end } = currentWeek;

        try {
            const versions = await API.reports.getVersions(start, end);
            if (!versions || versions.length === 0) {
                return addMsg('전송할 보고서가 없습니다. 먼저 보고서를 생성하세요.', 'bot');
            }

            if (versions.length === 1) {
                await doSend(versions[0].id, start, end);
                return;
            }

            showVersionPicker(versions, start, end);
        } catch (e) {
            addMsg(e.message, 'bot error');
        }
    };

    /**
     * Show version picker UI
     */
    function showVersionPicker(versions, weekStart, weekEnd) {
        let html = '<div class="version-picker">' +
            '<div class="version-picker-title">전송할 버전을 선택하세요</div>' +
            '<div class="version-list">';

        versions.forEach((v, i) => {
            const checked = v.isLast ? 'checked' : '';
            const sentLabel = v.isSent ? ' <span class="sent-badge">전송됨</span>' : '';
            const vNum = versions.length - i;
            const fullText = Common.escapeHtml(v.rendered || '');
            const preview = (v.rendered || '').substring(0, 80) + ((v.rendered || '').length > 80 ? '...' : '');

            html += `<div class="version-item">` +
                `<input type="radio" name="version-select" value="${v.id}" ${checked} id="ver-${v.id}">` +
                `<div class="version-info">` +
                `<label class="version-header" for="ver-${v.id}">` +
                `<span class="version-label">v${vNum}${sentLabel}</span>` +
                `<span class="version-date">${new Date(v.createdAt).toLocaleString('ko-KR')}</span>` +
                `</label>` +
                `<div class="version-preview">${Common.escapeHtml(preview)}</div>` +
                `<button class="version-toggle" onclick="toggleVersionDetail(this)">` +
                `<i class="bi bi-chevron-down"></i> 상세보기</button>` +
                `<div class="version-detail" style="display:none">${fullText}</div>` +
                `</div>` +
                `</div>`;
        });

        html += '</div>' +
            `<div class="report-actions" style="margin-top:8px">` +
            `<button onclick="confirmSend('${weekStart}','${weekEnd}')"><i class="bi bi-send"></i> 전송</button>` +
            `<button onclick="this.closest('.msg').remove()"><i class="bi bi-x"></i> 취소</button>` +
            `</div></div>`;

        addHtmlMsg(html, '버전 선택');
    }

    /**
     * Toggle version detail view
     */
    window.toggleVersionDetail = function(btn) {
        const detail = btn.nextElementSibling;
        const icon = btn.querySelector('i');
        const isOpen = detail.style.display !== 'none';
        detail.style.display = isOpen ? 'none' : 'block';
        icon.className = isOpen ? 'bi bi-chevron-down' : 'bi bi-chevron-up';
        btn.childNodes[1].textContent = isOpen ? ' 상세보기' : ' 접기';
    };

    /**
     * Confirm and send selected version
     */
    window.confirmSend = async function(weekStart, weekEnd) {
        const selected = document.querySelector('input[name="version-select"]:checked');
        if (!selected) {
            return addMsg('버전을 선택하세요.', 'bot');
        }
        await doSend(Number(selected.value), weekStart, weekEnd);
    };

    /**
     * Execute send
     */
    async function doSend(reportId, weekStart, weekEnd) {
        showTyping();
        try {
            const report = await API.reports.send({
                weekStart: weekStart,
                weekEnd: weekEnd,
                reportId: reportId
            });
            hideTyping();
            addMsg(`주간보고 #${report.id} 전송 완료!`, 'bot', '전송됨');
        } catch (e) {
            hideTyping();
            addMsg(e.message, 'bot error');
        }
    }

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
