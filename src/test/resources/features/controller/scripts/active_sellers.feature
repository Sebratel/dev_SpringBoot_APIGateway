Feature: Active Sellers Management
  As a developer
  I want to ensure active sellers endpoints are functional
  So that the API remains reliable

  @ActiveSellers
  Scenario: Successfully recover active sellers
    Given the active sellers service is ready
    When I request to recover active sellers
    Then the response status should be 200
    And the response should contain a list of active sellers
