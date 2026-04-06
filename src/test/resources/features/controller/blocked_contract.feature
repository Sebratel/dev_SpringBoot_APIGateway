Feature: Blocked Contract Management
  As a developer
  I want to ensure blocked contract endpoints are functional
  So that the API remains reliable

  @BlockedContract
  Scenario: Successfully recover blocked contracts
    Given the blocked contract service is ready
    When I request to recover blocked contracts
    Then the response status should be 200
