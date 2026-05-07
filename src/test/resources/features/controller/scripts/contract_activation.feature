Feature: Contract Activation Management
  As a developer
  I want to ensure contract activation endpoints are functional
  So that the API remains reliable

  @ContractActivation
  Scenario: Successfully recover contract activations
    Given the contract activation service is ready
    When I request to recover contract activations
    Then the response status should be 200
