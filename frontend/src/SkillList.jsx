import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import './SkillList.css'

export default function SkillList() {
  const [skills, setSkills] = useState({})
  const [listError, setListError] = useState(null)
  const navigate = useNavigate()

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
    function onSkillsChanged() {
      loadSkills()
    }
    window.addEventListener('skills-changed', onSkillsChanged)
    return () => window.removeEventListener('skills-changed', onSkillsChanged)
  }, [])

  const skillEntries = Object.entries(skills)

  return (
    <>
      {listError && <p className="list-error">{listError}</p>}
      <main className="content">
        <section className="cards">
          {skillEntries.length === 0 && !listError && (
            <p className="muted">Keine Skills vorhanden.</p>
          )}
          {skillEntries.map(([key, info]) => (
            <article
              key={key}
              className="card"
              onClick={() => navigate(`/${encodeURIComponent(key)}`)}
            >
              <h3>{info.name || key}</h3>
              <p className="card-desc">{info.description}</p>
            </article>
          ))}
        </section>
      </main>
    </>
  )
}
