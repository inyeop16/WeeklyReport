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
            if (!confirm('새 보고서를 생성합니다. 현재 보고서와 비교 후 선택할 수 있습니다. 계속하시겠습니까?')) return;

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
     * Show report in chat - if candidateRendered exists, show comparison UI
     */
    function showReport(report) {
        if (report.candidateRendered) {
            showComparison(report);
        } else {
            showSingleReport(report);
        }
    }

    /**
     * Show single report (no candidate)
     */
    function showSingleReport(report) {
        const seq = ++msgSeq;
        const sentBadge = report.isSent ? ' <span class="sent-badge">전송됨</span>' : '';
        addHtmlMsg(
            `<div class="report-text report-content" id="report-text-${seq}">${marked.parse(report.rendered || '')}</div>` +
            `<div class="report-actions" id="report-actions-${seq}">` +
            `<button onclick="editReport(${seq})"><i class="bi bi-pencil"></i> 수정</button>` +
            `<button onclick="sendReport()"><i class="bi bi-send"></i> 전송</button>` +
            `</div>`,
            `주간보고 #${report.id} (${report.weekStart} ~ ${report.weekEnd})${sentBadge}`
        );
    }

    /**
     * Show comparison UI (current vs candidate)
     */
    function showComparison(report) {
        addHtmlMsg(
            `<div class="candidate-comparison">` +
            `<div class="comparison-panel">` +
            `<div class="comparison-label">현재 보고서</div>` +
            `<div class="report-content">${marked.parse(report.rendered || '')}</div>` +
            `</div>` +
            `<div class="comparison-panel candidate">` +
            `<div class="comparison-label new">새 보고서</div>` +
            `<div class="report-content">${marked.parse(report.candidateRendered || '')}</div>` +
            `</div>` +
            `</div>` +
            `<div class="report-actions" style="margin-top:8px">` +
            `<button onclick="selectCandidate(false)"><i class="bi bi-arrow-counterclockwise"></i> 현재 유지</button>` +
            `<button class="primary" onclick="selectCandidate(true)"><i class="bi bi-check-lg"></i> 새 버전 선택</button>` +
            `</div>`,
            `주간보고 #${report.id} - 비교 (${report.weekStart} ~ ${report.weekEnd})`
        );
    }

    /**
     * Select candidate (accept or reject)
     */
    window.selectCandidate = async function(accept) {
        const { start, end } = currentWeek;
        showTyping();

        try {
            const report = await API.reports.selectCandidate({
                weekStart: start,
                weekEnd: end,
                acceptCandidate: accept
            });
            hideTyping();
            addMsg(accept ? '새 버전을 선택했습니다.' : '현재 버전을 유지합니다.', 'bot');
            showReport(report);
        } catch (e) {
            hideTyping();
            addMsg(e.message, 'bot error');
        }
    };

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
     * Send report
     */
    window.sendReport = async function() {
        const { start, end } = currentWeek;

        try {
            const reports = await API.reports.getAll();
            const report = reports.find(r => r.weekStart === start && r.weekEnd === end);

            if (!report) {
                return addMsg('전송할 보고서가 없습니다. 먼저 보고서를 생성하세요.', 'bot');
            }

            if (report.candidateRendered) {
                return addMsg('후보 보고서가 있습니다. 먼저 버전을 선택하세요.', 'bot');
            }

            if (!confirm('이 보고서를 전송하시겠습니까?')) return;

            await doSend(report.id);
        } catch (e) {
            addMsg(e.message, 'bot error');
        }
    };

    /**
     * Execute send
     */
    async function doSend(reportId) {
        showTyping();
        try {
            const report = await API.reports.send({
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
