@diagnostic @edge-case
Feature: Classification des valeurs de diagnostic

  Scenario Outline: Le signe distingue les valeurs negatives, nulles et positives
    Given une valeur de <value>
    When son signe est determine
    Then le signe est <sign>

    Examples:
      | value       | sign |
      | -2147483648 | -1   |
      | -1          | -1   |
      | 0           | 0    |
      | 1           | 1    |
      | 2147483647  | 1    |
