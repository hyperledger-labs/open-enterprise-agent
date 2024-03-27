Feature: Present Proof Protocol

Scenario: Holder presents anoncreds credential proof to verifier
  Given Issuer and Holder have an existing connection
  And Verifier and Holder have an existing connection
  And Issuer has a published DID
  And Holder has an unpublished DID
  And Issuer creates anoncred schema
  And Issuer creates anoncred credential definition
  And Issuer offers anoncred to Holder
  And Holder receives the credential offer
  And Holder accepts credential offer for anoncred
  And Issuer issues the credential
  And Holder receives the issued credential
  When Verifier sends a anoncreds request for proof presentation to Holder using credential definition issued by Issuer
  And Holder receives the anoncreds request
  And Holder accepts the anoncreds presentation request from Verifier
  Then Verifier has the proof verified
