import { Routes, Route, Navigate, NavLink } from 'react-router-dom'
import { useAuth } from './context/AuthContext.jsx'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import Dashboard from './pages/Dashboard.jsx'
import Biens from './pages/Biens.jsx'
import Locataires from './pages/Locataires.jsx'
import Baux from './pages/Baux.jsx'
import Paiements from './pages/Paiements.jsx'
import Rappels from './pages/Rappels.jsx'
import PagePaiementPublic from './pages/PagePaiementPublic.jsx'

function ProtectedLayout({ children }) {
  const { user, logout } = useAuth()
  if (!user) return <Navigate to="/login" replace />

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <h2>🏠 Gestion Locative</h2>
        <nav>
          <NavLink to="/" end>Tableau de bord</NavLink>
          <NavLink to="/biens">Biens</NavLink>
          <NavLink to="/locataires">Locataires</NavLink>
          <NavLink to="/baux">Baux</NavLink>
          <NavLink to="/paiements">Paiements</NavLink>
          <NavLink to="/rappels">Rappels &amp; relances</NavLink>
        </nav>
        <div style={{ marginTop: 32, fontSize: 13, color: 'var(--text-muted)' }}>
          Connecté : {user.nom}
          <br />
          <button style={{ marginTop: 8 }} onClick={logout}>Déconnexion</button>
        </div>
      </aside>
      <main className="main-content">{children}</main>
    </div>
  )
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/paiement/:token" element={<PagePaiementPublic />} />

      <Route path="/" element={<ProtectedLayout><Dashboard /></ProtectedLayout>} />
      <Route path="/biens" element={<ProtectedLayout><Biens /></ProtectedLayout>} />
      <Route path="/locataires" element={<ProtectedLayout><Locataires /></ProtectedLayout>} />
      <Route path="/baux" element={<ProtectedLayout><Baux /></ProtectedLayout>} />
      <Route path="/paiements" element={<ProtectedLayout><Paiements /></ProtectedLayout>} />
      <Route path="/rappels" element={<ProtectedLayout><Rappels /></ProtectedLayout>} />
    </Routes>
  )
}
