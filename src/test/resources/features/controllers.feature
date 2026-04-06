Feature: Controller Endpoints
  As a developer
  I want to ensure all controller endpoints are functional
  So that the API remains reliable

  @Acquisition
  Scenario: Successfully recover acquisition orders
    Given the acquisition service is ready
    When I request to recover acquisition orders
    Then the response status should be 200
    And the response should contain a list of acquisitions

  @ActiveSellers
  Scenario: Successfully recover active sellers
    Given the active sellers service is ready
    When I request to recover active sellers
    Then the response status should be 200
    And the response should contain a list of active sellers

  @AffectedUser
  Scenario: Successfully get all impacted users
    Given the affected user service is ready with users
    When I request to get all impacted users
    Then the response status should be 200
    And the response success flag should be true

  @AffectedUser
  Scenario: Failure when no impacted users found
    Given the affected user service has no users
    When I request to get all impacted users
    Then the response status should be 404
    And the response success flag should be false

  @AuthenticationSites
  Scenario: Successfully recover authentication sites
    Given the authentication sites service is ready
    When I request to recover authentication sites
    Then the response status should be 200

  @BlockedContract
  Scenario: Successfully recover blocked contracts
    Given the blocked contract service is ready
    When I request to recover blocked contracts
    Then the response status should be 200

  @Consumption
  Scenario: Successfully recover consumption data
    Given the consumption service is ready
    When I request to recover consumption data for contract 123
    Then the response status should be 200

  @ContractActivation
  Scenario: Successfully recover contract activations
    Given the contract activation service is ready
    When I request to recover contract activations
    Then the response status should be 200

  @ContractActivationInvoice
  Scenario: Successfully recover contract activation invoices
    Given the contract activation invoice service is ready
    When I request to recover contract activation invoices
    Then the response status should be 200

  @ContractPayment
  Scenario: Successfully recover contract payments
    Given the contract payment service is ready
    When I request to recover contract payments
    Then the response status should be 200

  @ContractWithoutInvoice
  Scenario: Successfully recover contracts without invoice
    Given the contract without invoice service is ready
    When I request to recover contracts without invoice
    Then the response status should be 200

  @DuplicateCallingStation
  Scenario: Successfully recover duplicate calling stations
    Given the duplicate calling station service is ready
    When I request to recover duplicate calling stations
    Then the response status should be 200

  @DuplicateClientName
  Scenario: Successfully recover duplicate client name report
    Given the duplicate client name report service is ready
    When I request to recover duplicate client name report
    Then the response status should be 200

  @DuplicatePrefix
  Scenario: Successfully recover duplicate prefixes
    Given the duplicate prefix service is ready
    When I request to recover duplicate prefixes
    Then the response status should be 200

  @Employee
  Scenario: Successfully recover employees
    Given the employee service is ready
    When I request to recover employees
    Then the response status should be 200

  @FirstAuthentication
  Scenario: Successfully recover first authentications
    Given the first authentication service is ready
    When I request to recover first authentications
    Then the response status should be 200

  @FirstMonthlyPayer
  Scenario: Successfully recover first monthly payers
    Given the first monthly payer service is ready
    When I request to recover first monthly payers
    Then the response status should be 200

  @Inventory
  Scenario: Successfully recover inventory data
    Given the inventory service is ready
    When I request to recover inventory data
    Then the response status should be 200

  @InventoryMoves
  Scenario: Successfully recover inventory moves
    Given the inventory moves service is ready
    When I request to recover inventory moves
    Then the response status should be 200

  @LastConnection
  Scenario: Successfully recover last connections
    Given the last connection service is ready
    When I request to recover last connections
    Then the response status should be 200

  @Matrix
  Scenario: Successfully recover matrix data
    Given the matrix service is ready
    When I request to recover matrix data
    Then the response status should be 200

  @PendingAsset
  Scenario: Successfully recover pending assets
    Given the pending asset service is ready
    When I request to recover pending assets
    Then the response status should be 200

  @QrCode
  Scenario: Successfully generate QrCode
    Given the QrCode service is ready
    When I request to generate QrCode with data "test-data"
    Then the response status should be 200

  @Splitters
  Scenario: Successfully list splitters
    Given the splitters service is ready
    When I request to list all splitters
    Then the response status should be 200

  @WeeklyReport
  Scenario: Successfully recover weekly report
    Given the weekly report service is ready
    When I request to recover weekly report
    Then the response status should be 200
