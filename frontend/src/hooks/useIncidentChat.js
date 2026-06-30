import { useEffect, useRef, useState, useCallback } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { useAuth } from '../context/AuthContext'

const WS_BASE_URL = import.meta.env.VITE_WS_BASE_URL || 'http://localhost:8080'

export function useIncidentChat(incidentId, onMessage) {
  const { getToken } = useAuth()
  const clientRef = useRef(null)
  const [connected, setConnected] = useState(false)

  const handleMessageRef = useRef(onMessage)
  handleMessageRef.current = onMessage

  useEffect(() => {
    if (!incidentId) return

    const client = new Client({
      webSocketFactory: () => new SockJS(`${WS_BASE_URL}/ws`),
      connectHeaders: {
        Authorization: `Bearer ${getToken() || ''}`,
      },
      reconnectDelay: 4000,
      onConnect: () => {
        setConnected(true)
        client.subscribe(`/topic/incidents/${incidentId}`, (frame) => {
          try {
            const payload = JSON.parse(frame.body)
            handleMessageRef.current?.(payload)
          } catch {
            // ignore malformed frames
          }
        })
      },
      onDisconnect: () => setConnected(false),
      onWebSocketClose: () => setConnected(false),
    })

    client.activate()
    clientRef.current = client

    return () => {
      client.deactivate()
      clientRef.current = null
      setConnected(false)
    }
  }, [incidentId, getToken])

  return { connected }
}
