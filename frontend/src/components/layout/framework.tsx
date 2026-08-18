import type { Framework } from '@snokam/ui/framework'
import { Link, useLocation, useNavigate } from 'react-router-dom'

/**
 * What the snokam components use for links and images. Without this they fall back to a plain
 * anchor, which works but reloads the page on every click and loses the router. Handing them the
 * router's Link keeps navigation client-side.
 */
export const framework: Partial<Framework> = {
  Link: ({ href, children, ...rest }) => (
    <Link to={href} {...rest}>
      {children}
    </Link>
  ),
  useNavigation: () => {
    const navigate = useNavigate()
    const location = useLocation()
    return { push: (href: string) => navigate(href), pathname: location.pathname }
  },
}
