import { CheckCircle2, LockKeyhole, Mail, Sparkles } from 'lucide-react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useState } from 'react'
import { getApiErrorMessage } from '../api/client'
import { useAuth } from '../context/AuthContext'

export function AuthPage() {
  const { login, register } = useAuth()
  const { pathname } = useLocation()
  const navigate = useNavigate()
  const isRegister = pathname === '/register'
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')

    try {
      setIsSubmitting(true)
      if (isRegister) {
        await register(email, password)
      } else {
        await login(email, password)
      }
      navigate('/my-day', { replace: true })
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, isRegister ? 'Unable to create your account.' : 'Unable to sign you in.'))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-panel auth-intro">
        <div className="auth-brand"><span><Sparkles size={21} /></span> NotifyHub</div>
        <div className="auth-intro-copy">
          <p className="eyebrow">A clearer day starts here</p>
          <h1>Make space for what matters.</h1>
          <p>Plan tasks, keep reminders close, and move through your day with a calm, focused view.</p>
        </div>
        <ul className="auth-benefits">
          <li><CheckCircle2 size={19} /> One focused place for every task</li>
          <li><CheckCircle2 size={19} /> Reminders that stay out of your way</li>
          <li><CheckCircle2 size={19} /> Live notifications when timing matters</li>
        </ul>
      </section>

      <section className="auth-panel auth-form-panel">
        <div className="auth-form-wrap">
          <p className="eyebrow">{isRegister ? 'Create your account' : 'Welcome back'}</p>
          <h2>{isRegister ? 'Start with a calmer workspace.' : 'Sign in to NotifyHub.'}</h2>
          <p className="muted-copy">{isRegister ? 'Your account is ready in a moment.' : 'Pick up where you left off.'}</p>

          <form className="auth-form" onSubmit={submit}>
            <label>
              Email
              <span className="input-with-icon"><Mail size={18} /><input autoComplete="email" onChange={(event) => setEmail(event.target.value)} placeholder="you@example.com" required type="email" value={email} /></span>
            </label>
            <label>
              Password
              <span className="input-with-icon"><LockKeyhole size={18} /><input autoComplete={isRegister ? 'new-password' : 'current-password'} minLength={8} onChange={(event) => setPassword(event.target.value)} placeholder="At least 8 characters" required type="password" value={password} /></span>
            </label>
            {error && <p className="form-error" role="alert">{error}</p>}
            <button className="button button-primary auth-submit" disabled={isSubmitting} type="submit">{isSubmitting ? 'Please wait...' : isRegister ? 'Create account' : 'Sign in'}</button>
          </form>

          <p className="auth-switch">
            {isRegister ? 'Already have an account?' : 'New to NotifyHub?'}{' '}
            <Link to={isRegister ? '/login' : '/register'}>{isRegister ? 'Sign in' : 'Create one'}</Link>
          </p>
        </div>
      </section>
    </main>
  )
}
