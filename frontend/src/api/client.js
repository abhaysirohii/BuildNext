const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

async function request(path, { method = 'GET', body, token, isFormData = false } = {}) {
  const headers = {};
  if (token) headers['Authorization'] = `Bearer ${token}`;
  if (!isFormData) headers['Content-Type'] = 'application/json';

  const res = await fetch(`${BASE_URL}${path}`, {
    method,
    headers,
    body: isFormData ? body : (body ? JSON.stringify(body) : undefined),
  });

  const isJson = res.headers.get('content-type')?.includes('application/json');
  const data = isJson ? await res.json() : null;

  if (!res.ok) {
    throw new Error(data?.message || `Request failed: ${res.status}`);
  }
  return data;
}

export const api = {
  register: (username, password) => request('/api/auth/register', { method: 'POST', body: { username, password } }),
  login: (username, password) => request('/api/auth/login', { method: 'POST', body: { username, password } }),

  listTasks: (token) => request('/api/tasks', { token }),
  createTask: (token, title, description) => request('/api/tasks', { method: 'POST', token, body: { title, description } }),
  updateTask: (token, id, task) => request(`/api/tasks/${id}`, { method: 'PUT', token, body: task }),
  deleteTask: (token, id) => request(`/api/tasks/${id}`, { method: 'DELETE', token }),

  redFlags: (token, jobDescription, projectDescription) =>
    request('/api/resume/redflags', { method: 'POST', token, body: { jobDescription, projectDescription } }),
  scoreDescription: (token, projectDescription, jobDescription) =>
    request('/api/resume/score', { method: 'POST', token, body: { projectDescription, jobDescription } }),

  // New: upload a resume PDF + JD, get ATS score + both analyses in one call
  analyzeResumePdf: (token, file, jobDescription) => {
    const form = new FormData();
    form.append('resume', file);
    form.append('jobDescription', jobDescription);
    return request('/api/resume/analyze-pdf', { method: 'POST', token, body: form, isFormData: true });
  },
};
