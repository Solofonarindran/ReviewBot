import { useEffect, useState } from 'react'

type Status = {
  application: string
  status: string
  javaVersion: string
  virtualThreads: boolean
}

export default function App() {
  const [status, setStatus] = useState<Status | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetch('/api/status')
      .then((r) => (r.ok ? r.json() : Promise.reject(new Error(`HTTP ${r.status}`))))
      .then(setStatus)
      .catch((e: Error) => setError(e.message))
  }, [])

  return (
    <main className="min-h-screen bg-slate-50 text-slate-900">
      <div className="mx-auto max-w-2xl px-6 py-16">
        <h1 className="text-3xl font-semibold tracking-tight">ReviewBot</h1>
        <p className="mt-2 text-slate-600">Revue de code automatique de pull requests.</p>

        <section className="mt-10 rounded-lg border border-slate-200 bg-white p-5">
          <h2 className="text-sm font-medium text-slate-500">État du serveur</h2>
          {error && <p className="mt-3 text-red-700">Le serveur ne répond pas ({error}). Lance le back-end sur le port 8080.</p>}
          {!error && !status && <p className="mt-3 text-slate-500">Connexion au serveur…</p>}
          {status && (
            <dl className="mt-3 grid grid-cols-[10rem_1fr] gap-y-2 text-sm">
              <dt className="text-slate-500">Application</dt>
              <dd>{status.application}</dd>
              <dt className="text-slate-500">Statut</dt>
              <dd className="font-medium text-emerald-700">{status.status}</dd>
              <dt className="text-slate-500">Version de Java</dt>
              <dd>{status.javaVersion}</dd>
              <dt className="text-slate-500">Threads virtuelles</dt>
              <dd>{status.virtualThreads ? 'actives' : 'inactives'}</dd>
            </dl>
          )}
        </section>
      </div>
    </main>
  )
}
