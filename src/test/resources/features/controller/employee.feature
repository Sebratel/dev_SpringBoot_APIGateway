Feature: Employee Management
  As a developer
  I want to ensure employee endpoints are functional
  So that the API remains reliable

  @Employee
  Scenario: Successfully recover employees
    Given the employee service is ready
    When I request to recover employees
    Then the response status should be 200
