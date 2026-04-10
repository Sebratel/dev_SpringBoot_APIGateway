Feature: Inactivate Account Management
  As a developer
  I want to ensure the inactivate account endpoint is functional
  So that the API remains reliable

  @InactivateAccount
  Scenario: Successfully send inactivation event
    Given the inactivate account producer is ready
    When I request to inactivate an account with name "Cucumber User" and cpf "12345678901"
    Then the response status should be 202
