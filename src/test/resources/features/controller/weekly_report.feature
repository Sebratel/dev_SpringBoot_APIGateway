Feature: Weekly Report Management
  As a developer
  I want to ensure weekly report endpoints are functional
  So that the API remains reliable

  @WeeklyReport
  Scenario: Successfully recover weekly report
    Given the weekly report service is ready
    When I request to recover weekly report
    Then the response status should be 200
