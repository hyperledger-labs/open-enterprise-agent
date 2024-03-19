@revocation @jwt
Feature: Credential revocation - JWT

  Background:
    Given Issuer and Holder have an existing connection
    And Issuer has issued a revocable credential to Holder

  Scenario: Revoke issued credential
    When Issuer revokes the credential from Holder
    Then Issuer should see the credential was revoked
    When Issuer sends a request for proof presentation to Holder
    And Holder receives the request
    And Holder makes the presentation of the proof to Issuer
    Then Holder sees the proof is rejected

