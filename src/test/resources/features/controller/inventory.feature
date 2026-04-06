Feature: Inventory Management
  As a developer
  I want to ensure inventory endpoints are functional
  So that the API remains reliable

  @Inventory
  Scenario: Successfully recover inventory data
    Given the inventory service is ready
    When I request to recover inventory data
    Then the response status should be 200
