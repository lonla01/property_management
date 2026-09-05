import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

/**
 * Page publique consultée par le LOCATAIRE (pas de compte, pas de JWT).
 * L'accès est protégé uniquement par le token unique et non-devinable
 * contenu dans l'URL — voir PaiementController /api/public/paiements/{token}.
 */
export default function PagePaiementPublic() {
  const { token } = useParams()
  const [paiement, setPaiement] = useState(null)
  const [erreur, setErreur] = useState(null)

  useEffect(() => {
    axios.get(`${API_BASE_URL}/api/public/paiements/${token}`)
      .then(res => setPaiement(res.data))
      .catch(() => setErreur("Ce lien de paiement est invalide ou a expiré."))
  }, [token])

  if (erreur) return <div className="auth-page"><div className="auth-box"><p className="error-msg">{erreur}</p></div></div>
  if (!paiement) return <div className="auth-page"><div className="auth-box"><p>Chargement...</p></div></div>

  const statutLabel = {
    EN_ATTENTE: 'En attente de paiement',
    REUSSI: 'Paiement confirmé ✅',
    ECHOUE: 'Paiement échoué',
  }[paiement.statut] || paiement.statut

  return (
    <div className="auth-page">
      <div className="auth-box" style={{ width: 420 }}>
        <h2>Paiement de loyer</h2>
        <p>Période : <strong>{paiement.periodeCouverte}</strong></p>
        <p>Montant : <strong>{paiement.montant.toLocaleString('fr-FR')} XAF</strong></p>
        <p>Statut : <strong>{statutLabel}</strong></p>

        {paiement.statut === 'EN_ATTENTE' && (
          <>
            <p style={{ fontSize: 13, color: 'var(--text-muted)' }}>
              Choisissez Orange Money ou MTN Mobile Money sur la page CinetPay pour finaliser le paiement.
            </p>
            {/* En sandbox, aucune vraie page CinetPay n'existe encore — ce bouton simule la confirmation
                du paiement pour permettre de tester tout le flux (webhook, reçu PDF) sans compte réel. */}
            <button onClick={() => window.location.reload()}>J'ai payé, vérifier le statut</button>
          </>
        )}

        {paiement.statut === 'REUSSI' && paiement.tokenRecu && (
          <a className="btn" href={`${API_BASE_URL}/api/public/recus/${paiement.tokenRecu}`} target="_blank" rel="noreferrer">
            Télécharger le reçu PDF
          </a>
        )}
      </div>
    </div>
  )
}
