/**
 * Common Utilities for Weekly Report
 */

const Common = (() => {
    /**
     * Format date to YYYY-MM-DD
     */
    function formatDate(date) {
        if (typeof date === 'string') {
            date = new Date(date);
        }
        return date.toISOString().split('T')[0];
    }

    /**
     * Format date to Korean readable format (YYYY년 MM월 DD일)
     */
    function formatDateKo(dateStr) {
        const date = new Date(dateStr);
        const year = date.getFullYear();
        const month = date.getMonth() + 1;
        const day = date.getDate();
        return `${year}년 ${month}월 ${day}일`;
    }

    /**
     * Get current week's Monday and Friday
     */
    function getCurrentWeek() {
        const today = new Date();
        const day = today.getDay();
        const monday = new Date(today);
        monday.setDate(today.getDate() - (day === 0 ? 6 : day - 1));
        const friday = new Date(monday);
        friday.setDate(monday.getDate() + 4);
        return {
            start: formatDate(monday),
            end: formatDate(friday)
        };
    }

    /**
     * Get today's date in YYYY-MM-DD format
     */
    function getToday() {
        return formatDate(new Date());
    }

    /**
     * Escape HTML to prevent XSS
     */
    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    /**
     * Show loading spinner
     */
    function showLoading(containerId) {
        const container = document.getElementById(containerId);
        if (container) {
            container.innerHTML = `
                <div class="spinner-container">
                    <div class="spinner-border" role="status">
                        <span class="visually-hidden">로딩 중...</span>
                    </div>
                </div>
            `;
        }
    }

    /**
     * Show empty state
     */
    function showEmpty(containerId, message, icon = 'inbox') {
        const container = document.getElementById(containerId);
        if (container) {
            container.innerHTML = `
                <div class="empty-state">
                    <i class="bi bi-${icon}"></i>
                    <p>${message}</p>
                </div>
            `;
        }
    }

    /**
     * Show error message
     */
    function showError(message, containerId = null) {
        const alertHtml = `
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <i class="bi bi-exclamation-triangle me-2"></i>
                ${escapeHtml(message)}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;

        if (containerId) {
            const container = document.getElementById(containerId);
            if (container) {
                container.innerHTML = alertHtml + container.innerHTML;
            }
        } else {
            console.error('Error:', message);
        }
    }

    /**
     * Show success message
     */
    function showSuccess(message, containerId = null) {
        const alertHtml = `
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                <i class="bi bi-check-circle me-2"></i>
                ${escapeHtml(message)}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;

        if (containerId) {
            const container = document.getElementById(containerId);
            if (container) {
                container.innerHTML = alertHtml + container.innerHTML;
            }
        }
    }

    /**
     * Show Bootstrap toast
     */
    function showToast(message, type = 'success') {
        const toastContainer = document.getElementById('toastContainer');
        if (!toastContainer) {
            const container = document.createElement('div');
            container.id = 'toastContainer';
            container.className = 'toast-container position-fixed top-0 end-0 p-3';
            container.style.zIndex = '1080';
            document.body.appendChild(container);
        }

        const toastId = 'toast-' + Date.now();
        const icon = type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-triangle' : 'info-circle';
        const bgClass = type === 'success' ? 'bg-success' : type === 'error' ? 'bg-danger' : 'bg-primary';

        const toastHtml = `
            <div id="${toastId}" class="toast" role="alert">
                <div class="toast-header ${bgClass} text-white">
                    <i class="bi bi-${icon} me-2"></i>
                    <strong class="me-auto">알림</strong>
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="toast"></button>
                </div>
                <div class="toast-body">
                    ${escapeHtml(message)}
                </div>
            </div>
        `;

        document.getElementById('toastContainer').insertAdjacentHTML('beforeend', toastHtml);
        const toastEl = document.getElementById(toastId);
        const toast = new bootstrap.Toast(toastEl, { delay: 3000 });
        toast.show();

        toastEl.addEventListener('hidden.bs.toast', () => {
            toastEl.remove();
        });
    }

    /**
     * Confirm dialog using Bootstrap modal
     */
    function confirm(message, callback) {
        if (window.confirm(message)) {
            callback();
        }
    }

    /**
     * Store selected user in localStorage
     */
    function setSelectedUser(userId) {
        localStorage.setItem('selectedUserId', userId);
    }

    /**
     * Get selected user from localStorage
     */
    function getSelectedUser() {
        return localStorage.getItem('selectedUserId');
    }

    /**
     * Store selected template in localStorage
     */
    function setSelectedTemplate(templateId) {
        localStorage.setItem('selectedTemplateId', templateId);
    }

    /**
     * Get selected template from localStorage
     */
    function getSelectedTemplate() {
        return localStorage.getItem('selectedTemplateId');
    }

    // Public API
    return {
        formatDate,
        formatDateKo,
        getCurrentWeek,
        getToday,
        escapeHtml,
        showLoading,
        showEmpty,
        showError,
        showSuccess,
        showToast,
        confirm,
        setSelectedUser,
        getSelectedUser,
        setSelectedTemplate,
        getSelectedTemplate
    };
})();
