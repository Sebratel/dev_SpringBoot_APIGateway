Feature: QrCode Management
  As a developer
  I want to ensure QrCode endpoints are functional
  So that the API remains reliable

  @QrCode
  Scenario: Successfully generate QrCode
    Given the QrCode service is ready
    When I request to generate QrCode with data "test-data"
    Then the response status should be 200
