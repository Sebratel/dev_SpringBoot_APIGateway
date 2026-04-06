Feature: Contract Without Invoice Management
  As a developer
  I want to ensure contract without invoice endpoints are functional
  So that the API remains reliable

  @ContractWithoutInvoice
  Scenario: Successfully recover contracts without invoice
    Given the contract without invoice service is ready
    When I request to recover contracts without invoice
    Then the response status should be 200
