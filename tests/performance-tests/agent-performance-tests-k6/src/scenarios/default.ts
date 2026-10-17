/* global __ENV */

import type { Options } from 'k6/options'

export const defaultOptions: Options = {
  setupTimeout: '120s',
  scenarios: {
    smoke: {
      // a simple test to ensure performance tests work and requests don't fail
      executor: 'shared-iterations',
      vus: 1,
      iterations: 1,
      tags: { scenario_label: __ENV.SCENARIO_LABEL || 'defaultScenarioLabel' } // add label for filtering in observability platform
    }
  },
  thresholds: {
    http_req_failed: [
      // fail if any requests fail during smoke test
      {
        threshold: 'rate==0',
        abortOnFail: true
      }
    ],
    http_req_duration: [
      // TODO: this threshold is too high, it should be adjusted to the actual performance requirements to lower value
      { threshold: 'p(95)<20000', abortOnFail: false } // 95% of requests should complete within 5 seconds, but don't fail tests
    ],
    checks: [{ threshold: 'rate==1', abortOnFail: true }] // fail if any checks fail (the checks are defined in test code which is executed)
  }
}
