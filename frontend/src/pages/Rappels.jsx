import { useEffect, useState } from 'react'
import client from '../api/client'
import Pagination from '../components/Pagination.jsx'

export default function Rappels() {
  const [page, setPage] = useState(0)
  const [data, setData] = useState(null)
  const [erreur, setErreur] = useState(null)

  function charger(p = page) {
    // Pagination obligatoire sur l'historique des rappels/relances.
    client.get('/api/rappels', { params: { page: p, size: 30 } })
      .then(res => setData(res.data))
      .catch(() => setErreur("Impossible de charger l'historique"))
  }

  useEffect(() => { charger(page) }, [page])

  function badgeClasse(statut) {
    if (statut === 'ENVOYE') return 'paye'
    if (statut === 'ECHOUE') return 'retard'
    return 'attente'
  }

  return (
    <div>
      <h1>Rappels &amp; relances</h1>
      {erreur && <div className="error-msg">{erreur}</div>}

      <div className="card">
        <p style={{ color: 'var(--text-muted)', fontSize: 13 }}>
          Cet historique est alimenté automatiquement par le planificateur externe (worker Railway + cron),
          qui appelle chaque jour l'endpoint <code>/api/rappels/executer-du-jour</code>. Les envois sont
          traités par petits lots avec une pause entre chaque lot, et chaque tentative (réussie ou échouée)
          est journalisée ici, canal par canal (SMS et WhatsApp), pour pouvoir vérifier que chaque locataire
          concerné a bien reçu son message et relancer manuellement les échecs si besoin.
        </p>
      </div>

      <div className="card">
        <table>
          <thead><tr><th>Locataire</th><th>Type</th><th>Période</th><th>Canal</th><th>Statut</th><th>Tentatives</th></tr></thead>
          <tbody>
            {data?.content?.map(r => (
              <tr key={r.id}>
                <td>{r.bail?.locataire?.nom}</td>
                <td>{r.type === 'RAPPEL_AVANT_ECHEANCE' ? 'Rappel' : 'Relance retard'}</td>
                <td>{r.periodeConcernee}</td>
                <td>{r.canal}</td>
                <td><span className={`badge ${badgeClasse(r.statutEnvoi)}`}>{r.statutEnvoi}</span></td>
                <td>{r.tentatives}</td>
              </tr>
            ))}
          </tbody>
        </table>
        {data && <Pagination page={page} totalPages={data.totalPages} onChange={setPage} />}
      </div>
    </div>
  )
}
