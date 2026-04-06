Feature: Acquisition Management
  As a developer
  I want to ensure acquisition endpoints are functional
  So that the API remains reliable

  @Acquisition
  Scenario: Successfully recover acquisition orders
    Given the acquisition service is ready
    When I request to recover acquisition orders
    Then the response status should be 200
    And the response should contain a list of acquisitions
