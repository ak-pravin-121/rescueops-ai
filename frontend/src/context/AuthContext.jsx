import { createContext, useContext, useEffect, useState, useCallback } from 'react'
import { authApi } from '../api/endpoints'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const storedUser = localStorage.getItem('ro_user')
    const storedToken = localStorage.getItem('ro_token')
    if (storedUser && storedToken) {
      setUser(JSON.parse(storedUser))
    }
    setIsLoading(false)
  }, [])

  const persistSession = useCallback((data) => {
    const { token, ...userData } = data
    localStorage.setItem('ro_token', token)
    localStorage.setItem('ro_user', JSON.stringify(userData))
    setUser(userData)
  }, [])

  const login = useCallback(
    async (email, password) => {
      const data = await authApi.login({ email, password })
      persistSession(data)
      return data
    },
    [persistSession]
  )

  const register = useCallback(
    async (payload) => {
      const data = await authApi.register(payload)
      persistSession(data)
      return data
    },
    [persistSession]
  )

  const logout = useCallback(() => {
    localStorage.removeItem('ro_token')
    localStorage.removeItem('ro_user')
    setUser(null)
  }, [])

  const getToken = useCallback(() => localStorage.getItem('ro_token'), [])

  return (
    <AuthContext.Provider value={{ user, isLoading, login, register, logout, getToken }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return ctx
}
