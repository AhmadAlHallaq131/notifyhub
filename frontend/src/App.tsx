import { Navigate, Route, Routes } from 'react-router-dom'
import { DashboardLayout } from './components/DashboardLayout'
import { ProtectedRoute, PublicOnlyRoute } from './components/RouteGuards'
import { AuthPage } from './pages/AuthPage'
import { MyDayPage } from './pages/MyDayPage'
import { SearchPage } from './pages/SearchPage'
import { TaskCollectionPage } from './pages/TaskCollectionPage'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<PublicOnlyRoute><AuthPage /></PublicOnlyRoute>} />
      <Route path="/register" element={<PublicOnlyRoute><AuthPage /></PublicOnlyRoute>} />
      <Route element={<ProtectedRoute><DashboardLayout /></ProtectedRoute>}>
        <Route index element={<Navigate replace to="/my-day" />} />
        <Route path="/my-day" element={<MyDayPage />} />
        <Route path="/important" element={<TaskCollectionPage kind="important" />} />
        <Route path="/tasks" element={<TaskCollectionPage kind="all" />} />
        <Route path="/planned" element={<TaskCollectionPage kind="planned" />} />
        <Route path="/completed" element={<TaskCollectionPage kind="completed" />} />
        <Route path="/lists/:listId" element={<TaskCollectionPage kind="list" />} />
        <Route path="/search" element={<SearchPage />} />
      </Route>
      <Route path="*" element={<Navigate replace to="/my-day" />} />
    </Routes>
  )
}
