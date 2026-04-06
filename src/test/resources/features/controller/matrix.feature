Feature: Matrix Management
  As a developer
  I want to ensure matrix endpoints are functional
  So that the API remains reliable

  @Matrix
  Scenario: Successfully recover matrix data
    Given the matrix service is ready
    When I request to recover matrix data
    Then the response status should be 200
