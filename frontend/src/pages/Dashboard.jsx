import { useEffect, useState } from 'react'
import client from '../api/client'

export default function Dashboard() {
  const [data, setData] = useState(null)
  const [erreur, setErreur] = useState(null)

  useEffect(() => {
    client.get('/api/dashboard')
      .then(res => setData(res.data))
      .catch(() => setErreur("Impossible de charger le tableau de bord"))
  }, [])

  if (erreur) return <div className="error-msg">{erreur}</div>
  if (!data) return <p>Chargement...</p>

  return (
    <div>
      <h1>Tableau de bord</h1>

      <div className="stat-grid">
        <div className="stat-card">
          <div className="value">{data.nombreLoyersEnRetard}</div>
          <div className="label">Loyers en retard</div>
        </div>
        <div className="stat-card">
          <div className="value">{data.montantTotalEnAttente.toLocaleString('fr-FR')} XAF</div>
          <div className="label">Montant total en attente ce mois-ci</div>
        </div>
      </div>

      <div className="card">
        <table>
          <thead>
            <tr>
              <th>Locataire</th>
              <th>Bien</th>
              <th>Statut</th>
              <th>Montant dû</th>
              <th>Dernier paiement</th>
            </tr>
          </thead>
          <tbody>
            {data.situations.map(s => (
              <tr key={s.bailId}>
                <td>{s.locataireNom}</td>
                <td>{s.bienAdresse}</td>
                <td>
                  <span className={`badge ${s.statutMoisCourant === 'PAYE' ? 'paye' : s.statutMoisCourant === 'EN_RETARD' ? 'retard' : 'attente'}`}>
                    {s.statutMoisCourant === 'PAYE' ? 'Payé' : s.statutMoisCourant === 'EN_RETARD' ? 'En retard' : 'En attente'}
                  </span>
                </td>
                <td>{s.montantDu ? `${s.montantDu.toLocaleString('fr-FR')} XAF` : '—'}</td>
                <td>{s.dateDernierPaiement || '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
