import type { Options } from 'k6/options'
import { Issuer } from '../../actors'
import { merge } from 'ts-deepmerge'
import { defaultOptions } from '../../scenarios/default'
import { describe } from '../../k6chaijs.js'

export const localOptions: Options = {
  thresholds: {
    'group_duration{group:::Issuer create published DID}': ['avg < 5000']
  }
}
export const options: Options = merge(localOptions, defaultOptions)

const issuer = new Issuer()

export default () => {
  describe('Issuer create published DID', function () {
    issuer.createUnpublishedDid()
    issuer.publishDid()
  })
}
