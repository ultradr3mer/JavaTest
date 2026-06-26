import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import ReactMarkdown from 'react-markdown'
import './SkillDetail.css'

export default function SkillDetail() {
  const { skillName } = useParams()
  const navigate = useNavigate()
  const [detail, setDetail] = useState(null)
  const [activeFile, setActiveFile] = useState(null)
  const [loadError, setLoadError] = useState(null)
  const [archiving, setArchiving] = useState(false)
  const [archiveError, setArchiveError] = useState(null)

  async function loadDetail(name) {
    setDetail(null)
    setActiveFile(null)
    setLoadError(null)
    try {
      const res = await fetch(`/api/skill/${encodeURIComponent(name)}`)
      if (!res.ok) throw new Error('Fehler beim Laden des Skills')
      const data = await res.json()
      setDetail(data)
      const firstFile = Object.keys(data.files || {})[0]
      if (firstFile) setActiveFile(firstFile)
    } catch (e) {
      setLoadError(e.message)
    }
  }

  useEffect(() => {
    if (skillName) loadDetail(skillName)
  }, [skillName])

  async function handleArchive() {
    if (!skillName) return
    if (!window.confirm(`Skill "${skillName}" archivieren? Das Original wird gelöscht.`)) return
    setArchiving(true)
    setArchiveError(null)
    try {
      const res = await fetch(`/api/skill/${encodeURIComponent(skillName)}`, {
        method: 'DELETE',
      })
      if (!res.ok) throw new Error('Fehler beim Archivieren')
      window.dispatchEvent(new CustomEvent('skills-changed'))
      navigate('/')
    } catch (e) {
      setArchiveError(e.message)
    } finally {
      setArchiving(false)
    }
  }

  function handleDownload() {
    if (!skillName) return
    window.location.href = `/api/skill/${encodeURIComponent(skillName)}/download`
  }

  function isMarkdown(name) {
    return /\.(md|markdown)$/i.test(name)
  }

  function buildTree(files) {
    const root = { name: '', children: {}, files: [] }
    for (const path of Object.keys(files)) {
      const parts = path.replace(/\\/g, '/').split('/')
      let node = root
      for (let i = 0; i < parts.length - 1; i++) {
        const part = parts[i]
        if (!node.children[part]) {
          node.children[part] = { name: part, children: {}, files: [] }
        }
        node = node.children[part]
      }
      node.files.push({ name: parts[parts.length - 1], path })
    }
    return root
  }

  function TreeNode({ node, depth, activeFile, onSelect }) {
    const [open, setOpen] = useState(true)
    const dirs = Object.values(node.children).sort((a, b) => a.name.localeCompare(b.name))
    const files = [...node.files].sort((a, b) => a.name.localeCompare(b.name))
    return (
      <>
        {dirs.map((dir) => (
          <li key={'dir-' + dir.name + depth} className="tree-dir">
            <span
              className="tree-dir-label"
              style={{ paddingLeft: `${depth * 14 + 4}px` }}
              onClick={() => setOpen((o) => !o)}
            >
              <span className="tree-toggle">{open ? '▾' : '▸'}</span>
              <span className="tree-icon">📁</span>
              {dir.name}
            </span>
            {open && (
              <ul className="tree-children">
                <TreeNode node={dir} depth={depth + 1} activeFile={activeFile} onSelect={onSelect} />
              </ul>
            )}
          </li>
        ))}
        {files.map((file) => (
          <li
            key={'file-' + file.path}
            className={'tree-file' + (file.path === activeFile ? ' active' : '')}
            style={{ paddingLeft: `${depth * 14 + 22}px` }}
            onClick={() => onSelect(file.path)}
          >
            <span className="tree-icon">📄</span>
            {file.name}
          </li>
        ))}
      </>
    )
  }

  if (loadError) {
    return <main className="content"><p className="error">{loadError}</p></main>
  }

  if (!detail) {
    return <main className="content"><p className="muted">Lädt...</p></main>
  }

  return (
    <main className="content">
      <section className="detail-container">
        <button className="back-btn" onClick={() => navigate('/')}>
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
            <ul className="tree-root">
              <TreeNode
                node={buildTree(detail.files || {})}
                depth={0}
                activeFile={activeFile}
                onSelect={setActiveFile}
              />
            </ul>
          </aside>

          <article className="file-content">
            {activeFile && detail.files?.[activeFile] != null ? (
              isMarkdown(activeFile) ? (
                <div className="markdown-body">
                  <ReactMarkdown>{detail.files[activeFile]}</ReactMarkdown>
                </div>
              ) : (
                <pre>{detail.files[activeFile]}</pre>
              )
            ) : (
              <p className="muted">Wähle eine Datei aus.</p>
            )}
          </article>
        </div>
      </section>
    </main>
  )
}
