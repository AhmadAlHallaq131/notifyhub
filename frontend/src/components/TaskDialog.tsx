import { useEffect, useState } from 'react'
import { BellRing, CalendarClock, Check, ChevronDown, Circle, ListTodo, Plus, SlidersHorizontal, Sun, Tag, Trash2, X } from 'lucide-react'
import { reminderApi } from '../api/reminders'
import { subTaskApi } from '../api/subtasks'
import { tagApi } from '../api/tags'
import { taskApi } from '../api/tasks'
import { getApiErrorMessage } from '../api/client'
import type { Reminder, ReminderRepeatFrequency, SubTask, Tag as TagModel, Task, TaskList, TaskPayload, TaskPriority } from '../types/api'
import { toDateTimeLocal } from '../utils/dates'
import { IconButton } from './IconButton'
import { useToast } from '../context/ToastContext'

interface TaskDialogProps {
  task: Task | null
  lists: TaskList[]
  initialListId?: number | null
  addToMyDay?: boolean
  onClose: () => void
  onSaved: () => void
}

const priorities: TaskPriority[] = ['LOW', 'MEDIUM', 'HIGH']
const repeatFrequencies: ReminderRepeatFrequency[] = ['NONE', 'DAILY', 'WEEKLY', 'MONTHLY']

