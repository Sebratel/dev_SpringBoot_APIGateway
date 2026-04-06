Feature: Last Connection Management
  As a developer
  I want to ensure last connection endpoints are functional
  So that the API remains reliable

  @LastConnection
  Scenario: Successfully recover last connections
    Given the last connection service is ready
    When I request to recover last connections
    Then the response status should be 200
