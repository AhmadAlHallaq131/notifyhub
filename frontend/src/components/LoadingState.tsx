export function LoadingState({ label = 'Loading your tasks...' }: { label?: string }) {
  return <div aria-label={label} className="task-skeleton" role="status">
    <span /><span /><span /><span />
    <small>{label}</small>
  </div>
}
