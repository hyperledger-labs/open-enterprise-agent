import type { Options } from 'k6/options'
import { issuer, holder } from '../common'
import { CredentialSchemaResponse } from '@hyperledger/identus-cloud-agent-client'
import { defaultOptions } from '../../scenarios/default'
import { merge } from 'ts-deepmerge'
import { describe } from '../../k6chaijs.js'

export const localOptions: Options = {
  thresholds: {
    'group_duration{group:::Issuer connects with Holder}': ['avg < 10000'],
    'group_duration{group:::Issuer creates credential offer for Holder}': [
      'avg < 10000'
    ],
    'group_duration{group:::Issuer issues credential to Holder}': [
      'avg < 10000'
    ],
    'group_duration{group:::Holder receives credential from Issuer}': [
      'avg < 10000'
    ]
  }
}
export const options: Options = merge(localOptions, defaultOptions)

// This is setup code. It runs once at the beginning of the test, regardless of the number of VUs.
export function setup () {
  describe('Issuer publishes DID', function () {
    issuer.createUnpublishedDid()
    issuer.publishDid()
  })

  describe('Issuer creates credential schema', function () {
    issuer.createCredentialSchema()
  })

  describe('Holder creates unpublished DID', function () {
    holder.createUnpublishedDid()
  })

  return {
    issuerDid: issuer.did,
    holderDid: holder.did,
    issuerSchema: issuer.schema
  }
}

export default (data: {
  issuerDid: string
  holderDid: string
  issuerSchema: CredentialSchemaResponse
}) => {
  // This is the only way to pass data from setup to default
  issuer.did = data.issuerDid
  issuer.schema = data.issuerSchema
  holder.did = data.holderDid

  describe('Issuer connects with Holder', function () {
    issuer.createHolderConnection()
    holder.acceptIssuerConnection(issuer.connectionWithHolder!.invitation)
    issuer.finalizeConnectionWithHolder()
    holder.finalizeConnectionWithIssuer()
  }) &&

  describe('Issuer creates credential offer for Holder', function () {
    issuer.createCredentialOffer()
    issuer.waitForCredentialOfferToBeSent()
  }) &&

  describe(
    'Holder achieves and accepts credential offer from Issuer',
    function () {
      holder.waitAndAcceptCredentialOffer(issuer.credential!.thid)
    }
  ) &&

  describe('Issuer issues credential to Holder', function () {
    issuer.receiveCredentialRequest()
    issuer.issueCredential()
    issuer.waitForCredentialToBeSent()
  }) &&

  describe('Holder receives credential from Issuer', function () {
    holder.receiveCredential()
  })
}
