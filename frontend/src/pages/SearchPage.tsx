import { Plus, Search, X } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { getApiErrorMessage } from '../api/client'
import { taskApi } from '../api/tasks'
import { TaskList } from '../components/TaskList'
import { LoadingState } from '../components/LoadingState'
import { TaskDialog } from '../components/TaskDialog'
import { listApi } from '../api/lists'
import type { Task, TaskList as TaskListModel } from '../types/api'

export function SearchPage() {
  const [searchParams] = useSearchParams()
  const query = searchParams.get('q') ?? ''
  const navigate = useNavigate()
  const [draftQuery, setDraftQuery] = useState(query)
  const [tasks, setTasks] = useState<Task[]>([])
  const [lists, setLists] = useState<TaskListModel[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [selectedTask, setSelectedTask] = useState<Task | null>(null)
  const [isDialogOpen, setIsDialogOpen] = useState(false)

  const load = useCallback(async () => {
    if (!query) {
      setTasks([])
      setIsLoading(false)
      return
    }

    try {
      setError('')
      setIsLoading(true)
      const [matchingTasks, taskLists] = await Promise.all([taskApi.search(query), listApi.getAll()])
      setTasks(matchingTasks)
      setLists(taskLists)
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to search tasks.'))
    } finally {
      setIsLoading(false)
    }
  }, [query])

  useEffect(() => { void load() }, [load])

  useEffect(() => {
    function refreshResults() {
      void load()
    }

    window.addEventListener('notifyhub:tasks-changed', refreshResults)
    return () => window.removeEventListener('notifyhub:tasks-changed', refreshResults)
  }, [load])

  useEffect(() => {
    setDraftQuery(query)
  }, [query])

  function updateSearch(value: string) {
    setDraftQuery(value)
    const nextQuery = value.trim()
    navigate(nextQuery ? `/search?q=${encodeURIComponent(nextQuery)}` : '/search', { replace: true })
  }

  function clearSearch() {
    updateSearch('')
  }

  async function toggleTask(task: Task) {
    try {
      if (task.status === 'COMPLETED') {
        await taskApi.reopen(task.id)
      } else {
        await taskApi.complete(task.id)
      }
      await load()
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to update this task.'))
    }
  }

  function openTask(task: Task | null) {
    setSelectedTask(task)
    setIsDialogOpen(true)
  }

  return (
    <section className="workspace-page">
      <header className="page-header">
        <div>
          <p className="eyebrow">Search</p>
          <h1>{query ? `Results for “${query}”` : 'Search tasks'}</h1>
          <p>Search task titles and notes across your workspace.</p>
        </div>
        <button className="button button-primary" onClick={() => openTask(null)} type="button"><Plus size={18} /> Add task</button>
      </header>
      <label className="search-page-field"><Search size={19} /><input autoFocus onChange={(event) => updateSearch(event.target.value)} onKeyDown={(event) => { if (event.key === 'Escape') clearSearch() }} placeholder="Search tasks and notes" value={draftQuery} />{draftQuery ? <button aria-label="Clear search" onClick={clearSearch} type="button"><X size={17} /></button> : <kbd>Esc</kbd>}</label>
      <div className="task-workspace-card">
        {error && <p className="page-error" role="alert">{error}</p>}
        {isLoading ? <LoadingState label="Searching your tasks..." /> : <TaskList emptyMessage={query ? 'Try a different title, note, or keyword.' : 'Start typing above to search your tasks.'} emptyTitle={query ? 'No matching tasks' : 'Search your workspace'} onOpen={openTask} onToggle={toggleTask} query={query} tasks={tasks} />}
      </div>
      {!tasks.length && !isLoading && !error && <p className="search-hint"><Search size={17} /> Search stays inside your own tasks and notes.</p>}
      {isDialogOpen && <TaskDialog lists={lists} onClose={() => setIsDialogOpen(false)} onSaved={() => void load()} task={selectedTask} />}
    </section>
  )
}
