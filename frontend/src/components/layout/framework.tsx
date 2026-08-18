import type { Framework } from '@snokam/ui/framework'
import { Link, useLocation, useNavigate } from 'react-router-dom'

export const framework: Partial<Framework> = {
  Link: ({ href, children, ...rest }) => (
    <Link to={href} {...rest}>
      {children}
    </Link>
  ),
  useNavigation: () => {
    const navigate = useNavigate()
    const location = useLocation()
    return {
      push: (href: string) => navigate(href),
      pathname: location.pathname,
    }
  },
}
