import { useEffect, useState } from 'react'
import { BookOpenText, Upload, Search, Loader2, FileSearch } from 'lucide-react'
import { kbApi } from '../api/endpoints'
import { DOCUMENT_SOURCE_TYPES } from '../utils/constants'
import LoadingSpinner from '../components/LoadingSpinner'

export default function Knowledge() {
  const [documents, setDocuments] = useState([])
  const [loadingDocs, setLoadingDocs] = useState(true)

  const [uploadForm, setUploadForm] = useState({ name: '', sourceType: 'SOP', content: '' })
  const [uploading, setUploading] = useState(false)
  const [uploadMsg, setUploadMsg] = useState('')

  const [query, setQuery] = useState('')
  const [searching, setSearching] = useState(false)
  const [searchResult, setSearchResult] = useState(null)

  const loadDocuments = async () => {
    setLoadingDocs(true)
    try {
      setDocuments(await kbApi.documents())
    } finally {
      setLoadingDocs(false)
    }
  }

  useEffect(() => {
    loadDocuments()
  }, [])

  const handleUpload = async (e) => {
    e.preventDefault()
    setUploading(true)
    setUploadMsg('')
    try {
      const doc = await kbApi.upload(uploadForm)
      setUploadMsg(`Indexed "${doc.name}" into ${doc.chunkCount} chunks.`)
      setUploadForm({ name: '', sourceType: 'SOP', content: '' })
      loadDocuments()
    } catch {
      setUploadMsg('Could not upload this document. Please try again.')
    } finally {
      setUploading(false)
    }
  }

  const handleSearch = async (e) => {
    e.preventDefault()
    if (!query.trim()) return
    setSearching(true)
    try {
      setSearchResult(await kbApi.search(query))
    } finally {
      setSearching(false)
    }
  }

  return (
    <div className="mx-auto max-w-5xl px-4 py-8">
      <div className="mb-6">
        <p className="font-mono text-xs uppercase tracking-widest text-ink-muted">RAG Knowledge Base</p>
        <h1 className="font-display text-4xl font-bold">Have we seen this before?</h1>
        <p className="mt-1 text-sm text-ink-muted">
          Upload SOPs, architecture docs, and past incident write-ups. Gemini embeds and semantically searches them.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <div className="rounded-lg border border-border bg-panel p-5">
          <h2 className="mb-3 flex items-center gap-2 font-display text-lg font-bold">
            <Upload size={18} className="text-brand" /> Upload document
          </h2>
          <form onSubmit={handleUpload} className="space-y-3">
            <input
              required
              value={uploadForm.name}
              onChange={(e) => setUploadForm({ ...uploadForm, name: e.target.value })}
              placeholder="Document name (e.g. Payment Service Runbook v2)"
              className="w-full rounded-md border border-border bg-void px-3 py-2 text-sm outline-none focus:border-brand"
            />
            <select
              value={uploadForm.sourceType}
              onChange={(e) => setUploadForm({ ...uploadForm, sourceType: e.target.value })}
              className="w-full rounded-md border border-border bg-void px-3 py-2 text-sm outline-none focus:border-brand"
            >
              {DOCUMENT_SOURCE_TYPES.map((t) => (
                <option key={t.value} value={t.value}>{t.label}</option>
              ))}
            </select>
            <textarea
              required
              rows={8}
              value={uploadForm.content}
              onChange={(e) => setUploadForm({ ...uploadForm, content: e.target.value })}
              placeholder="Paste the full document text here..."
              className="w-full resize-none rounded-md border border-border bg-void px-3 py-2 font-mono text-xs outline-none focus:border-brand"
            />
            <button
              type="submit"
              disabled={uploading}
              className="flex w-full items-center justify-center gap-2 rounded-md bg-brand py-2 text-sm font-semibold text-void transition-opacity hover:opacity-90 disabled:opacity-50"
            >
              {uploading ? <Loader2 size={15} className="animate-spin" /> : <Upload size={15} />}
              {uploading ? 'Embedding & indexing…' : 'Upload & index'}
            </button>
            {uploadMsg && <p className="text-center text-xs text-ink-muted">{uploadMsg}</p>}
          </form>

          <div className="mt-5 border-t border-border pt-4">
            <h3 className="mb-2 font-mono text-xs uppercase tracking-wide text-ink-muted">Indexed documents</h3>
            {loadingDocs ? (
              <LoadingSpinner label="Loading documents" />
            ) : documents.length === 0 ? (
              <p className="text-sm text-ink-muted">Nothing uploaded yet.</p>
            ) : (
              <ul className="space-y-1.5">
                {documents.map((d) => (
                  <li key={d.id} className="flex items-center justify-between rounded-md border border-border px-3 py-2 text-sm">
                    <span className="flex items-center gap-2"><BookOpenText size={13} className="text-ink-muted" /> {d.name}</span>
                    <span className="font-mono text-[11px] text-ink-muted">{d.chunkCount} chunks</span>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>

        <div className="rounded-lg border border-border bg-panel p-5">
          <h2 className="mb-3 flex items-center gap-2 font-display text-lg font-bold">
            <Search size={18} className="text-brand" /> Semantic search
          </h2>
          <form onSubmit={handleSearch} className="flex gap-2">
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Have we seen a database connection pool exhaustion before?"
              className="flex-1 rounded-md border border-border bg-void px-3 py-2 text-sm outline-none focus:border-brand"
            />
            <button
              type="submit"
              disabled={searching}
              className="flex items-center gap-1.5 rounded-md bg-brand px-4 py-2 text-sm font-semibold text-void disabled:opacity-50"
            >
              {searching ? <Loader2 size={15} className="animate-spin" /> : <FileSearch size={15} />}
            </button>
          </form>

          {searchResult && (
            <div className="mt-4 space-y-4">
              <div className="rounded-md bg-brand/10 p-3 text-sm">
                <p className="mb-1 font-mono text-[10px] uppercase tracking-wide text-brand">AI Answer</p>
                {searchResult.answer}
              </div>

              {searchResult.sources?.length > 0 && (
                <div className="space-y-2">
                  <p className="font-mono text-[10px] uppercase tracking-wide text-ink-muted">Sources</p>
                  {searchResult.sources.map((s, i) => (
                    <div key={i} className="rounded-md border border-border p-3">
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-semibold text-ink">{s.documentName}</span>
                        <span className="font-mono text-[10px] text-ink-muted">{Math.round(s.similarity * 100)}% match</span>
                      </div>
                      <p className="mt-1 line-clamp-3 text-xs text-ink-muted">{s.chunkContent}</p>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
