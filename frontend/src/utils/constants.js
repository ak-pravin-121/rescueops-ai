export const SEVERITIES = {
  CRITICAL: { label: 'Critical', color: 'var(--color-critical)' },
  HIGH: { label: 'High', color: 'var(--color-high)' },
  MEDIUM: { label: 'Medium', color: 'var(--color-medium)' },
  LOW: { label: 'Low', color: 'var(--color-low)' },
}

export const STATUSES = {
  OPEN: { label: 'Open', color: 'var(--color-critical)' },
  ACKNOWLEDGED: { label: 'Acknowledged', color: 'var(--color-high)' },
  INVESTIGATING: { label: 'Investigating', color: 'var(--color-medium)' },
  RESOLVED: { label: 'Resolved', color: 'var(--color-success)' },
  CLOSED: { label: 'Closed', color: 'var(--color-ink-muted)' },
}

export const STATUS_ORDER = ['OPEN', 'ACKNOWLEDGED', 'INVESTIGATING', 'RESOLVED', 'CLOSED']
export const SEVERITY_ORDER = ['CRITICAL', 'HIGH', 'MEDIUM', 'LOW']

export const ANALYSIS_TYPES = {
  ANALYSIS: { label: 'Analyzer Agent', agent: 'Analyzer Agent' },
  ROOT_CAUSE: { label: 'Root Cause Agent', agent: 'Root Cause Agent' },
  RUNBOOK: { label: 'Runbook Agent', agent: 'Runbook Agent' },
  POSTMORTEM: { label: 'Postmortem Agent', agent: 'Postmortem Agent' },
  SECURITY: { label: 'Security Agent', agent: 'Security Agent' },
}

export const DOCUMENT_SOURCE_TYPES = [
  { value: 'SOP', label: 'SOP' },
  { value: 'ARCHITECTURE_DOC', label: 'Architecture Doc' },
  { value: 'PAST_INCIDENT', label: 'Past Incident' },
  { value: 'RUNBOOK', label: 'Runbook' },
  { value: 'OTHER', label: 'Other' },
]

export const FILE_TYPES = [
  'SPRING_BOOT_LOG',
  'DOCKER_LOG',
  'KUBERNETES_LOG',
  'NGINX_LOG',
  'MONITORING_REPORT',
  'OTHER',
]
