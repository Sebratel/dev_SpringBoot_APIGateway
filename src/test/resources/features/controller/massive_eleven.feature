Feature: Massive Eleven Management
  As a developer
  I want to ensure massive eleven endpoints are functional
  So that the API remains reliable

  @MassiveEleven
  Scenario: Successfully recover all massive incidents from database
    Given the massive service is ready to recover from database
    When I request to recover all massive incidents from database
    Then the response status should be 200

  @MassiveEleven
  Scenario: Successfully recover all massive incidents from API
    Given the massive service is ready to list all massives
    When I request to recover all massive incidents from API
    Then the response status should be 200

  @MassiveEleven
  Scenario: Successfully list all massive incidents
    Given the massive service is ready to list all massives
    When I request to list all massive incidents
    Then the response status should be 200

  @MassiveEleven
  Scenario: Successfully close a massive incident
    Given the massive service is ready to close an incident
    When I request to close massive incident with ID 1
    Then the response status should be 200
