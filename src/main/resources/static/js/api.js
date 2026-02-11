/**
 * REST API Wrapper for Weekly Report
 */

const API = (() => {
    const BASE_URL = '/api';

    /**
     * Generic HTTP request wrapper
     */
    async function request(method, path, body = null) {
        const options = {
            method,
            headers: {
                'Content-Type': 'application/json'
            }
        };

        const token = Common.getToken();
        if (token) {
            options.headers['Authorization'] = 'Bearer ' + token;
        }

        if (body) {
            options.body = JSON.stringify(body);
        }

        try {
            const response = await fetch(BASE_URL + path, options);

            if (response.status === 401) {
                Common.removeToken();
                window.location.href = '/login';
                return;
            }

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || `HTTP ${response.status}: ${response.statusText}`);
            }

            return data;
        } catch (error) {
            console.error(`API Error [${method} ${path}]:`, error);
            throw error;
        }
    }

    /**
     * Generic CRUD methods
     */
    const http = {
        get: (path) => request('GET', path),
        post: (path, body) => request('POST', path, body),
        put: (path, body) => request('PUT', path, body),
        delete: (path) => request('DELETE', path)
    };

    /**
     * Users API
     */
    const users = {
        getAll: () => http.get('/users'),
        getById: (id) => http.get(`/users/${id}`),
        create: (data) => http.post('/users', data),
        update: (id, data) => http.put(`/users/${id}`, data),
        delete: (id) => http.delete(`/users/${id}`)
    };

    /**
     * Templates API
     */
    const templates = {
        getAll: () => http.get('/templates'),
        getById: (id) => http.get(`/templates/${id}`),
        create: (data) => http.post('/templates', data),
        update: (id, data) => http.put(`/templates/${id}`, data),
        delete: (id) => http.delete(`/templates/${id}`)
    };

    /**
     * Daily Entries API
     */
    const entries = {
        getAll: () => http.get('/daily-entries'),
        getByUser: (userId, startDate = null, endDate = null) => {
            let url = `/daily-entries?userId=${userId}`;
            if (startDate) url += `&startDate=${startDate}`;
            if (endDate) url += `&endDate=${endDate}`;
            return http.get(url);
        },
        getById: (id) => http.get(`/daily-entries/${id}`),
        create: (data) => http.post('/daily-entries', data),
        update: (id, data) => http.put(`/daily-entries/${id}`, data),
        delete: (id) => http.delete(`/daily-entries/${id}`)
    };

    /**
     * Reports API
     */
    const reports = {
        getAll: () => http.get('/reports'),
        getByUser: (userId) => http.get(`/reports?userId=${userId}`),
        getById: (id) => http.get(`/reports/${id}`),
        generate: (data) => http.post('/reports/generate', data),
        update: (id, instruction) => http.put(`/reports/${id}`, { instruction }),
        delete: (id) => http.delete(`/reports/${id}`)
    };

    // Public API
    return {
        http,
        users,
        templates,
        entries,
        reports
    };
})();
