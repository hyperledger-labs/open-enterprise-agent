import type { Options } from 'k6/options'
import { Issuer } from '../../actors'
import { defaultOptions } from '../../scenarios/default'
import { merge } from 'ts-deepmerge'
import { describe } from '../../k6chaijs.js'

export const localOptions: Options = {
  thresholds: {
    'group_duration{group:::Issuer creates credential schema}': ['avg < 5000']
  }
}
export const options: Options = merge(localOptions, defaultOptions)
export const issuer = new Issuer()

export function setup () {
  describe('Issuer publishes DID', function () {
    issuer.createUnpublishedDid()
    issuer.publishDid()
  })

  return {
    issuerDid: issuer.did
  }
}

export default (data: { issuerDid: string }) => {
  // This is the only way to pass data from setup to default
  issuer.did = data.issuerDid

  describe('Issuer creates credential schema', function () {
    issuer.createCredentialSchema()
  })
}
