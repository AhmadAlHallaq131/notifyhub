import { CheckCircle2, Info, X } from 'lucide-react'
import { createContext, useCallback, useContext, useMemo, useState } from 'react'

type ToastKind = 'success' | 'info' | 'error'

interface ToastOptions {
  kind?: ToastKind
  actionLabel?: string
  onAction?: () => void
}

interface ToastItem extends Required<Pick<ToastOptions, 'kind'>> {
  id: number
  message: string
  actionLabel?: string
  onAction?: () => void
}

interface ToastContextValue {
  showToast: (message: string, options?: ToastOptions) => void
}

const ToastContext = createContext<ToastContextValue | undefined>(undefined)

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toasts, setToasts] = useState<ToastItem[]>([])

  const dismiss = useCallback((id: number) => {
    setToasts((current) => current.filter((toast) => toast.id !== id))
  }, [])

  const showToast = useCallback((message: string, options: ToastOptions = {}) => {
    const id = Date.now() + Math.floor(Math.random() * 1000)
    setToasts((current) => [...current, { id, message, kind: options.kind ?? 'success', actionLabel: options.actionLabel, onAction: options.onAction }])
    window.setTimeout(() => dismiss(id), 5000)
  }, [dismiss])

  const value = useMemo(() => ({ showToast }), [showToast])

  return <ToastContext.Provider value={value}>
    {children}
    <div aria-live="polite" className="toast-viewport">
      {toasts.map((toast) => <div className={`toast toast-${toast.kind}`} key={toast.id} role="status">
        {toast.kind === 'success' ? <CheckCircle2 size={18} /> : <Info size={18} />}
        <span>{toast.message}</span>
        {toast.actionLabel && <button onClick={() => { toast.onAction?.(); dismiss(toast.id) }} type="button">{toast.actionLabel}</button>}
        <button aria-label="Dismiss notification" className="toast-dismiss" onClick={() => dismiss(toast.id)} type="button"><X size={17} /></button>
      </div>)}
    </div>
  </ToastContext.Provider>
}

export function useToast() {
  const context = useContext(ToastContext)
  if (!context) throw new Error('useToast must be used inside ToastProvider')
  return context
}