export function TaskDialog({ task, lists, initialListId = null, addToMyDay = false, onClose, onSaved }: TaskDialogProps) {
  const [title, setTitle] = useState('')
  const [notes, setNotes] = useState('')
  const [priority, setPriority] = useState<TaskPriority>('MEDIUM')
  const [dueDate, setDueDate] = useState('')
  const [taskListId, setTaskListId] = useState<string>('')
  const [reminder, setReminder] = useState<Reminder | null>(null)
  const [removedReminderId, setRemovedReminderId] = useState<number | null>(null)
  const [remindAt, setRemindAt] = useState('')
  const [repeatFrequency, setRepeatFrequency] = useState<ReminderRepeatFrequency>('NONE')
  const [tags, setTags] = useState<TagModel[]>([])
  const [selectedTagIds, setSelectedTagIds] = useState<number[]>([])
  const [newTagName, setNewTagName] = useState('')
  const [subTasks, setSubTasks] = useState<SubTask[]>([])
  const [newSubTaskTitle, setNewSubTaskTitle] = useState('')
  const [isSaving, setIsSaving] = useState(false)
  const [error, setError] = useState('')
  const [showDetails, setShowDetails] = useState(Boolean(task))
  const { showToast } = useToast()

  useEffect(() => {
    setTitle(task?.title ?? '')
    setNotes(task?.notes ?? '')
    setPriority(task?.priority ?? 'MEDIUM')
    setDueDate(toDateTimeLocal(task?.dueDate ?? null))
    setTaskListId(String(task?.taskListId ?? initialListId ?? ''))
    setReminder(null)
    setRemovedReminderId(null)
    setRemindAt('')
    setRepeatFrequency('NONE')
    setSelectedTagIds(task?.tagIds ?? [])
    setNewTagName('')
    setSubTasks([])
    setNewSubTaskTitle('')
    setError('')
    setShowDetails(Boolean(task))
  }, [initialListId, task])

  useEffect(() => {
    void tagApi.getAll().then(setTags).catch(() => setError('Unable to load your tags.'))
  }, [])

  useEffect(() => {
    if (!task) return

    let isCurrent = true
    void reminderApi.getForTask(task.id)
      .then((reminders) => {
        if (!isCurrent) return
        const nextReminder = reminders.find((item) => item.active) ?? reminders[0] ?? null
        setReminder(nextReminder)
        setRemindAt(toDateTimeLocal(nextReminder?.remindAt ?? null))
        setRepeatFrequency(nextReminder?.repeatFrequency ?? 'NONE')
      })
      .catch(() => {
        if (isCurrent) setError('Unable to load this task reminder.')
      })

    return () => { isCurrent = false }
  }, [task])

  useEffect(() => {
    function closeOnEscape(event: KeyboardEvent) {
      if (event.key === 'Escape') onClose()
    }
    window.addEventListener('keydown', closeOnEscape)
    return () => window.removeEventListener('keydown', closeOnEscape)
  }, [onClose])

  useEffect(() => {
    if (!task) return

    let isCurrent = true
    void subTaskApi.getForTask(task.id)
      .then((items) => { if (isCurrent) setSubTasks(items) })
      .catch(() => { if (isCurrent) setError('Unable to load this task checklist.') })

    return () => { isCurrent = false }
  }, [task])

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')

    if (!title.trim()) {
      setError('A task title is required.')
      return
    }

    const payload: TaskPayload = {
      title: title.trim(),
      notes: notes.trim() || null,
      priority,
      dueDate: dueDate || null,
      taskListId: taskListId ? Number(taskListId) : null,
    }

    try {
      setIsSaving(true)
      const savedTask = task ? await taskApi.update(task.id, payload) : await taskApi.create(payload)
      if (!task && addToMyDay) {
        await taskApi.addToMyDay(savedTask.id)
      }

      const previousTagIds = savedTask.tagIds
      const tagsToAdd = selectedTagIds.filter((tagId) => !previousTagIds.includes(tagId))
      const tagsToRemove = previousTagIds.filter((tagId) => !selectedTagIds.includes(tagId))
      await Promise.all([
        ...tagsToAdd.map((tagId) => taskApi.addTag(savedTask.id, tagId)),
        ...tagsToRemove.map((tagId) => taskApi.removeTag(savedTask.id, tagId)),
      ])

      if (remindAt) {
        const reminderPayload = { remindAt, repeatFrequency }
        if (reminder) {
          await reminderApi.update(reminder.id, reminderPayload)
        } else {
          if (removedReminderId) await reminderApi.remove(removedReminderId)
          await reminderApi.create(savedTask.id, reminderPayload)
        }
      } else if (removedReminderId) {
        await reminderApi.remove(removedReminderId)
      }
      onSaved()
      showToast(task ? 'Task changes saved' : 'Task created')
      onClose()
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to save this task.'))
    } finally {
      setIsSaving(false)
    }
  }

  function clearReminder() {
    if (reminder) setRemovedReminderId(reminder.id)
    setReminder(null)
    setRemindAt('')
    setRepeatFrequency('NONE')
  }

  function toggleTag(tagId: number) {
    setSelectedTagIds((current) => current.includes(tagId) ? current.filter((id) => id !== tagId) : [...current, tagId])
  }

  async function createTag() {
    const name = newTagName.trim()
    if (!name) return

    try {
      const createdTag = await tagApi.create(name)
      setTags((current) => [...current, createdTag])
      setSelectedTagIds((current) => [...current, createdTag.id])
      setNewTagName('')
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to create this tag.'))
    }
  }

  async function addSubTask() {
    if (!task || !newSubTaskTitle.trim()) return

    try {
      const createdSubTask = await subTaskApi.create(task.id, newSubTaskTitle.trim())
      setSubTasks((current) => [...current, createdSubTask])
      setNewSubTaskTitle('')
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to add this checklist item.'))
    }
  }

  async function toggleSubTask(subTask: SubTask) {
    try {
      const updatedSubTask = subTask.completed ? await subTaskApi.reopen(subTask.id) : await subTaskApi.complete(subTask.id)
      setSubTasks((current) => current.map((item) => item.id === updatedSubTask.id ? updatedSubTask : item))
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to update this checklist item.'))
    }
  }

  async function deleteSubTask(subTaskId: number) {
    try {
      await subTaskApi.remove(subTaskId)
      setSubTasks((current) => current.filter((item) => item.id !== subTaskId))
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to delete this checklist item.'))
    }
  }

  async function deleteTask() {
    if (!task || !window.confirm(`Delete “${task.title}”? This cannot be undone.`)) return

    try {
      setIsSaving(true)
      await taskApi.remove(task.id)
      onSaved()
      showToast('Task deleted', { kind: 'info' })
      onClose()
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to delete this task.'))
    } finally {
      setIsSaving(false)
    }
  }

  async function toggleMyDay() {
    if (!task) return

    try {
      setIsSaving(true)
      if (task.myDayDate) {
        await taskApi.removeFromMyDay(task.id)
      } else {
        await taskApi.addToMyDay(task.id)
      }
      onSaved()
      showToast(task.myDayDate ? 'Removed from My Day' : 'Added to My Day')
      onClose()
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to update My Day.'))
    } finally {
      setIsSaving(false)
    }
  }

  async function toggleTaskCompletion() {
    if (!task) return

    try {
      setIsSaving(true)
      if (task.status === 'COMPLETED') {
        await taskApi.reopen(task.id)
      } else {
        await taskApi.complete(task.id)
      }
      onSaved()
      showToast(task.status === 'COMPLETED' ? 'Task reopened' : 'Task completed')
      onClose()
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Unable to update this task.'))
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <div className="dialog-backdrop" onMouseDown={onClose} role="presentation">
      <section aria-labelledby="task-dialog-title" className="task-dialog" onMouseDown={(event) => event.stopPropagation()}>
        <header className="dialog-header">
          <div>
            <p className="eyebrow">{task ? 'Edit task' : 'New task'}</p>
            <h2 id="task-dialog-title">{task ? 'Keep the details clear' : 'What needs your attention?'}</h2>
          </div>
          <IconButton label="Close task dialog" onClick={onClose}><X size={20} /></IconButton>
        </header>

        <form className="task-form" onSubmit={handleSubmit}>
          {task && <div className="task-quick-actions">
            <button onClick={() => void toggleTaskCompletion()} type="button"><Circle size={17} /> {task.status === 'COMPLETED' ? 'Mark as not completed' : 'Mark as completed'}</button>
            <button onClick={() => void toggleMyDay()} type="button"><Sun size={17} /> {task.myDayDate ? 'Remove from My Day' : 'Add to My Day'}</button>
          </div>}
          <label className="task-title-field">
            Task title
            <input autoFocus maxLength={200} onChange={(event) => setTitle(event.target.value)} placeholder="For example, prepare the project update" value={title} />
          </label>

          <label className="task-notes-field">
            Notes <span>Optional</span>
            <textarea maxLength={5000} onChange={(event) => setNotes(event.target.value)} placeholder="Add useful context, links, or next steps." rows={3} value={notes} />
          </label>

          <div className="task-dialog-toolbar">
            <label className="compact-priority-field">
              <span>Priority</span>
              <select aria-label="Priority" onChange={(event) => setPriority(event.target.value as TaskPriority)} value={priority}>
                {priorities.map((value) => <option key={value} value={value}>{value[0]}{value.slice(1).toLowerCase()}</option>)}
              </select>
            </label>
            <button aria-expanded={showDetails} className="details-toggle" onClick={() => setShowDetails((value) => !value)} type="button"><SlidersHorizontal size={16} /> {showDetails ? 'Hide details' : 'Add details'}<ChevronDown className={showDetails ? 'is-open' : ''} size={15} /></button>
          </div>

          {showDetails && <div className="task-details-panel">
            <section className="task-detail-section">
              <div className="task-detail-heading"><CalendarClock size={16} /><div><strong>Schedule</strong><span>Keep this task on your radar.</span></div></div>
              <label>
                Due date <span>Optional</span>
                <input onChange={(event) => setDueDate(event.target.value)} type="datetime-local" value={dueDate} />
              </label>
            </section>

            <section className="task-detail-section">
              <div className="task-detail-heading"><BellRing size={16} /><div><strong>Reminder</strong><span>Get a notification when it is time.</span></div></div>
              <div className="form-grid">
                <label>
                  Remind me at
                  <input onChange={(event) => setRemindAt(event.target.value)} type="datetime-local" value={remindAt} />
                </label>
                <label>
                  Repeat
                  <select disabled={!remindAt} onChange={(event) => setRepeatFrequency(event.target.value as ReminderRepeatFrequency)} value={repeatFrequency}>
                    {repeatFrequencies.map((value) => <option key={value} value={value}>{value[0]}{value.slice(1).toLowerCase()}</option>)}
                  </select>
                </label>
              </div>
              {(remindAt || reminder) && <button className="text-button" onClick={clearReminder} type="button"><Trash2 size={15} /> Remove reminder</button>}
            </section>

            <section className="task-detail-section">
              <div className="task-detail-heading"><Tag size={16} /><div><strong>Organize</strong><span>Add this task to a list or label it.</span></div></div>
              <label>
                List <span>Optional</span>
                <select onChange={(event) => setTaskListId(event.target.value)} value={taskListId}>
                  <option value="">No list</option>
                  {lists.map((list) => <option key={list.id} value={list.id}>{list.name}</option>)}
                </select>
              </label>
              <div className="tag-options">
                {tags.map((item) => <button className={`tag-option ${selectedTagIds.includes(item.id) ? 'is-selected' : ''}`} key={item.id} onClick={() => toggleTag(item.id)} type="button"><Check size={13} /> {item.name}</button>)}
                {!tags.length && <span className="form-note">Create labels to group related tasks.</span>}
              </div>
              <div className="inline-create">
                <input aria-label="New label name" maxLength={100} onChange={(event) => setNewTagName(event.target.value)} onKeyDown={(event) => { if (event.key === 'Enter') { event.preventDefault(); void createTag() } }} placeholder="New label" value={newTagName} />
                <button className="button button-secondary" onClick={() => void createTag()} type="button"><Plus size={15} /> Add</button>
              </div>
            </section>
          </div>}

          {task && <div className="subtask-fields">
            <div className="label-with-icon"><ListTodo size={15} /> Steps</div>
            <div className="subtask-list">
              {subTasks.map((subTask) => <div className={`subtask-row ${subTask.completed ? 'is-complete' : ''}`} key={subTask.id}>
                <button aria-label={subTask.completed ? `Reopen ${subTask.title}` : `Complete ${subTask.title}`} className="subtask-check" onClick={() => void toggleSubTask(subTask)} type="button">{subTask.completed && <Check size={13} />}</button>
                <span>{subTask.title}</span>
                <IconButton label={`Delete ${subTask.title}`} onClick={() => void deleteSubTask(subTask.id)}><Trash2 size={15} /></IconButton>
              </div>)}
            </div>
            <div className="inline-create">
              <input aria-label="New checklist item" maxLength={200} onChange={(event) => setNewSubTaskTitle(event.target.value)} onKeyDown={(event) => { if (event.key === 'Enter') { event.preventDefault(); void addSubTask() } }} placeholder="Add a step" value={newSubTaskTitle} />
              <button className="button button-secondary" onClick={() => void addSubTask()} type="button"><Plus size={15} /> Add</button>
            </div>
          </div>}

          {error && <p className="form-error" role="alert">{error}</p>}

          <footer className="dialog-actions">
            {task && <button className="button button-danger" disabled={isSaving} onClick={() => void deleteTask()} type="button"><Trash2 size={16} /> Delete</button>}
            <button className="button button-secondary" onClick={onClose} type="button">Cancel</button>
            <button className="button button-primary" disabled={isSaving} type="submit">{isSaving ? 'Saving...' : task ? 'Save changes' : 'Create task'}</button>
          </footer>
        </form>
      </section>
    </div>
  )
}
