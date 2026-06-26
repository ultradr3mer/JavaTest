import { useState } from 'react'
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom'
import SkillList from './SkillList.jsx'
import SkillDetail from './SkillDetail.jsx'
import './App.css'

export default function App() {
  return (
    <BrowserRouter>
      <Layout>
        <Routes>
          <Route path="/" element={<SkillList />} />
          <Route path="/:skillName" element={<SkillDetail />} />
        </Routes>
      </Layout>
    </BrowserRouter>
  )
}

function Layout({ children }) {
  const [drawerOpen, setDrawerOpen] = useState(false)
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
      window.dispatchEvent(new CustomEvent('skills-changed'))
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="layout">
      <header className="topbar">
        <h1><Link to="/">Skill Manager</Link></h1>
        <button className="menu-btn" onClick={() => setDrawerOpen(true)}>
          Upload
        </button>
      </header>

      {children}

      {drawerOpen && (
        <div className="drawer-overlay" onClick={() => setDrawerOpen(false)}>
          <aside className="drawer" onClick={(e) => e.stopPropagation()}>
            <div className="drawer-header">
              <h2>Upload</h2>
              <button className="close-btn" onClick={() => setDrawerOpen(false)}>
                &times;
              </button>
            </div>

            <div className="drawer-body">
              <input
                type="file"
                accept=".zip,application/zip"
                onChange={handleFileChange}
              />
              <button
                onClick={handleUpload}
                disabled={loading || !file}
              >
                {loading ? 'Lädt...' : 'Hochladen'}
              </button>

              {file && (
                <p className="file-info">
                  Ausgewählt: <strong>{file.name}</strong>
                  ({(file.size / 1024).toFixed(1)} KB)
                </p>
              )}
              {message && <p className="success">{message}</p>}
              {error && <p className="error">{error}</p>}
            </div>
          </aside>
        </div>
      )}
    </div>
  )
}
