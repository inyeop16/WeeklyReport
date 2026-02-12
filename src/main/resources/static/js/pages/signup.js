if (Common.isLoggedIn()) {
    window.location.href = '/reports';
}

(async function loadDepartments() {
    try {
        const response = await fetch('/api/departments');
        const departments = await response.json();
        const select = document.getElementById('departmentId');
        departments.forEach(dept => {
            const option = document.createElement('option');
            option.value = dept.id;
            option.textContent = dept.name;
            select.appendChild(option);
        });
    } catch (e) {
        console.error('부서 목록 로드 실패:', e);
    }
})();

async function handleSignup(event) {
    event.preventDefault();

    const username = document.getElementById('username').value;
    const name = document.getElementById('name').value;
    const departmentId = document.getElementById('departmentId').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const passwordConfirm = document.getElementById('passwordConfirm').value;

    if (password !== passwordConfirm) {
        alert('비밀번호가 일치하지 않습니다.');
        return;
    }

    const submitBtn = document.querySelector('.btn-signup');
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>가입 처리 중...';

    const body = { username, name, email, password };
    if (departmentId) {
        body.departmentId = Number(departmentId);
    }

    try {
        const response = await fetch('/api/auth/signup', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || '회원가입에 실패했습니다.');
        }

        alert('회원가입이 완료되었습니다.');
        window.location.href = '/login';
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
        document.getElementById('signupForm').insertAdjacentHTML('beforebegin', alertHtml);
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="bi bi-check-circle"></i> 가입하기';
    }
}
