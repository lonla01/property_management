import { useEffect, useState } from 'react'
import client from '../api/client'
import Pagination from '../components/Pagination.jsx'

export default function Locataires() {
  const [page, setPage] = useState(0)
  const [data, setData] = useState(null)
  const [form, setForm] = useState({ nom: '', telephone: '', email: '' })
  const [erreur, setErreur] = useState(null)

  function charger(p = page) {
    client.get('/api/locataires', { params: { page: p, size: 20 } })
      .then(res => setData(res.data))
      .catch(() => setErreur('Impossible de charger les locataires'))
  }

  useEffect(() => { charger(page) }, [page])

  async function ajouter(e) {
    e.preventDefault()
    try {
      await client.post('/api/locataires', form)
      setForm({ nom: '', telephone: '', email: '' })
      charger(0)
      setPage(0)
    } catch {
      setErreur("Échec de l'ajout du locataire (rattachez-le à une unité via la page Baux)")
    }
  }

  async function archiver(id) {
    await client.delete(`/api/locataires/${id}`)
    charger()
  }

  return (
    <div>
      <h1>Locataires</h1>
      {erreur && <div className="error-msg">{erreur}</div>}

      <form className="card" onSubmit={ajouter} style={{ display: 'flex', gap: 12, alignItems: 'flex-end' }}>
        <div style={{ flex: 1 }}>
          <input placeholder="Nom" value={form.nom} onChange={e => setForm({ ...form, nom: e.target.value })} required />
        </div>
        <div style={{ flex: 1 }}>
          <input placeholder="Téléphone (+237...)" value={form.telephone} onChange={e => setForm({ ...form, telephone: e.target.value })} required />
        </div>
        <div style={{ flex: 1 }}>
          <input placeholder="Email (optionnel)" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} />
        </div>
        <button type="submit" style={{ marginBottom: 12 }}>Ajouter</button>
      </form>

      <div className="card">
        <table>
          <thead><tr><th>Nom</th><th>Téléphone</th><th>Email</th><th></th></tr></thead>
          <tbody>
            {data?.content?.map(l => (
              <tr key={l.id}>
                <td>{l.nom}</td>
                <td>{l.telephone}</td>
                <td>{l.email || '—'}</td>
                <td><button onClick={() => archiver(l.id)}>Archiver</button></td>
              </tr>
            ))}
          </tbody>
        </table>
        {data && <Pagination page={page} totalPages={data.totalPages} onChange={setPage} />}
      </div>
    </div>
  )
}
