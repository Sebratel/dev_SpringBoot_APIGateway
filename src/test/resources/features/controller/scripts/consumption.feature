Feature: Consumption Management
  As a developer
  I want to ensure consumption endpoints are functional
  So that the API remains reliable

  @Consumption
  Scenario: Successfully recover consumption data
    Given the consumption service is ready
    When I request to recover consumption data for contract 123
    Then the response status should be 200
