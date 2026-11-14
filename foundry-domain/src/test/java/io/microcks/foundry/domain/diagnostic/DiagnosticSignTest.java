package io.microcks.foundry.domain.diagnostic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DiagnosticSignTest {
   @ParameterizedTest
   @CsvSource({"-2147483648, -1", "-1, -1", "0, 0", "1, 1", "2147483647, 1"})
   void classifiesNegativeZeroAndPositiveValues(int value, int expected) {
      assertEquals(expected, DiagnosticSign.classify(value));
   }
}
