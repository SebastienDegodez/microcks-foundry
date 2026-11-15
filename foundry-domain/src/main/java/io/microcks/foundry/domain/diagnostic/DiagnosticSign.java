package io.microcks.foundry.domain.diagnostic;

/** Temporary probe for the mutation-test setup, not a product rule. */
public final class DiagnosticSign {
   private DiagnosticSign() {
   }

   public static int classify(int value) {
      if (value < 0) {
         return -1;
      }
      if (value > 0) {
         return 1;
      }
      return 0;
   }
}
