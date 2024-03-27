@credential_schema
Feature: Credential schemas

  Background:
    When Issuer creates unpublished DID

  Scenario: Successful schema creation
    When Issuer creates a new credential STUDENT_SCHEMA schema
    Then He sees new credential schema is available

  Scenario Outline: Multiple schema creation
    When Issuer creates <schemas> new schemas
    Then He can access all of them one by one
    Examples:
      | schemas |
      | 4       |

  Scenario Outline: Schema creation should fail for cases
    When Issuer creates a schema containing '<schema_issue>' issue
    Then Issuer should see the schema creation failed
    Examples:
      | schema_issue                            |
      | TYPE_AND_PROPERTIES_WITHOUT_SCHEMA_TYPE |
      | CUSTOM_WORDS_NOT_DEFINED                |
      | MISSING_REQUIRED_FOR_MANDATORY_PROPERTY |
