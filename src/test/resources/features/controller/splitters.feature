Feature: Splitters Management
  As a developer
  I want to ensure splitters endpoints are functional
  So that the API remains reliable

  @Splitters
  Scenario: Successfully list splitters
    Given the splitters service is ready
    When I request to list all splitters
    Then the response status should be 200
