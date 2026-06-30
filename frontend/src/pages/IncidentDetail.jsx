import { useEffect, useState, useCallback } from 'react'
import { useParams } from 'react-router-dom'
import { Server, UserRound, Clock, Paperclip, Search, Wrench, FileText, ShieldAlert, Workflow, Loader2 } from 'lucide-react'
import { incidentsApi, aiApi } from '../api/endpoints'
import { STATUS_ORDER, STATUSES, FILE_TYPES } from '../utils/constants'
import SeverityBadge from '../components/SeverityBadge'
import StatusBadge from '../components/StatusBadge'
import LoadingSpinner from '../components/LoadingSpinner'
import AgentPanel from '../components/AgentPanel'
import TextAgentPanel from '../components/TextAgentPanel'
import WarRoomChat from '../components/WarRoomChat'
import { useAuth } from '../context/AuthContext'

export default function IncidentDetail() {
  const { id } = useParams()
  const incidentId = Number(id)
  const { user } = useAuth()

  const [incident, setIncident] = useState(null)
  const [files, setFiles] = useState([])
  const [loading, setLoading] = useState(true)

  const [analysis, setAnalysis] = useState(null)
  const [rootCause, setRootCause] = useState(null)
  const [runbook, setRunbook] = useState(null)
  const [postmortem, setPostmortem] = useState(null)
  const [security, setSecurity] = useState(null)
  const [agentLoading, setAgentLoading] = useState(null)

  const [fileForm, setFileForm] = useState({ fileName: '', fileType: FILE_TYPES[0], content: '' })
  const [uploadingFile, setUploadingFile] = useState(false)

  const loadIncident = useCallback(async () => {
    const data = await incidentsApi.get(incidentId)
    setIncident(data)
  }, [incidentId])

  useEffect(() => {
    let active = true
    async function load() {
      setLoading(true)
      try {
        const [incidentData, fileData, history] = await Promise.all([
          incidentsApi.get(incidentId),
          incidentsApi.listFiles(incidentId),
          aiApi.history(incidentId),
        ])
        if (!active) return
        setIncident(incidentData)
        setFiles(fileData)

        for (const h of history) {
          if (h.analysisType === 'ANALYSIS') setAnalysis(h);
          if (h.analysisType === 'ROOT_CAUSE') setRootCause(h);
          if (h.analysisType === 'SECURITY') setSecurity(h);
          if (h.analysisType === 'POSTMORTEM') setPostmortem(h);
        }
      } finally {
        if (active) setLoading(false)
      }
    }
    load()
    return () => { active = false }
  }, [incidentId])

  const updateStatus = async (status) => {
    const updated = await incidentsApi.update(incidentId, { status })
    setIncident(updated)
  }

  const assignToMe = async () => {
    const updated = await incidentsApi.update(incidentId, { assignedToId: user.userId })
    setIncident(updated)
  }

  const runAgent = async (key, fn) => {
    setAgentLoading(key)
    try {
      const result = await fn()
      return result
    } finally {
      setAgentLoading(null)
      loadIncident()
    }
  }

  const handleAnalyze = () => runAgent('analyze', async () => setAnalysis(await aiApi.analyze(incidentId)))
  const handleRootCause = () => runAgent('rootCause', async () => setRootCause(await aiApi.rootCause(incidentId)))
  const handleRunbook = () => runAgent('runbook', async () => setRunbook(await aiApi.runbook(incidentId)))
  const handlePostmortem = () => runAgent('postmortem', async () => setPostmortem(await aiApi.postmortem(incidentId)))
  const handleSecurity = () => runAgent('security', async () => setSecurity(await aiApi.securityScan(incidentId)))

  const handleFullTriage = () =>
    runAgent('fullTriage', async () => {
      const result = await aiApi.fullTriage(incidentId)
      setAnalysis(result.analysis)
      setRootCause(result.rootCause)
      setRunbook(result.runbook)
    })

  const handleFileUpload = async (e) => {
    e.preventDefault()
    if (!fileForm.fileName.trim() || !fileForm.content.trim()) return
    setUploadingFile(true)
    try {
      const file = await incidentsApi.addFile(incidentId, fileForm)
      setFiles((prev) => [...prev, file])
      setFileForm({ fileName: '', fileType: FILE_TYPES[0], content: '' })
    } finally {
      setUploadingFile(false)
    }
  }

  if (loading || !incident) return <LoadingSpinner label="Loading incident" />

  return (
    <div className="mx-auto max-w-6xl px-4 py-8">
      <div className="mb-6 rounded-lg border border-border bg-panel p-5">
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <p className="font-mono text-xs text-ink-muted">#{String(incident.id).padStart(5, '0')}</p>
            <h1 className="font-display text-2xl font-bold">{incident.title}</h1>
          </div>
          <div className="flex items-center gap-2">
            <SeverityBadge severity={incident.severity} />
            <StatusBadge status={incident.status} />
          </div>
        </div>

        <p className="mt-3 text-sm text-ink">{incident.description}</p>

        <div className="mt-4 flex flex-wrap items-center gap-5 text-xs text-ink-muted">
          <span className="flex items-center gap-1"><Server size={13} /> {incident.serviceName}</span>
          <span className="flex items-center gap-1"><UserRound size={13} /> {incident.assignedToName || 'Unassigned'}</span>
          <span className="flex items-center gap-1"><Clock size={13} /> opened {new Date(incident.createdAt).toLocaleString()}</span>
        </div>

        {incident.rootCause && (
          <div className="mt-4 rounded-md border border-border bg-void/50 p-3">
            <p className="font-mono text-[10px] uppercase tracking-wide text-ink-muted">Current root cause</p>
            <p className="mt-1 text-sm">{incident.rootCause}</p>
            {incident.suggestedFix && (
              <p className="mt-2 text-sm text-success">
                <span className="font-mono text-[10px] uppercase tracking-wide opacity-80">Suggested fix · </span>
                {incident.suggestedFix}
              </p>
            )}
          </div>
        )}

        <div className="mt-4 flex flex-wrap items-center gap-3 border-t border-border pt-4">
          <select
            value={incident.status}
            onChange={(e) => updateStatus(e.target.value)}
            className="rounded-md border border-border bg-void px-3 py-1.5 text-sm text-ink outline-none focus:border-brand"
          >
            {STATUS_ORDER.map((s) => (
              <option key={s} value={s}>{STATUSES[s].label}</option>
            ))}
          </select>

          {!incident.assignedToId && (
            <button
              onClick={assignToMe}
              className="rounded-md border border-brand/40 px-3 py-1.5 text-sm font-semibold text-brand hover:bg-brand/10"
            >
              Assign to me
            </button>
          )}

          <button
            onClick={handleFullTriage}
            disabled={agentLoading !== null}
            className="ml-auto flex items-center gap-2 rounded-md bg-brand px-4 py-1.5 text-sm font-semibold text-void transition-opacity hover:opacity-90 disabled:opacity-50"
          >
            {agentLoading === 'fullTriage' ? <Loader2 size={15} className="animate-spin" /> : <Workflow size={15} />}
            Run Full Multi-Agent Triage
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <div className="space-y-4">
          <AgentPanel
            title="Analyzer Agent"
            description="Severity, root cause and confidence from logs"
            icon={Search}
            result={analysis}
            loading={agentLoading === 'analyze'}
            onRun={handleAnalyze}
          />
          <AgentPanel
            title="Root Cause Agent"
            description="Deeper dependency & failure correlation, grounded in past incidents"
            icon={Wrench}
            result={rootCause}
            loading={agentLoading === 'rootCause'}
            onRun={handleRootCause}
          />
          <AgentPanel
            title="Security Agent"
            description="Scans logs for SQLi, brute force, XSS, DDoS indicators"
            icon={ShieldAlert}
            result={security}
            loading={agentLoading === 'security'}
            onRun={handleSecurity}
          />
          <TextAgentPanel
            title="Runbook Agent"
            description="Step-by-step remediation runbook"
            icon={FileText}
            content={runbook?.content}
            loading={agentLoading === 'runbook'}
            onRun={handleRunbook}
          />
          <TextAgentPanel
            title="Postmortem Agent"
            description="Blameless postmortem document"
            icon={FileText}
            content={postmortem?.content}
            loading={agentLoading === 'postmortem'}
            onRun={handlePostmortem}
          />

          <div className="rounded-lg border border-border bg-panel p-4">
            <h3 className="mb-3 flex items-center gap-2 font-display text-base font-bold">
              <Paperclip size={16} className="text-brand" /> Logs & Evidence
            </h3>

            {files.length > 0 && (
              <div className="mb-3 space-y-2">
                {files.map((f) => (
                  <details key={f.id} className="rounded-md border border-border">
                    <summary className="cursor-pointer px-3 py-2 font-mono text-xs text-ink-muted">
                      {f.fileName} <span className="text-ink-muted/60">({f.fileType})</span>
                    </summary>
                    <pre className="max-h-48 overflow-y-auto whitespace-pre-wrap border-t border-border bg-void/60 p-3 font-mono text-[11px]">
                      {f.content}
                    </pre>
                  </details>
                ))}
              </div>
            )}

            <form onSubmit={handleFileUpload} className="space-y-2">
              <div className="flex gap-2">
                <input
                  value={fileForm.fileName}
                  onChange={(e) => setFileForm({ ...fileForm, fileName: e.target.value })}
                  placeholder="app.log"
                  className="flex-1 rounded-md border border-border bg-void px-3 py-1.5 text-sm outline-none focus:border-brand"
                />
                <select
                  value={fileForm.fileType}
                  onChange={(e) => setFileForm({ ...fileForm, fileType: e.target.value })}
                  className="rounded-md border border-border bg-void px-3 py-1.5 text-sm outline-none focus:border-brand"
                >
                  {FILE_TYPES.map((t) => (
                    <option key={t} value={t}>{t.replaceAll('_', ' ')}</option>
                  ))}
                </select>
              </div>
              <textarea
                value={fileForm.content}
                onChange={(e) => setFileForm({ ...fileForm, content: e.target.value })}
                rows={4}
                placeholder="Paste log content here..."
                className="w-full resize-none rounded-md border border-border bg-void px-3 py-2 font-mono text-xs outline-none focus:border-brand"
              />
              <button
                type="submit"
                disabled={uploadingFile}
                className="w-full rounded-md border border-brand/40 py-1.5 text-sm font-semibold text-brand hover:bg-brand/10 disabled:opacity-50"
              >
                {uploadingFile ? 'Uploading…' : 'Attach log file'}
              </button>
            </form>
          </div>
        </div>

        <div>
          <WarRoomChat incidentId={incidentId} />
        </div>
      </div>
    </div>
  )
}
