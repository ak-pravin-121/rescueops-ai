import { useEffect, useRef, useState, useCallback } from 'react'
import { Send, Bot, Cog } from 'lucide-react'
import { incidentsApi } from '../api/endpoints'
import { useIncidentChat } from '../hooks/useIncidentChat'
import LiveIndicator from './LiveIndicator'

export default function WarRoomChat({ incidentId }) {
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const [sending, setSending] = useState(false)
  const bottomRef = useRef(null)

  useEffect(() => {
    incidentsApi.getMessages(incidentId).then(setMessages).catch(() => {})
  }, [incidentId])

  const handleIncoming = useCallback((payload) => {
    setMessages((prev) => {
      if (prev.some((m) => m.id === payload.id)) return prev
      return [...prev, payload]
    })
  }, [])

  const { connected } = useIncidentChat(incidentId, handleIncoming)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const sendMessage = async (e) => {
    e.preventDefault()
    const text = input.trim()
    if (!text || sending) return
    setSending(true)
    try {
      await incidentsApi.postMessage(incidentId, text)
      setInput('')
    } catch {
      // the message simply won't appear; user can retry
    } finally {
      setSending(false)
    }
  }

  return (
    <div className="flex h-[480px] flex-col rounded-lg border border-border bg-panel">
      <div className="flex items-center justify-between border-b border-border px-4 py-3">
        <h3 className="font-display text-base font-bold">War Room</h3>
        {connected ? <LiveIndicator /> : <span className="font-mono text-[11px] text-ink-muted">connecting…</span>}
      </div>

      <div className="flex-1 space-y-3 overflow-y-auto px-4 py-3">
        {messages.length === 0 && (
          <p className="py-8 text-center text-sm text-ink-muted">No activity yet. Updates and agent findings will appear here in real time.</p>
        )}
        {messages.map((m) => (
          <ChatLine key={m.id} message={m} />
        ))}
        <div ref={bottomRef} />
      </div>

      <form onSubmit={sendMessage} className="flex items-center gap-2 border-t border-border p-3">
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Post an update to the war room..."
          className="flex-1 rounded-md border border-border bg-void px-3 py-2 text-sm text-ink outline-none focus:border-brand"
        />
        <button
          type="submit"
          disabled={sending}
          className="rounded-md bg-brand p-2 text-void transition-opacity disabled:opacity-50"
          aria-label="Send"
        >
          <Send size={16} />
        </button>
      </form>
    </div>
  )
}

function ChatLine({ message }) {
  const isAi = message.senderType === 'AI'
  const isSystem = message.senderType === 'SYSTEM'

  if (isAi || isSystem) {
    return (
      <div className="flex items-start gap-2 rounded-md bg-brand/5 px-3 py-2 text-sm">
        {isAi ? <Bot size={14} className="mt-0.5 shrink-0 text-brand" /> : <Cog size={14} className="mt-0.5 shrink-0 text-ink-muted" />}
        <div>
          <p className="font-mono text-[10px] uppercase tracking-wide text-ink-muted">{message.senderName}</p>
          <p className="text-ink">{message.message}</p>
        </div>
      </div>
    )
  }

  return (
    <div className="rounded-md border border-border px-3 py-2 text-sm">
      <p className="font-mono text-[10px] uppercase tracking-wide text-ink-muted">{message.senderName}</p>
      <p className="text-ink">{message.message}</p>
    </div>
  )
}
