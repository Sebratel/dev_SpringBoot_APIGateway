Feature: First Authentication Management
  As a developer
  I want to ensure first authentication endpoints are functional
  So that the API remains reliable

  @FirstAuthentication
  Scenario: Successfully recover first authentications
    Given the first authentication service is ready
    When I request to recover first authentications
    Then the response status should be 200
