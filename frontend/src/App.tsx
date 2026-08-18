import { Link, Outlet, Route, Routes } from 'react-router-dom'
import { Layout } from './components/layout/Layout'
import { TasksProvider } from './components/workshop/TasksProvider'
import { Case as FileClaimCase } from './pages/file-claim/Case'
import { Case as ClaimHandlerCase } from './pages/claim-handler/Case'
import { Cases as ClaimHandlerCases } from './pages/claim-handler/Cases'
import { MyCases } from './pages/file-claim/MyCases'
import { NewCase } from './pages/file-claim/NewCase'

/**
 * Two audiences, one app, split by URL rather than a toggle: filing a claim under `/`, the claim
 * handler under `/casehandler`. There is no login and no cross-navigation between the two — each
 * side is simply its own address. The roles are a vocabulary distinction here, not a permission
 * model.
 *
 * Every screen has an address, which is the point of the router being here at all: a case can be
 * refreshed, bookmarked, opened in a second tab, or sent to a colleague. The workshop demo also
 * stops depending on clicking through from the top on every restart.
 */
export default function App() {
  return (
    <TasksProvider>
      <Layout>
        <Routes>
          <Route path="/" element={<FileClaimShell />}>
            <Route index element={<NewCase />} />
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

/** The filing side; the nav that used to live here is now in the branded header. */
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
