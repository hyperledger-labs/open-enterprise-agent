import type { Options } from 'k6/options'
import { Issuer, Holder } from '../../actors'
import { Connection, CredentialSchemaResponse } from '@hyperledger/identus-cloud-agent-client'
import { defaultOptions } from '../../scenarios/default'
import { merge } from 'ts-deepmerge'
import { describe } from '../../k6chaijs.js'

export const localOptions: Options = {
  thresholds: {
    'group_duration{group:::Issuer creates credential offer}': ['avg < 5000']
  }
}
export const options: Options = merge(localOptions, defaultOptions)
export const issuer = new Issuer()
export const holder = new Holder()

export function setup () {
  describe('Issuer publishes DID', function () {
    issuer.createUnpublishedDid()
    issuer.publishDid()
  })

  describe('Holder creates unpublished DID', function () {
    holder.createUnpublishedDid()
  })

  describe('Issuer connects with Holder', function () {
    issuer.createHolderConnection()
    holder.acceptIssuerConnection(issuer.connectionWithHolder!.invitation)
    issuer.finalizeConnectionWithHolder()
    holder.finalizeConnectionWithIssuer()
  })

  describe('Issuer creates credential schema', function () {
    issuer.createCredentialSchema()
  })

  return {
    issuerDid: issuer.did,
    holderDid: holder.did,
    issuerSchema: issuer.schema,
    connectionWithHolder: issuer.connectionWithHolder!,
    connectionWithIssuer: holder.connectionWithIssuer!
  }
}

export default (data: { issuerDid: string, holderDid: string, issuerSchema: CredentialSchemaResponse, connectionWithHolder: Connection, connectionWithIssuer: Connection }) => {
  // This is the only way to pass data from setup to default
  issuer.did = data.issuerDid
  issuer.schema = data.issuerSchema
  holder.did = data.holderDid
  issuer.connectionWithHolder = data.connectionWithHolder
  holder.connectionWithIssuer = data.connectionWithIssuer

  describe('Issuer creates credential offer', function () {
    issuer.createCredentialOffer()
  })
}
