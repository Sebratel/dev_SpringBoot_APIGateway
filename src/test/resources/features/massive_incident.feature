Feature: Massive Incident Management
  As an administrator
  I want to create massive incidents in the ERP
  So that customer service agents are aware of outages

  Scenario: Successfully create a massive incident with valid data
    Given the ERP system is ready to accept a massive incident
    When I submit a request to create a massive incident with:
      | startDate              | 06/04/2026      |
      | startTime              | 10:00           |
      | accessPointIds         | 1               |
      | assignmentDescription  | Test Outage     |
      | maintenanceDate        | 06/04/2026      |
      | maintenanceTime        | 11:00           |
    Then the massive incident should be successfully created
    And the response should contain a valid incident ID