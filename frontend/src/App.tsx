import { Link, Outlet, Route, Routes } from 'react-router-dom'
import { Layout } from './components/layout/Layout'
import { TasksProvider } from './components/workshop/TasksProvider'
import { Claim as FileClaimClaim } from './pages/file-claim/Claim'
import { Claim as ClaimHandlerClaim } from './pages/claim-handler/Claim'
import { Claims as ClaimHandlerClaims } from './pages/claim-handler/Claims'
import { MyClaims } from './pages/file-claim/MyClaims'
import { NewClaim } from './pages/file-claim/NewClaim'
import { ReportWithHelp } from './pages/file-claim/ReportWithHelp'

export default function App() {
  return (
    <TasksProvider>
      <Layout>
        <Routes>
          <Route path="/" element={<FileClaimShell />}>
            <Route index element={<NewClaim />} />
            <Route path="chat" element={<ReportWithHelp />} />
            <Route path="claims" element={<MyClaims />} />
          </Route>
          <Route path="/claims/:claimId" element={<FileClaimClaim />} />
          <Route path="/claimhandler" element={<ClaimHandlerClaims />} />
          <Route path="/claimhandler/claims/:claimId" element={<ClaimHandlerClaim />} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </Layout>
    </TasksProvider>
  )
}

function FileClaimShell() {
  return <Outlet />
}

function NotFound() {
  return (
    <>
      <header>
        <h1>Nothing here</h1>
        <p>That address does not match a screen. Start from the top.</p>
      </header>
      <Link className="back" to="/">
        ← Report a claim
      </Link>
    </>
  )
}
