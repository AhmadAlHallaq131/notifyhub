export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH'
export type TaskStatus = 'PENDING' | 'COMPLETED'

export interface User {
  id: number
  email: string
  createdAt: string
}

export interface LoginResponse {
  accessToken: string
  tokenType: string
  expiresIn: number
}

export interface Task {
  id: number
  title: string
  notes: string | null
  priority: TaskPriority
  status: TaskStatus
  dueDate: string | null
  completedAt: string | null
  myDayDate: string | null
  taskListId: number | null
  createdAt: string
  updatedAt: string
  tagIds: number[]
}

export interface TaskList {
  id: number
  name: string
  createdAt: string
  updatedAt: string
}

export interface Notification {
  id: number
  type: 'REMINDER' | string
  title: string
  message: string
  read: boolean
  readAt: string | null
  createdAt: string
}

export type ReminderRepeatFrequency = 'NONE' | 'DAILY' | 'WEEKLY' | 'MONTHLY'

export interface Reminder {
  id: number
  taskId: number
  remindAt: string
  repeatFrequency: ReminderRepeatFrequency
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface Tag {
  id: number
  name: string
  createdAt: string
  updatedAt: string
}

export interface SubTask {
  id: number
  title: string
  completed: boolean
  completedAt: string | null
  taskId: number
  createdAt: string
  updatedAt: string
}

export interface PageResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  first: boolean
  last: boolean
}

export interface TaskPayload {
  title: string
  notes: string | null
  priority: TaskPriority
  dueDate: string | null
  taskListId: number | null
}
