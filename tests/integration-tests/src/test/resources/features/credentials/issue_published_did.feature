@RFC0453 @AIP20 @credentials
Feature: Issue Credentials Protocol with published DID

  Background:
    Given Issuer and Holder have an existing connection
    And Issuer has a published DID
    And Issuer has published STUDENT_SCHEMA schema
    And Holder has an unpublished DID

  Scenario: Issuing credential with published PRISM DID
    When Issuer offers a credential to Holder with "short" form DID
    And Holder receives the credential offer
    And Holder accepts credential offer for JWT
    And Issuer issues the credential
    Then Holder receives the issued credential

  Scenario: Issuing anoncred with published PRISM DID
    When Issuer creates anoncred schema
    And Issuer creates anoncred credential definition
    And Issuer offers anoncred to Holder
    And Holder receives the credential offer
    And Holder accepts credential offer for anoncred
    And Issuer issues the credential
    Then Holder receives the issued credential

  Scenario: Issuing credential with an existing schema
    When Issuer offers a credential to Holder with "short" form using STUDENT_SCHEMA schema
    And Holder receives the credential offer
    And Holder accepts credential offer for JWT
    And Issuer issues the credential
    Then Holder receives the issued credential

  Scenario: Issuing credential with wrong claim structure for schema
    When Issuer offers a credential to Holder with "short" form DID with wrong claims structure using STUDENT_SCHEMA schema
    Then Issuer should see that credential issuance has failed
