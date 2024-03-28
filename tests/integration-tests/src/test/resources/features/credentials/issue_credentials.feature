@RFC0453 @AIP20 @credentials
Feature: Issue Credentials Protocol

  Background:
    Given Issuer and Holder have an existing connection

  Scenario: Issuing credential with published PRISM DID
    When Issuer creates unpublished DID
    And He publishes DID to ledger
    And Holder creates unpublished DID
    And Issuer offers a credential to Holder with "short" form DID
    And Holder receives the credential offer
    And Holder accepts credential offer for JWT
    And Issuer issues the credential
    Then Holder receives the issued credential

  Scenario: Issuing credential with unpublished PRISM DID
    When Issuer creates unpublished DID
    And Holder creates unpublished DID
    And Issuer offers a credential to Holder with "long" form DID
    And Holder receives the credential offer
    And Holder accepts credential offer for JWT
    And Issuer issues the credential
    Then Holder receives the issued credential

  Scenario: Issuing anoncred with published PRISM DID
    When Issuer creates unpublished DID
    And He publishes DID to ledger
    And Holder creates unpublished DID
    And Issuer creates anoncred schema
    And Issuer creates anoncred credential definition
    And Issuer offers anoncred to Holder
    And Holder receives the credential offer
    And Holder accepts credential offer for anoncred
    And Issuer issues the credential
    Then Holder receives the issued credential
