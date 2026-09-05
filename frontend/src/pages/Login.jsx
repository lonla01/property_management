import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [motDePasse, setMotDePasse] = useState('')
  const [erreur, setErreur] = useState(null)
  const [chargement, setChargement] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setErreur(null)
    setChargement(true)
    try {
      await login(email, motDePasse)
      navigate('/')
    } catch (err) {
      setErreur(err.response?.data?.message || 'Échec de connexion')
    } finally {
      setChargement(false)
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-box" onSubmit={handleSubmit}>
        <h2>Connexion propriétaire</h2>
        {erreur && <div className="error-msg">{erreur}</div>}
        <input type="email" placeholder="Email" value={email} onChange={e => setEmail(e.target.value)} required />
        <input type="password" placeholder="Mot de passe" value={motDePasse} onChange={e => setMotDePasse(e.target.value)} required />
        <button type="submit" disabled={chargement}>{chargement ? '...' : 'Se connecter'}</button>
        <p style={{ marginTop: 16, fontSize: 13 }}>
          Pas encore de compte ? <Link to="/register" style={{ color: 'var(--accent)' }}>Créer un compte</Link>
        </p>
      </form>
    </div>
  )
}
