import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { authApi } from '../api/auth'
import type { User } from '../types/api'

interface AuthContextValue {
  user: User | null
  isLoading: boolean
  login: (email: string, password: string) => Promise<void>
  register: (email: string, password: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

const TOKEN_KEY = 'notifyhub.accessToken'

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem(TOKEN_KEY)

    if (!token) {
      setIsLoading(false)
      return
    }

    authApi.getCurrentUser()
      .then(setUser)
      .catch(() => localStorage.removeItem(TOKEN_KEY))
      .finally(() => setIsLoading(false))
  }, [])

  useEffect(() => {
    function clearExpiredSession() {
      localStorage.removeItem(TOKEN_KEY)
      setUser(null)
      setIsLoading(false)
    }

    window.addEventListener('notifyhub:unauthorized', clearExpiredSession)
    return () => window.removeEventListener('notifyhub:unauthorized', clearExpiredSession)
  }, [])

  const value = useMemo<AuthContextValue>(() => ({
    user,
    isLoading,
    async login(email, password) {
      const response = await authApi.login(email, password)
      localStorage.setItem(TOKEN_KEY, response.accessToken)
      const currentUser = await authApi.getCurrentUser()
      setUser(currentUser)
    },
    async register(email, password) {
      await authApi.register(email, password)
      const response = await authApi.login(email, password)
      localStorage.setItem(TOKEN_KEY, response.accessToken)
      const currentUser = await authApi.getCurrentUser()
      setUser(currentUser)
    },
    logout() {
      localStorage.removeItem(TOKEN_KEY)
      setUser(null)
    },
  }), [isLoading, user])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)

  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider')
  }

  return context
}
