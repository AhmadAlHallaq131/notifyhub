import type { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { LoadingState } from './LoadingState'

export function ProtectedRoute({ children }: { children: ReactNode }) {
  const { user, isLoading } = useAuth()

  if (isLoading) {
    return <div className="route-loading"><LoadingState label="Opening NotifyHub..." /></div>
  }

  return user ? <>{children}</> : <Navigate replace to="/login" />
}

export function PublicOnlyRoute({ children }: { children: ReactNode }) {
  const { user, isLoading } = useAuth()

  if (isLoading) {
    return <div className="route-loading"><LoadingState label="Opening NotifyHub..." /></div>
  }

  return user ? <Navigate replace to="/my-day" /> : <>{children}</>
}
