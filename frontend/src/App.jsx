import { useEffect, useState } from 'react'
import './App.css'

export default function App() {
  const [skills, setSkills] = useState({})
  const [selected, setSelected] = useState(null)
  const [detail, setDetail] = useState(null)
  const [activeFile, setActiveFile] = useState(null)
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [file, setFile] = useState(null)
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState(null)
  const [error, setError] = useState(null)
  const [listError, setListError] = useState(null)

  async function loadSkills() {
    setListError(null)
    try {
      const res = await fetch('/api/skill')
      if (!res.ok) throw new Error('Fehler beim Laden der Skills')
      const data = await res.json()
      setSkills(data)
    } catch (e) {
      setListError(e.message)
    }
  }

  useEffect(() => {
    loadSkills()
  }, [])

  async function openSkill(name) {
    setSelected(name)
    setDetail(null)
    setActiveFile(null)
    try {
      const res = await fetch(`/api/skill/${encodeURIComponent(name)}`)
      if (!res.ok) throw new Error('Fehler beim Laden des Skills')
      const data = await res.json()
      setDetail(data)
      const firstFile = Object.keys(data.files || {})[0]
      if (firstFile) setActiveFile(firstFile)
    } catch (e) {
      setListError(e.message)
    }
  }

  function backToList() {
    setSelected(null)
    setDetail(null)
    setActiveFile(null)
  }

  const [archiving, setArchiving] = useState(false)
  const [archiveError, setArchiveError] = useState(null)

  async function handleArchive() {
    if (!selected) return
    if (!window.confirm(`Skill "${selected}" archivieren? Das Original wird gelöscht.`)) return
    setArchiving(true)
    setArchiveError(null)
    try {
      const res = await fetch(`/api/skill/${encodeURIComponent(selected)}`, {
        method: 'DELETE',
      })
      if (!res.ok) throw new Error('Fehler beim Archivieren')
      await loadSkills()
      backToList()
    } catch (e) {
      setArchiveError(e.message)
    } finally {
      setArchiving(false)
    }
  }

  function handleDownload() {
    if (!selected) return
    window.location.href = `/api/skill/${encodeURIComponent(selected)}/download`
  }

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
      await loadSkills()
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  const skillEntries = Object.entries(skills)

  return (
    <div className="layout">
      <header className="topbar">
        <h1>Skill Manager</h1>
        <button className="menu-btn" onClick={() => setDrawerOpen(true)}>
          Upload
        </button>
      </header>

      {listError && <p className="list-error">{listError}</p>}

      <main className="content">
        {selected && detail ? (
          <section className="detail">
            <button className="back-btn" onClick={backToList}>
              &larr; Zurück
            </button>

            <div className="header-info">
              <h2>{detail.header?.name}</h2>
              {detail.header?.description && (
                <p><strong>Beschreibung:</strong> {detail.header.description}</p>
              )}
              {detail.header?.['argument-hint'] && (
                <p><strong>Argument-Hint:</strong> {detail.header['argument-hint']}</p>
              )}
              {archiveError && <p className="error">{archiveError}</p>}
            </div>

            <div className="detail-body">
              <aside className="file-list">
                <div className="file-list-actions">
                  <button className="download-btn" onClick={handleDownload}>
                    Download (ZIP)
                  </button>
                  <button
                    className="archive-btn"
                    onClick={handleArchive}
                    disabled={archiving}
                  >
                    {archiving ? 'Archiviere...' : 'Archivieren'}
                  </button>
                </div>
                <h3>Dateien</h3>
                <ul>
                  {Object.keys(detail.files || {}).map((f) => (
                    <li
                      key={f}
                      className={f === activeFile ? 'active' : ''}
                      onClick={() => setActiveFile(f)}
                    >
                      {f}
                    </li>
                  ))}
                </ul>
              </aside>

              <article className="file-content">
                {activeFile && detail.files?.[activeFile] != null ? (
                  <pre>{detail.files[activeFile]}</pre>
                ) : (
                  <p className="muted">Wähle eine Datei aus.</p>
                )}
              </article>
            </div>
          </section>
        ) : (
          <section className="cards">
            {skillEntries.length === 0 && !listError && (
              <p className="muted">Keine Skills vorhanden.</p>
            )}
            {skillEntries.map(([key, info]) => (
              <article
                key={key}
                className="card"
                onClick={() => openSkill(key)}
              >
                <h3>{info.name || key}</h3>
                <p className="card-desc">{info.description}</p>
              </article>
            ))}
          </section>
        )}
      </main>

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
