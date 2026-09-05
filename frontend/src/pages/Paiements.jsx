import { useEffect, useState } from 'react'
import client from '../api/client'
import Pagination from '../components/Pagination.jsx'

export default function Paiements() {
  const [page, setPage] = useState(0)
  const [data, setData] = useState(null)
  const [baux, setBaux] = useState([])
  const [bailId, setBailId] = useState('')
  const [lienGenere, setLienGenere] = useState(null)
  const [erreur, setErreur] = useState(null)

  function charger(p = page) {
    client.get('/api/paiements', { params: { page: p, size: 20 } })
      .then(res => setData(res.data))
      .catch(() => setErreur('Impossible de charger les paiements'))
  }

  useEffect(() => { charger(page) }, [page])
  useEffect(() => {
    client.get('/api/baux', { params: { page: 0, size: 200 } }).then(res => setBaux(res.data.content))
  }, [])

  async function genererLien(e) {
    e.preventDefault()
    setErreur(null)
    setLienGenere(null)
    try {
      const { data } = await client.post('/api/paiements/initier', { bailId })
      setLienGenere(data)
      charger(0)
      setPage(0)
    } catch (err) {
      setErreur(err.response?.data?.message || 'Échec de la génération du lien de paiement')
    }
  }

  function badgeClasse(statut) {
    if (statut === 'REUSSI') return 'paye'
    if (statut === 'ECHOUE') return 'retard'
    return 'attente'
  }

  return (
    <div>
      <h1>Paiements</h1>
      {erreur && <div className="error-msg">{erreur}</div>}

      <form className="card" onSubmit={genererLien} style={{ display: 'flex', gap: 12, alignItems: 'flex-end' }}>
        <div style={{ flex: 1 }}>
          <select value={bailId} onChange={e => setBailId(e.target.value)} required>
            <option value="">-- Choisir un bail --</option>
            {baux.map(b => <option key={b.id} value={b.id}>{b.locataire.nom} — {b.montantLoyer.toLocaleString('fr-FR')} XAF</option>)}
          </select>
        </div>
        <button type="submit" style={{ marginBottom: 12 }}>Générer le lien de paiement (mois courant)</button>
      </form>

      {lienGenere && (
        <div className="card">
          {lienGenere.urlPaiement ? (
            <>
              <p>Lien à envoyer au locataire par SMS/WhatsApp :</p>
              <input readOnly value={lienGenere.urlPaiement} onFocus={e => e.target.select()} />
            </>
          ) : (
            <p className="error-msg">Échec de génération : {lienGenere.statut}</p>
          )}
        </div>
      )}

      <div className="card">
        <table>
          <thead><tr><th>Locataire</th><th>Période</th><th>Montant</th><th>Statut</th><th>Reçu</th></tr></thead>
          <tbody>
            {data?.content?.map(p => (
              <tr key={p.id}>
                <td>{p.bail?.locataire?.nom}</td>
                <td>{p.periodeCouverte}</td>
                <td>{p.montant.toLocaleString('fr-FR')} XAF</td>
                <td><span className={`badge ${badgeClasse(p.statut)}`}>{p.statut}</span></td>
                <td>{p.tokenRecu ? <a href={`${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/api/public/recus/${p.tokenRecu}`} target="_blank" rel="noreferrer">Télécharger</a> : '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
        {data && <Pagination page={page} totalPages={data.totalPages} onChange={setPage} />}
      </div>
    </div>
  )
}
