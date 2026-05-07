Feature: Duplicate Prefix Management
  As a developer
  I want to ensure duplicate prefix endpoints are functional
  So that the API remains reliable

  @DuplicatePrefix
  Scenario: Successfully recover duplicate prefixes
    Given the duplicate prefix service is ready
    When I request to recover duplicate prefixes
    Then the response status should be 200
