@RFC0453 @AIP20 @credentials
Feature: Issue Credentials Protocol with unpublished DID

  Background:
    Given Issuer and Holder have an existing connection
    And Issuer has an unpublished DID
    And Holder has an unpublished DID

  Scenario: Issuing credential with unpublished PRISM DID
    And Issuer offers a credential to Holder with "long" form DID
    And Holder receives the credential offer
    And Holder accepts credential offer for JWT
    And Issuer issues the credential
    Then Holder receives the issued credential
