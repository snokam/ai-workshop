import type { FraudScreening } from '../api'
import { INDICATOR_LABEL } from '../lib/labels'

export function Screening({ screening }: { screening: FraudScreening }) {
  return (
    <section className="screening">
      <h3>Worth a look</h3>
      {screening.indicators.map((indicator, index) => (
        <div key={index} className={`indicator ${indicator.weight.toLowerCase()}`}>
          <p className="what">
            <span className="kind">{INDICATOR_LABEL[indicator.kind]}</span>
            {indicator.detail}
          </p>
          {indicator.evidence.length > 0 && (
            <ul>
              {indicator.evidence.map((line) => (
                <li key={line}>{line}</li>
              ))}
            </ul>
          )}
        </div>
      ))}
    </section>
  )
}
