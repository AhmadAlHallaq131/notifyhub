import { Bell, CalendarDays, ChevronDown, Plus } from 'lucide-react'
import { useState } from 'react'
import { getApiErrorMessage } from '../api/client'
import { reminderApi } from '../api/reminders'
import { taskApi } from '../api/tasks'
import { useToast } from '../context/ToastContext'
import type { ReminderRepeatFrequency, TaskPriority } from '../types/api'

interface QuickTaskInputProps {
  taskListId?: number | null
  addToMyDay?: boolean
  onCreated: () => void
}

export function QuickTaskInput({ taskListId = null, addToMyDay = false, onCreated }: QuickTaskInputProps) {
  const [title, setTitle] = useState('')
  const [error, setError] = useState('')
  const [isSaving, setIsSaving] = useState(false)
  const [isExpanded, setIsExpanded] = useState(false)
  const [priority, setPriority] = useState<TaskPriority>('MEDIUM')
  const [dueDate, setDueDate] = useState('')
  const [remindAt, setRemindAt] = useState('')
  const { showToast } = useToast()

  function toDateTimeInput(date: Date) {
    const timezoneOffset = date.getTimezoneOffset() * 60_000
    return new Date(date.getTime() - timezoneOffset).toISOString().slice(0, 16)
  }

  function setDueDatePreset(daysFromToday: number) {
    const date = new Date()
    date.setDate(date.getDate() + daysFromToday)
    date.setHours(daysFromToday === 0 ? Math.max(date.getHours() + 1, 18) : 9, 0, 0, 0)
    setDueDate(toDateTimeInput(date))
  }

  async function createTask(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const trimmedTitle = title.trim()
    if (!trimmedTitle) return

    try {
      setError('')
      setIsSaving(true)
      const task = await taskApi.create({
        title: trimmedTitle,
        notes: null,
        priority,
        dueDate: dueDate || null,
        taskListId,
      })
      if (addToMyDay) await taskApi.addToMyDay(task.id)
      if (remindAt) await reminderApi.create(task.id, { remindAt, repeatFrequency: 'NONE' satisfies ReminderRepeatFrequency })
      setTitle('')
      setPriority('MEDIUM')
      setDueDate('')
      setRemindAt('')
      setIsExpanded(false)
      onCreated()
      window.dispatchEvent(new Event('notifyhub:tasks-changed'))
      showToast('Task added')
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to add this task.'))
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <form className={`quick-task-input ${isExpanded ? 'is-expanded' : ''}`} onSubmit={createTask}>
      <div className="quick-task-main"><Plus size={20} />
      <input aria-label="Add a task" disabled={isSaving} maxLength={200} onChange={(event) => setTitle(event.target.value)} onFocus={() => setIsExpanded(true)} placeholder="Add a task" value={title} />
      {title.trim() && <button disabled={isSaving} type="submit">{isSaving ? 'Adding...' : 'Add'}</button>}</div>
      {isExpanded && <div className="quick-task-options">
        <div className="quick-date-presets"><button onClick={() => setDueDatePreset(0)} type="button">Today</button><button onClick={() => setDueDatePreset(1)} type="button">Tomorrow</button></div>
        <label><CalendarDays size={16} /><input aria-label="Due date" onChange={(event) => setDueDate(event.target.value)} type="datetime-local" value={dueDate} /></label>
        <label><Bell size={16} /><input aria-label="Reminder" onChange={(event) => setRemindAt(event.target.value)} type="datetime-local" value={remindAt} /></label>
        <label className="quick-priority"><select aria-label="Priority" onChange={(event) => setPriority(event.target.value as TaskPriority)} value={priority}><option value="LOW">Low</option><option value="MEDIUM">Medium</option><option value="HIGH">Important</option></select><ChevronDown size={15} /></label>
      </div>}
      {error && <span className="quick-task-error" role="alert">{error}</span>}
    </form>
  )
}
