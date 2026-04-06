Feature: First Monthly Payer Management
  As a developer
  I want to ensure first monthly payer endpoints are functional
  So that the API remains reliable

  @FirstMonthlyPayer
  Scenario: Successfully recover first monthly payers
    Given the first monthly payer service is ready
    When I request to recover first monthly payers
    Then the response status should be 200
