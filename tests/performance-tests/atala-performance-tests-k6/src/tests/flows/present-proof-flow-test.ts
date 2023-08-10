import { group } from 'k6';
import { Options } from 'k6/options';
import { Issuer, Holder, Verifier } from '../../actors';
import { CredentialSchemaResponse } from '@input-output-hk/prism-typescript-client';

export let options: Options = {
  scenarios: {
    // smoke: {
    //   executor: 'constant-vus',
    //   vus: 20,
    //   duration: "10m",
    //   gracefulStop: "2m",
    // },
    acapy: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '2m', target: 10 },
        { duration: '2m', target: 10 },
        { duration: '3m', target: 0 },
        // { duration: '1m', target: 150 },
        // { duration: '1m', target: 150 },
        // { duration: '1m', target: 0 },
        // { duration: '1m', target: 200 },
        // { duration: '1m', target: 200 },
        // { duration: '1m', target: 0 },
        // { duration: '1m', target: 250 },
        // { duration: '1m', target: 250 },
        // { duration: '1m', target: 0 },
        // { duration: '1m', target: 300 },
        // { duration: '1m', target: 300 },
        // { duration: '1m', target: 0 },
      ],
      gracefulRampDown: '10m',
    }
  },
  thresholds: {
    http_req_failed: [{
      threshold: 'rate==0',
      // abortOnFail: true,
    }],
    'group_duration{group:::Holder connects with Issuer}': ['max >= 0'],
    'group_duration{group:::Issuer creates credential offer for Holder}': ['max >= 0'],
    'group_duration{group:::Holder connects with Verifier}': ['max >= 0'],
    'group_duration{group:::Verifier requests proof from Holder}': ['max >= 0'],
    http_req_duration: ['p(95)<=500'],
    checks: ['rate==1'],
  },
};

const issuer = new Issuer();
const holder = new Holder();
const verifier = new Verifier();

export function setup() {
  group('Issuer publishes DID', function () {
    issuer.createUnpublishedDid();
    issuer.publishDid();
  });

  group('Issuer creates credential schema', function () {
    issuer.createCredentialSchema();
  });

  group('Holder creates unpublished DID', function () {
    holder.createUnpublishedDid();
  });

  return { issuerDid: issuer.did, holderDid: holder.did, issuerSchema: issuer.schema };
}

export default (data: { issuerDid: string; holderDid: string; issuerSchema: CredentialSchemaResponse; }) => {

  issuer.did = data.issuerDid;
  issuer.schema = data.issuerSchema;
  holder.did = data.holderDid;

  group('Holder connects with Issuer', function () {
    issuer.createHolderConnection();
    holder.acceptIssuerConnection(issuer.connectionWithHolder!.invitation);
    issuer.finalizeConnectionWithHolder();
    holder.finalizeConnectionWithIssuer();
  });

  group('Issuer creates credential offer for Holder', function () {
    issuer.createCredentialOffer();
    // issuer.waitForCredentialOfferToBeSent();
    holder.waitAndAcceptCredentialOffer(issuer.credential!.thid);
    issuer.receiveCredentialRequest();
    issuer.issueCredential();
    issuer.waitForCredentialToBeSent();
    holder.receiveCredential();
  });

  group('Holder connects with Verifier', function () {
    verifier.createHolderConnection();
    holder.acceptVerifierConnection(verifier.connectionWithHolder!.invitation);
    verifier.finalizeConnectionWithHolder();
    holder.finalizeConnectionWithVerifier();
  });

  group('Verifier requests proof from Holder', function () {
    verifier.requestProof();
    holder.waitAndAcceptProofRequest(verifier.presentation!.thid);
    verifier.acknowledgeProof();
  });

};
