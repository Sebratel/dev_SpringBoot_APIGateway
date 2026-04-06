Feature: Affected User Management
  As a developer
  I want to ensure affected user endpoints are functional
  So that the API remains reliable

  @AffectedUser
  Scenario: Successfully get all impacted users
    Given the affected user service is ready with users
    When I request to get all impacted users
    Then the response status should be 200
    And the response success flag should be true

  @AffectedUser
  Scenario: Failure when no impacted users found
    Given the affected user service has no users
    When I request to get all impacted users
    Then the response status should be 404
    And the response success flag should be false
