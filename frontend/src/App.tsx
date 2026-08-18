import { Link, Outlet, Route, Routes } from 'react-router-dom'
import { Layout } from './components/layout/Layout'
import { TasksProvider } from './components/workshop/TasksProvider'
import { Case as FileClaimCase } from './pages/file-claim/Case'
import { Case as ClaimHandlerCase } from './pages/claim-handler/Case'
import { Cases as ClaimHandlerCases } from './pages/claim-handler/Cases'
import { MyCases } from './pages/file-claim/MyCases'
import { NewCase } from './pages/file-claim/NewCase'
import { ReportWithChat } from './pages/file-claim/ReportWithChat'

export default function App() {
  return (
    <TasksProvider>
      <Layout>
        <Routes>
          <Route path="/" element={<FileClaimShell />}>
            <Route index element={<NewCase />} />
            <Route path="chat" element={<ReportWithChat />} />
            <Route path="cases" element={<MyCases />} />
          </Route>
          <Route path="/cases/:caseId" element={<FileClaimCase />} />
          <Route path="/casehandler" element={<ClaimHandlerCases />} />
          <Route path="/casehandler/cases/:caseId" element={<ClaimHandlerCase />} />
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
        ← Report a case
      </Link>
    </>
  )
}
