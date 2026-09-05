import { useEffect, useState } from 'react'
import client from '../api/client'
import Pagination from '../components/Pagination.jsx'

export default function Baux() {
  const [page, setPage] = useState(0)
  const [data, setData] = useState(null)
  const [locataires, setLocataires] = useState([])
  const [biens, setBiens] = useState([])
  const [unites, setUnites] = useState([])
  const [form, setForm] = useState({
    locataireId: '', bienId: '', uniteId: '', montantLoyer: '', dateDebut: '', jourEcheance: 5
  })
  const [erreur, setErreur] = useState(null)

  function charger(p = page) {
    client.get('/api/baux', { params: { page: p, size: 20 } })
      .then(res => setData(res.data))
      .catch(() => setErreur('Impossible de charger les baux'))
  }

  useEffect(() => { charger(page) }, [page])

  useEffect(() => {
    client.get('/api/locataires', { params: { page: 0, size: 200 } }).then(res => setLocataires(res.data.content))
    client.get('/api/biens', { params: { page: 0, size: 200 } }).then(res => setBiens(res.data.content))
  }, [])

  function onBienChange(bienId) {
    setForm({ ...form, bienId, uniteId: '' })
    if (bienId) {
      client.get('/api/unites', { params: { bienId } }).then(res => setUnites(res.data))
    } else {
      setUnites([])
    }
  }

  async function ajouter(e) {
    e.preventDefault()
    try {
      await client.post('/api/baux', {
        locataireId: form.locataireId,
        uniteId: form.uniteId,
        montantLoyer: Number(form.montantLoyer),
        dateDebut: form.dateDebut,
        jourEcheance: Number(form.jourEcheance),
      })
      setForm({ locataireId: '', bienId: '', uniteId: '', montantLoyer: '', dateDebut: '', jourEcheance: 5 })
      charger(0)
      setPage(0)
    } catch {
      setErreur('Échec de la création du bail — vérifiez que le locataire et l\u2019unité sont bien choisis')
    }
  }

  async function resilier(id) {
    await client.post(`/api/baux/${id}/resilier`)
    charger()
  }

  return (
    <div>
      <h1>Baux</h1>
      {erreur && <div className="error-msg">{erreur}</div>}

      <form className="card" onSubmit={ajouter}>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
          <select value={form.locataireId} onChange={e => setForm({ ...form, locataireId: e.target.value })} required>
            <option value="">-- Locataire --</option>
            {locataires.map(l => <option key={l.id} value={l.id}>{l.nom}</option>)}
          </select>

          <select value={form.bienId} onChange={e => onBienChange(e.target.value)} required>
            <option value="">-- Bien --</option>
            {biens.map(b => <option key={b.id} value={b.id}>{b.adresse}</option>)}
          </select>

          <select value={form.uniteId} onChange={e => setForm({ ...form, uniteId: e.target.value })} required disabled={!form.bienId}>
            <option value="">-- Unité --</option>
            {unites.map(u => <option key={u.id} value={u.id}>{u.libelle || '(sans libellé)'}</option>)}
          </select>

          <input type="number" placeholder="Montant du loyer (XAF)" value={form.montantLoyer}
                 onChange={e => setForm({ ...form, montantLoyer: e.target.value })} required />

          <input type="date" value={form.dateDebut} onChange={e => setForm({ ...form, dateDebut: e.target.value })} required />

          <input type="number" min="1" max="28" placeholder="Jour d'échéance (1-28)" value={form.jourEcheance}
                 onChange={e => setForm({ ...form, jourEcheance: e.target.value })} required />
        </div>
        <button type="submit" style={{ marginTop: 12 }}>Créer le bail</button>
      </form>

      <div className="card">
        <table>
          <thead><tr><th>Locataire</th><th>Unité</th><th>Loyer</th><th>Échéance</th><th>Statut</th><th></th></tr></thead>
          <tbody>
            {data?.content?.map(b => (
              <tr key={b.id}>
                <td>{b.locataire.nom}</td>
                <td>{b.unite.libelle || b.unite.bien?.adresse}</td>
                <td>{b.montantLoyer.toLocaleString('fr-FR')} XAF</td>
                <td>Le {b.jourEcheance}</td>
                <td>{b.statut}</td>
                <td>{b.statut === 'ACTIF' && <button onClick={() => resilier(b.id)}>Résilier</button>}</td>
              </tr>
            ))}
          </tbody>
        </table>
        {data && <Pagination page={page} totalPages={data.totalPages} onChange={setPage} />}
      </div>
    </div>
  )
}
