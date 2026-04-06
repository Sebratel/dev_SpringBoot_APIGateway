Feature: Contract Activation Invoice Management
  As a developer
  I want to ensure contract activation invoice endpoints are functional
  So that the API remains reliable

  @ContractActivationInvoice
  Scenario: Successfully recover contract activation invoices
    Given the contract activation invoice service is ready
    When I request to recover contract activation invoices
    Then the response status should be 200
