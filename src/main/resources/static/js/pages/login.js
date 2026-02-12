if (Common.isLoggedIn()) {
    window.location.href = '/reports';
}

async function handleLogin(event) {
    event.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const submitBtn = document.querySelector('.btn-login');

    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>로그인 중...';

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || '로그인에 실패했습니다.');
        }

        Common.saveToken(data.accessToken);
        Common.setSelectedUser(data.userId);
        window.location.href = '/reports';
    } catch (error) {
        const existingAlert = document.querySelector('.alert');
        if (existingAlert) existingAlert.remove();

        const alertHtml = `
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <i class="bi bi-exclamation-triangle me-2"></i>
                ${Common.escapeHtml(error.message)}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;
        document.getElementById('loginForm').insertAdjacentHTML('beforebegin', alertHtml);
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="bi bi-box-arrow-in-right"></i> 로그인';
    }
}
