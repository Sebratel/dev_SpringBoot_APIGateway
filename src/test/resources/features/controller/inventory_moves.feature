Feature: Inventory Moves Management
  As a developer
  I want to ensure inventory moves endpoints are functional
  So that the API remains reliable

  @InventoryMoves
  Scenario: Successfully recover inventory moves
    Given the inventory moves service is ready
    When I request to recover inventory moves
    Then the response status should be 200
