import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function Register() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ nom: '', email: '', telephone: '', motDePasse: '' })
  const [erreur, setErreur] = useState(null)
  const [chargement, setChargement] = useState(false)

  function update(field) {
    return (e) => setForm({ ...form, [field]: e.target.value })
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setErreur(null)
    setChargement(true)
    try {
      await register(form.nom, form.email, form.telephone, form.motDePasse)
      navigate('/')
    } catch (err) {
      setErreur(err.response?.data?.message || 'Échec de la création du compte')
    } finally {
      setChargement(false)
    }
  }

  return (
    <div className="auth-page">
      <form className="auth-box" onSubmit={handleSubmit}>
        <h2>Créer un compte propriétaire</h2>
        {erreur && <div className="error-msg">{erreur}</div>}
        <input placeholder="Nom complet" value={form.nom} onChange={update('nom')} required />
        <input type="email" placeholder="Email" value={form.email} onChange={update('email')} required />
        <input placeholder="Téléphone (+237...)" value={form.telephone} onChange={update('telephone')} required />
        <input type="password" placeholder="Mot de passe (8 caractères min.)" value={form.motDePasse} onChange={update('motDePasse')} required />
        <button type="submit" disabled={chargement}>{chargement ? '...' : 'Créer le compte'}</button>
        <p style={{ marginTop: 16, fontSize: 13 }}>
          Déjà un compte ? <Link to="/login" style={{ color: 'var(--accent)' }}>Se connecter</Link>
        </p>
      </form>
    </div>
  )
}
