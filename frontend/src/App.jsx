import { useState } from 'react'
import './App.css'

export default function App() {
  const [file, setFile] = useState(null)
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState(null)
  const [error, setError] = useState(null)

  function handleFileChange(e) {
    const selected = e.target.files?.[0]
    setFile(selected)
    setMessage(null)
    setError(null)
  }

  async function handleUpload() {
    if (!file) {
      setError('Bitte zuerst eine ZIP-Datei auswählen.')
      return
    }

    const formData = new FormData()
    formData.append('file', file)

    setLoading(true)
    setMessage(null)
    setError(null)
    try {
      const res = await fetch('/api/skill/upload', {
        method: 'POST',
        body: formData,
      })
      const text = await res.text()
      if (!res.ok) throw new Error(text || 'Fehler beim Upload')
      setMessage(text)
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="app">
      <h1>Skill ZIP-Upload</h1>

      <div className="upload">
        <input
          type="file"
          accept=".zip,application/zip"
          onChange={handleFileChange}
        />
        <button onClick={handleUpload} disabled={loading || !file}>
          {loading ? 'Lädt...' : 'Hochladen'}
        </button>
      </div>

      {file && (
        <p className="file-info">
          Ausgewählt: <strong>{file.name}</strong> ({(file.size / 1024).toFixed(1)} KB)
        </p>
      )}

      {message && <p className="success">{message}</p>}
      {error && <p className="error">{error}</p>}
    </main>
  )
}
