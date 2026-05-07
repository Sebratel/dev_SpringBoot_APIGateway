Feature: Authentication Sites Management
  As a developer
  I want to ensure authentication sites endpoints are functional
  So that the API remains reliable

  @AuthenticationSites
  Scenario: Successfully recover authentication sites
    Given the authentication sites service is ready
    When I request to recover authentication sites
    Then the response status should be 200
