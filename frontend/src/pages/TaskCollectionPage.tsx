import { ChevronDown, Filter, X } from 'lucide-react'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { useParams } from 'react-router-dom'
import { getApiErrorMessage } from '../api/client'
import { listApi } from '../api/lists'
import { taskApi } from '../api/tasks'
import { LoadingState } from '../components/LoadingState'
import { TaskDialog } from '../components/TaskDialog'
import { TaskList } from '../components/TaskList'
import { QuickTaskInput } from '../components/QuickTaskInput'
import { useToast } from '../context/ToastContext'
import type { Task, TaskList as TaskListModel, TaskPriority, TaskStatus } from '../types/api'

type CollectionKind = 'all' | 'list' | 'planned' | 'important' | 'completed'

interface TaskCollectionPageProps {
  kind: CollectionKind
}

export function TaskCollectionPage({ kind }: TaskCollectionPageProps) {
  const { listId } = useParams()
  const [tasks, setTasks] = useState<Task[]>([])
  const [lists, setLists] = useState<TaskListModel[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [status, setStatus] = useState<TaskStatus | ''>('')
  const [priority, setPriority] = useState<TaskPriority | ''>('')
  const [isDialogOpen, setIsDialogOpen] = useState(false)
  const [selectedTask, setSelectedTask] = useState<Task | null>(null)
  const [showCompleted, setShowCompleted] = useState(kind === 'completed')
  const [areFiltersOpen, setAreFiltersOpen] = useState(false)
  const { showToast } = useToast()
  const numericListId = listId ? Number(listId) : undefined

  const currentList = useMemo(() => lists.find((list) => list.id === numericListId), [lists, numericListId])
  const pageTitle = kind === 'all' ? 'Tasks' : kind === 'planned' ? 'Planned' : kind === 'important' ? 'Important' : kind === 'completed' ? 'Completed' : currentList?.name ?? 'List'
  const pageDescription = kind === 'all' ? 'Everything you have on your plate.' : kind === 'planned' ? 'Tasks with a date worth looking ahead to.' : kind === 'important' ? 'Tasks you marked as important.' : kind === 'completed' ? 'A record of what you have finished.' : 'Keep this part of your work together.'

  const load = useCallback(async () => {
    try {
      setError('')
      const [page, taskLists] = await Promise.all([
        taskApi.getPage({
          size: 100,
          sortBy: kind === 'planned' ? 'dueDate' : 'createdAt',
          direction: kind === 'planned' ? 'ASC' : 'DESC',
          status: kind === 'completed' ? 'COMPLETED' : status || undefined,
          priority: kind === 'important' ? 'HIGH' : priority || undefined,
          taskListId: kind === 'list' ? numericListId : undefined,
        }),
        listApi.getAll(),
      ])
      const result = kind === 'planned' ? page.content.filter((task) => task.dueDate) : page.content
      setTasks(result)
      setLists(taskLists)
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to load tasks.'))
    } finally {
      setIsLoading(false)
    }
  }, [kind, numericListId, priority, status])

  useEffect(() => { void load() }, [load])

  useEffect(() => {
    function refreshTasks() {
      void load()
    }

    window.addEventListener('notifyhub:tasks-changed', refreshTasks)
    return () => window.removeEventListener('notifyhub:tasks-changed', refreshTasks)
  }, [load])

  useEffect(() => {
    setShowCompleted(kind === 'completed')
  }, [kind])

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
      await taskApi.update(task.id, {
        title: task.title,
        notes: task.notes,
        priority: isImportant ? 'MEDIUM' : 'HIGH',
        dueDate: task.dueDate,
        taskListId: task.taskListId,
      })
      await load()
      window.dispatchEvent(new Event('notifyhub:tasks-changed'))
      showToast(isImportant ? 'Removed from Important' : 'Marked as important')
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to update task importance.'))
    }
  }

  async function toggleMyDay(task: Task) {
    try {
      if (task.myDayDate) {
        await taskApi.removeFromMyDay(task.id)
        showToast('Removed from My Day', { kind: 'info' })
      } else {
        await taskApi.addToMyDay(task.id)
        showToast('Added to My Day')
      }
      await load()
      window.dispatchEvent(new Event('notifyhub:tasks-changed'))
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to update My Day.'))
    }
  }

  async function deleteTask(task: Task) {
    if (!window.confirm(`Delete “${task.title}”? This cannot be undone.`)) return
    try {
      await taskApi.remove(task.id)
      await load()
      window.dispatchEvent(new Event('notifyhub:tasks-changed'))
      showToast('Task deleted', { kind: 'info' })
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to delete this task.'))
    }
  }

  function openTask(task: Task | null) {
    setSelectedTask(task)
    setIsDialogOpen(true)
  }

  function getPlannedGroups() {
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    const tomorrow = new Date(today)
    tomorrow.setDate(tomorrow.getDate() + 1)
    const nextWeek = new Date(today)
    nextWeek.setDate(nextWeek.getDate() + 7)
    const groups: Array<{ label: string; tasks: Task[] }> = [
      { label: 'Today', tasks: [] },
      { label: 'Tomorrow', tasks: [] },
      { label: 'This week', tasks: [] },
      { label: 'Later', tasks: [] },
    ]

    tasks.forEach((task) => {
      const dueDate = task.dueDate ? new Date(task.dueDate) : null
      if (!dueDate || dueDate < tomorrow) groups[0].tasks.push(task)
      else if (dueDate < new Date(tomorrow.getTime() + 86_400_000)) groups[1].tasks.push(task)
      else if (dueDate < nextWeek) groups[2].tasks.push(task)
      else groups[3].tasks.push(task)
    })
    return groups.filter((group) => group.tasks.length)
  }

  const activeTasks = kind === 'all' ? tasks.filter((task) => task.status !== 'COMPLETED') : tasks
  const completedTasks = kind === 'all' ? tasks.filter((task) => task.status === 'COMPLETED') : []
  const taskListProps = { onDelete: deleteTask, onOpen: openTask, onToggle: toggleTask, onToggleImportant: toggleImportant, onToggleMyDay: toggleMyDay }
  const activeFilterCount = Number(Boolean(status)) + Number(Boolean(priority))
  const pageTaskCount = kind === 'all' ? activeTasks.length : tasks.length

  function clearFilters() {
    setStatus('')
    setPriority('')
  }

  return (
    <section className="workspace-page">
      <header className="page-header">
        <div>
          <p className="eyebrow">{kind === 'list' ? 'Custom list' : kind === 'planned' ? 'Your schedule' : kind === 'important' ? 'Priority tasks' : kind === 'completed' ? 'Finished tasks' : 'Task workspace'}</p>
          <h1>{pageTitle}</h1>
          <p>{pageDescription}</p>
          {!isLoading && <span className="page-summary">{pageTaskCount} {pageTaskCount === 1 ? 'task' : 'tasks'} {kind === 'completed' ? 'finished' : 'in view'}</span>}
        </div>
      </header>

      {kind !== 'completed' && <div className="task-list-actions">
        <button aria-expanded={areFiltersOpen} className={`filter-trigger ${activeFilterCount ? 'has-active-filters' : ''}`} onClick={() => setAreFiltersOpen((value) => !value)} type="button">
          <Filter size={16} /> Filters {activeFilterCount > 0 && <span>{activeFilterCount}</span>}
        </button>
        {activeFilterCount > 0 && <button className="clear-filters" onClick={clearFilters} type="button"><X size={15} /> Clear</button>}
      </div>}

      {areFiltersOpen && kind !== 'completed' && <div className="filter-panel">
        <label>Status
          <select onChange={(event) => setStatus(event.target.value as TaskStatus | '')} value={status}>
            <option value="">All statuses</option>
            <option value="PENDING">Open</option>
            <option value="COMPLETED">Completed</option>
          </select>
        </label>
        <label>Priority
          <select onChange={(event) => setPriority(event.target.value as TaskPriority | '')} value={priority}>
            <option value="">All priorities</option>
            <option value="HIGH">Important</option>
            <option value="MEDIUM">Medium</option>
            <option value="LOW">Low</option>
          </select>
        </label>
      </div>}

      {activeFilterCount > 0 && <div aria-label="Active filters" className="active-filter-chips">
        {status && <button onClick={() => setStatus('')} type="button">{status === 'PENDING' ? 'Open' : 'Completed'} <X size={13} /></button>}
        {priority && <button onClick={() => setPriority('')} type="button">{priority[0]}{priority.slice(1).toLowerCase()} priority <X size={13} /></button>}
      </div>}

      <div className="task-workspace-card">
        {error && <p className="page-error" role="alert">{error}</p>}
        {kind !== 'completed' && <QuickTaskInput taskListId={kind === 'list' ? numericListId : null} onCreated={() => void load()} />}
        {isLoading ? <LoadingState /> : kind === 'planned' ? getPlannedGroups().length ? <div className="planned-groups">{getPlannedGroups().map((group) => <section className="planned-group" key={group.label}><h2>{group.label}</h2><TaskList emptyMessage="" emptyTitle="" tasks={group.tasks} {...taskListProps} /></section>)}</div> : <TaskList emptyMessage="Give a task a due date and it will appear here." emptyTitle="Nothing planned" tasks={[]} {...taskListProps} /> : <>
          <TaskList emptyMessage="Create a task or adjust your filters to see more." emptyTitle="Nothing here yet" tasks={activeTasks} {...taskListProps} />
          {completedTasks.length > 0 && <section className="completed-section">
            <button aria-expanded={showCompleted} onClick={() => setShowCompleted((value) => !value)} type="button"><ChevronDown className={showCompleted ? 'is-open' : ''} size={17} /> Completed <span>{completedTasks.length}</span></button>
            {showCompleted && <TaskList emptyMessage="" emptyTitle="" tasks={completedTasks} {...taskListProps} />}
          </section>}
        </>}
      </div>

      {isDialogOpen && <TaskDialog initialListId={kind === 'list' ? numericListId : null} lists={lists} onClose={() => setIsDialogOpen(false)} onSaved={() => { void load(); window.dispatchEvent(new Event('notifyhub:tasks-changed')) }} task={selectedTask} />}
    </section>
  )
}
