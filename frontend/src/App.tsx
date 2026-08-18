import { Link, NavLink, Outlet, Route, Routes } from 'react-router-dom'
import { ClaimantCase } from './routes/ClaimantCase'
import { HandlerCase } from './routes/HandlerCase'
import { HandlerCases } from './routes/HandlerCases'
import { MyCases } from './routes/MyCases'
import { NewCase } from './routes/NewCase'

/**
 * Two audiences, one app, split by URL rather than a toggle: the claimant under `/`, the case
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
    <main>
      <Routes>
        <Route path="/" element={<ClaimantShell />}>
          <Route index element={<NewCase />} />
          <Route path="cases" element={<MyCases />} />
        </Route>
        <Route path="/cases/:caseId" element={<ClaimantCase />} />
        <Route path="/casehandler" element={<HandlerCases />} />
        <Route path="/casehandler/cases/:caseId" element={<HandlerCase />} />
        <Route path="*" element={<NotFound />} />
      </Routes>
    </main>
  )
}

/** The claimant's two entry points share a nav; a case opened from either does not. */
function ClaimantShell() {
  return (
    <>
      <nav className="claimant-nav">
        <NavLink to="/" end className={({ isActive }) => (isActive ? 'on' : '')}>
          Report a new case
        </NavLink>
        <NavLink to="/cases" className={({ isActive }) => (isActive ? 'on' : '')}>
          My cases
        </NavLink>
      </nav>
      <Outlet />
    </>
  )
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
