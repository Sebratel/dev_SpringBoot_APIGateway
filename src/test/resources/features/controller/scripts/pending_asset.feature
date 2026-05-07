Feature: Pending Asset Management
  As a developer
  I want to ensure pending asset endpoints are functional
  So that the API remains reliable

  @PendingAsset
  Scenario: Successfully recover pending assets
    Given the pending asset service is ready
    When I request to recover pending assets
    Then the response status should be 200
