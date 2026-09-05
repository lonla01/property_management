import { useEffect, useState } from 'react'
import client from '../api/client'
import Pagination from '../components/Pagination.jsx'

const TYPES = ['APPARTEMENT', 'MAISON', 'STUDIO', 'IMMEUBLE']

export default function Biens() {
  const [page, setPage] = useState(0)
  const [data, setData] = useState(null)
  const [form, setForm] = useState({ adresse: '', type: 'APPARTEMENT' })
  const [erreur, setErreur] = useState(null)

  // Gestion des unités locatives, nécessaires pour créer un bail (voir page Baux).
  const [bienSelectionne, setBienSelectionne] = useState('')
  const [unites, setUnites] = useState([])
  const [libelleUnite, setLibelleUnite] = useState('')

  function chargerUnites(bienId) {
    if (!bienId) { setUnites([]); return }
    client.get('/api/unites', { params: { bienId } }).then(res => setUnites(res.data))
  }

  async function ajouterUnite(e) {
    e.preventDefault()
    if (!bienSelectionne) return
    await client.post('/api/unites', { bienId: bienSelectionne, libelle: libelleUnite })
    setLibelleUnite('')
    chargerUnites(bienSelectionne)
  }

  function charger(p = page) {
    // Pagination obligatoire: on ne charge jamais toute la collection d'un coup.
    client.get('/api/biens', { params: { page: p, size: 20 } })
      .then(res => setData(res.data))
      .catch(() => setErreur('Impossible de charger les biens'))
  }

  useEffect(() => { charger(page) }, [page])

  async function ajouter(e) {
    e.preventDefault()
    try {
      await client.post('/api/biens', form)
      setForm({ adresse: '', type: 'APPARTEMENT' })
      charger(0)
      setPage(0)
    } catch {
      setErreur("Échec de l'ajout du bien")
    }
  }

  async function archiver(id) {
    await client.delete(`/api/biens/${id}`)
    charger()
  }

  return (
    <div>
      <h1>Biens</h1>
      {erreur && <div className="error-msg">{erreur}</div>}

      <form className="card" onSubmit={ajouter} style={{ display: 'flex', gap: 12, alignItems: 'flex-end' }}>
        <div style={{ flex: 1 }}>
          <input placeholder="Adresse" value={form.adresse} onChange={e => setForm({ ...form, adresse: e.target.value })} required />
        </div>
        <div style={{ width: 180 }}>
          <select value={form.type} onChange={e => setForm({ ...form, type: e.target.value })}>
            {TYPES.map(t => <option key={t} value={t}>{t}</option>)}
          </select>
        </div>
        <button type="submit" style={{ marginBottom: 12 }}>Ajouter</button>
      </form>

      <div className="card">
        <table>
          <thead><tr><th>Adresse</th><th>Type</th><th></th></tr></thead>
          <tbody>
            {data?.content?.map(b => (
              <tr key={b.id}>
                <td>{b.adresse}</td>
                <td>{b.type}</td>
                <td><button onClick={() => archiver(b.id)}>Archiver</button></td>
              </tr>
            ))}
          </tbody>
        </table>
        {data && <Pagination page={page} totalPages={data.totalPages} onChange={setPage} />}
      </div>

      <div className="card">
        <h3>Unités locatives</h3>
        <p style={{ color: 'var(--text-muted)', fontSize: 13 }}>
          Nécessaires pour créer un bail — surtout utile si un bien est subdivisé (immeuble à plusieurs appartements).
        </p>
        <select value={bienSelectionne} onChange={e => { setBienSelectionne(e.target.value); chargerUnites(e.target.value) }}>
          <option value="">-- Choisir un bien --</option>
          {data?.content?.map(b => <option key={b.id} value={b.id}>{b.adresse}</option>)}
        </select>

        {bienSelectionne && (
          <>
            <form onSubmit={ajouterUnite} style={{ display: 'flex', gap: 12, alignItems: 'flex-end', marginTop: 8 }}>
              <div style={{ flex: 1 }}>
                <input placeholder="Libellé (ex. Appartement 2A)" value={libelleUnite} onChange={e => setLibelleUnite(e.target.value)} />
              </div>
              <button type="submit" style={{ marginBottom: 12 }}>Ajouter l'unité</button>
            </form>
            <ul>
              {unites.map(u => <li key={u.id}>{u.libelle || '(sans libellé)'}</li>)}
            </ul>
          </>
        )}
      </div>
    </div>
  )
}
