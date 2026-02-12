const Common = (() => {
    function formatDate(date) {
        if (typeof date === 'string') {
            date = new Date(date);
        }
        return date.toISOString().split('T')[0];
    }

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

    function getToday() {
        return formatDate(new Date());
    }

    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    // --- Auth Token ---
    function saveToken(token) { localStorage.setItem('accessToken', token); }
    function getToken() { return localStorage.getItem('accessToken'); }
    function removeToken() { localStorage.removeItem('accessToken'); }
    function isLoggedIn() { return !!getToken(); }
    function logout() { removeToken(); window.location.href = '/login'; }

    return {
        getCurrentWeek,
        getToday,
        escapeHtml,
        saveToken,
        getToken,
        removeToken,
        isLoggedIn,
        logout
    };
})();
