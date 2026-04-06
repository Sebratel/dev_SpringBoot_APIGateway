Feature: Duplicate Client Name Management
  As a developer
  I want to ensure duplicate client name endpoints are functional
  So that the API remains reliable

  @DuplicateClientName
  Scenario: Successfully recover duplicate client name report
    Given the duplicate client name report service is ready
    When I request to recover duplicate client name report
    Then the response status should be 200
