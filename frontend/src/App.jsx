import { useEffect, useState } from 'react'
import './App.css'

export default function App() {
  const [count, setCount] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  async function fetchCount() {
    try {
      const res = await fetch('/api/counter')
      if (!res.ok) throw new Error('Fehler beim Laden')
      const data = await res.json()
      setCount(data.count)
    } catch (e) {
      setError(e.message)
    }
  }

  async function increment() {
    setLoading(true)
    setError(null)
    try {
      const res = await fetch('/api/counter/increment', { method: 'POST' })
      if (!res.ok) throw new Error('Fehler beim Hochzählen')
      const data = await res.json()
      setCount(data.count)
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  async function reset() {
    setLoading(true)
    setError(null)
    try {
      const res = await fetch('/api/counter/reset', { method: 'POST' })
      if (!res.ok) throw new Error('Fehler beim Zurücksetzen')
      const data = await res.json()
      setCount(data.count)
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchCount()
  }, [])

  return (
    <main className="app">
      <h1>Counter Demo</h1>
      <p className="count">Aktueller Zähler: <strong>{count}</strong></p>
      <div className="buttons">
        <button onClick={increment} disabled={loading}>
          {loading ? '...' : 'Hochzählen'}
        </button>
        <button onClick={reset} disabled={loading}>
          Zurücksetzen
        </button>
      </div>
      {error && <p className="error">{error}</p>}
    </main>
  )
}
