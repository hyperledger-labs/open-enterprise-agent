@revocation @jwt
Feature: Credential revocation - JWT

  Background:
    Given Holder has an issued credential from Issuer

  Scenario: Revoke issued credential
    When Issuer revokes the credential issued to Holder
    Then Issuer should see the credential was revoked
    When Issuer sends a request for proof presentation to Holder
    And Holder receives the request
    And Holder makes the presentation of the proof to Issuer
    Then Issuer sees the proof returned verification failed

  Scenario: Holder tries to revoke credential from issuer
    When Holder tries to revoke credential from Issuer
    And Issuer sends a request for proof presentation to Holder
    And Holder receives the request
    And Holder makes the presentation of the proof to Issuer
    Then Issuer has the proof verified
