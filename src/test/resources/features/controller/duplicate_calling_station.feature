Feature: Duplicate Calling Station Management
  As a developer
  I want to ensure duplicate calling station endpoints are functional
  So that the API remains reliable

  @DuplicateCallingStation
  Scenario: Successfully recover duplicate calling stations
    Given the duplicate calling station service is ready
    When I request to recover duplicate calling stations
    Then the response status should be 200
