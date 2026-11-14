package io.microcks.foundry.application.diagnostic;

import io.microcks.foundry.domain.diagnostic.DiagnosticSign;

/** Temporary application entry point for the executable Gherkin probe. */
public final class DiagnosticClassification {
   public int classify(int value) {
      return DiagnosticSign.classify(value);
   }
}
