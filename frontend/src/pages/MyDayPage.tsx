import { Lightbulb, Plus, Sparkles, X } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { listApi } from '../api/lists'
import { getApiErrorMessage } from '../api/client'
import { taskApi } from '../api/tasks'
import { TaskDialog } from '../components/TaskDialog'
import { TaskList } from '../components/TaskList'
import { LoadingState } from '../components/LoadingState'
import { QuickTaskInput } from '../components/QuickTaskInput'
import { useToast } from '../context/ToastContext'
import type { Task, TaskList as TaskListModel } from '../types/api'
import { greeting } from '../utils/dates'

export function MyDayPage() {
  const [tasks, setTasks] = useState<Task[]>([])
  const [lists, setLists] = useState<TaskListModel[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [suggestions, setSuggestions] = useState<Task[]>([])
  const [showSuggestions, setShowSuggestions] = useState(false)
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [selectedTask, setSelectedTask] = useState<Task | null>(null)
  const { showToast } = useToast()

  const load = useCallback(async () => {
    try {
      setError('')
      const [myDayTasks, taskLists, pendingTasks] = await Promise.all([
        taskApi.getMyDay(),
        listApi.getAll(),
        taskApi.getPage({ size: 25, status: 'PENDING', sortBy: 'dueDate', direction: 'ASC' }),
      ])
      setTasks(myDayTasks)
      setLists(taskLists)
      setSuggestions(pendingTasks.content.filter((task) => !task.myDayDate).slice(0, 8))
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to load My Day.'))
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => { void load() }, [load])

  useEffect(() => {
    function refreshTasks() {
      void load()
    }

    window.addEventListener('notifyhub:tasks-changed', refreshTasks)
    return () => window.removeEventListener('notifyhub:tasks-changed', refreshTasks)
  }, [load])

  async function toggleTask(task: Task) {
    try {
      if (task.status === 'COMPLETED') {
        await taskApi.reopen(task.id)
        showToast('Task reopened', { kind: 'info' })
      } else {
        await taskApi.complete(task.id)
        showToast('Task completed', { actionLabel: 'Undo', onAction: () => { void taskApi.reopen(task.id).then(load) } })
      }
      await load()
      window.dispatchEvent(new Event('notifyhub:tasks-changed'))
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to update this task.'))
    }
  }

  async function toggleImportant(task: Task) {
    try {
      const isImportant = task.priority === 'HIGH'
      await taskApi.update(task.id, { title: task.title, notes: task.notes, priority: isImportant ? 'MEDIUM' : 'HIGH', dueDate: task.dueDate, taskListId: task.taskListId })
      await load()
      window.dispatchEvent(new Event('notifyhub:tasks-changed'))
      showToast(isImportant ? 'Removed from Important' : 'Marked as important')
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to update task importance.'))
    }
  }

  async function removeTaskFromMyDay(task: Task) {
    try {
      await taskApi.removeFromMyDay(task.id)
      await load()
      window.dispatchEvent(new Event('notifyhub:tasks-changed'))
      showToast('Removed from My Day', { kind: 'info' })
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to remove this task from My Day.'))
    }
  }

  function openTask(task: Task | null) {
    setSelectedTask(task)
    setIsDialogOpen(true)
  }

  async function addSuggestion(task: Task) {
    try {
      await taskApi.addToMyDay(task.id)
      await load()
      window.dispatchEvent(new Event('notifyhub:tasks-changed'))
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to add this task to My Day.'))
    }
  }

  const date = new Intl.DateTimeFormat(undefined, { weekday: 'long', month: 'long', day: 'numeric' }).format(new Date())
  const completedCount = tasks.filter((task) => task.status === 'COMPLETED').length
  const openCount = tasks.length - completedCount

  return (
    <section className="workspace-page my-day-page">
      <header className="list-header my-day-header">
        <div className="my-day-heading">
          <p className="my-day-date">{date}</p>
          <h1><Sparkles size={32} /> My Day</h1>
          <span className="my-day-greeting">{greeting()}.</span>
        </div>
        <div className="my-day-actions">
          <div className="my-day-summary" aria-label={`${openCount} open tasks and ${completedCount} completed tasks`}>
            <div><strong>{openCount}</strong><span>to do</span></div>
            <div><strong>{completedCount}</strong><span>done</span></div>
          </div>
          <button aria-expanded={showSuggestions} className="suggestions-button" onClick={() => setShowSuggestions((value) => !value)} type="button"><Lightbulb size={18} /> Suggestions</button>
        </div>
      </header>

      {showSuggestions && <section className="suggestions-pane" aria-label="Suggested tasks">
        <header><div><strong>Suggestions</strong><span>Choose tasks to focus on today.</span></div><button aria-label="Close suggestions" onClick={() => setShowSuggestions(false)} type="button"><X size={18} /></button></header>
        {suggestions.length ? <div>{suggestions.map((task) => <div className="suggestion-row" key={task.id}><span>{task.title}</span><button onClick={() => void addSuggestion(task)} type="button"><Plus size={15} /> Add to My Day</button></div>)}</div> : <p>No open tasks to suggest right now.</p>}
      </section>}

      <div className="task-workspace-card todo-list-card">
          {error && <p className="page-error" role="alert">{error}</p>}
          <QuickTaskInput addToMyDay onCreated={() => void load()} />
          {isLoading ? <LoadingState /> : <TaskList emptyAction={<button className="empty-state-action" onClick={() => openTask(null)} type="button"><Plus size={16} /> Plan your first task</button>} emptyMessage="A clear day is a good start. Add one thing that matters to you." emptyTitle="Your day is clear" onOpen={openTask} onToggle={toggleTask} onToggleImportant={toggleImportant} onToggleMyDay={removeTaskFromMyDay} tasks={tasks} />}
      </div>

      {isDialogOpen && <TaskDialog addToMyDay initialListId={null} lists={lists} onClose={() => setIsDialogOpen(false)} onSaved={() => { void load(); window.dispatchEvent(new Event('notifyhub:tasks-changed')) }} task={selectedTask} />}
    </section>
  )
}
