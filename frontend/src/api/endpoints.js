import apiClient from './client'

export const authApi = {
  register: (payload) => apiClient.post('/auth/register', payload).then((r) => r.data),
  login: (payload) => apiClient.post('/auth/login', payload).then((r) => r.data),
}

export const incidentsApi = {
  create: (payload) => apiClient.post('/incidents', payload).then((r) => r.data),
  list: (params) => apiClient.get('/incidents', { params }).then((r) => r.data),
  get: (id) => apiClient.get(`/incidents/${id}`).then((r) => r.data),
  update: (id, payload) => apiClient.put(`/incidents/${id}`, payload).then((r) => r.data),
  serviceNames: () => apiClient.get('/incidents/meta/services').then((r) => r.data),

  addFile: (id, payload) => apiClient.post(`/incidents/${id}/files`, payload).then((r) => r.data),
  listFiles: (id) => apiClient.get(`/incidents/${id}/files`).then((r) => r.data),

  getMessages: (id) => apiClient.get(`/incidents/${id}/messages`).then((r) => r.data),
  postMessage: (id, message) => apiClient.post(`/incidents/${id}/messages`, { message }).then((r) => r.data),
}

export const aiApi = {
  analyze: (incidentId, extraContext) => apiClient.post('/ai/analyze', { incidentId, extraContext }).then((r) => r.data),
  rootCause: (incidentId, extraContext) => apiClient.post('/ai/root-cause', { incidentId, extraContext }).then((r) => r.data),
  runbook: (incidentId) => apiClient.post('/ai/runbook', { incidentId }).then((r) => r.data),
  postmortem: (incidentId) => apiClient.post('/ai/postmortem', { incidentId }).then((r) => r.data),
  securityScan: (incidentId, extraContext) => apiClient.post('/ai/security-scan', { incidentId, extraContext }).then((r) => r.data),
  fullTriage: (incidentId, extraContext) => apiClient.post('/ai/full-triage', { incidentId, extraContext }).then((r) => r.data),
  history: (incidentId) => apiClient.get(`/ai/history/${incidentId}`).then((r) => r.data),
  securityAlerts: () => apiClient.get('/ai/security-alerts').then((r) => r.data),
}

export const kbApi = {
  upload: (payload) => apiClient.post('/kb/upload', payload).then((r) => r.data),
  search: (query) => apiClient.get('/kb/search', { params: { query } }).then((r) => r.data),
  documents: () => apiClient.get('/kb/documents').then((r) => r.data),
}

export const predictionsApi = {
  recent: () => apiClient.get('/predictions').then((r) => r.data),
  run: (serviceName) => apiClient.post('/predictions/run', null, { params: { serviceName } }).then((r) => r.data),
  latest: (serviceName) => apiClient.get(`/predictions/${serviceName}/latest`).then((r) => r.data),
}

export const dashboardApi = {
  summary: () => apiClient.get('/dashboard/summary').then((r) => r.data),
}
