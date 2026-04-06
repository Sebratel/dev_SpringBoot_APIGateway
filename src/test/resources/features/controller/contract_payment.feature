Feature: Contract Payment Management
  As a developer
  I want to ensure contract payment endpoints are functional
  So that the API remains reliable

  @ContractPayment
  Scenario: Successfully recover contract payments
    Given the contract payment service is ready
    When I request to recover contract payments
    Then the response status should be 200
