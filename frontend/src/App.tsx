import { Link, Outlet, Route, Routes } from 'react-router-dom'
import { Layout } from './components/layout/Layout'
import { TasksProvider } from './components/workshop/TasksProvider'
import { Claim as FileClaimCase } from './pages/file-claim/Claim'
import { Claim as ClaimHandlerCase } from './pages/claim-handler/Claim'
import { Claims as ClaimHandlerCases } from './pages/claim-handler/Claims'
import { MyClaims } from './pages/file-claim/MyClaims'
import { NewClaim } from './pages/file-claim/NewClaim'
import { ReportWithChat } from './pages/file-claim/ReportWithChat'

export default function App() {
  return (
    <TasksProvider>
      <Layout>
        <Routes>
          <Route path="/" element={<FileClaimShell />}>
            <Route index element={<NewClaim />} />
            <Route path="chat" element={<ReportWithChat />} />
            <Route path="claims" element={<MyClaims />} />
          </Route>
          <Route path="/claims/:claimId" element={<FileClaimCase />} />
          <Route path="/claimhandler" element={<ClaimHandlerCases />} />
          <Route path="/claimhandler/claims/:claimId" element={<ClaimHandlerCase />} />
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
