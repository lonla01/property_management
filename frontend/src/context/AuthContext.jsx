import { createContext, useContext, useState } from 'react'
import client from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const nom = localStorage.getItem('nom')
    const email = localStorage.getItem('email')
    return nom ? { nom, email } : null
  })

  async function login(email, motDePasse) {
    const { data } = await client.post('/api/auth/login', { email, motDePasse })
    localStorage.setItem('token', data.token)
    localStorage.setItem('nom', data.nom)
    localStorage.setItem('email', data.email)
    setUser({ nom: data.nom, email: data.email })
  }

  async function register(nom, email, telephone, motDePasse) {
    const { data } = await client.post('/api/auth/register', { nom, email, telephone, motDePasse })
    localStorage.setItem('token', data.token)
    localStorage.setItem('nom', data.nom)
    localStorage.setItem('email', data.email)
    setUser({ nom: data.nom, email: data.email })
  }

  function logout() {
    localStorage.removeItem('token')
    localStorage.removeItem('nom')
    localStorage.removeItem('email')
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
