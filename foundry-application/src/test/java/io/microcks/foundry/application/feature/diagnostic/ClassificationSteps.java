package io.microcks.foundry.application.feature.diagnostic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.microcks.foundry.application.diagnostic.DiagnosticClassification;

public class ClassificationSteps {
   private int value;
   private int sign;

   @Given("une valeur de {int}")
   public void aValue(int value) {
      this.value = value;
   }

   @When("son signe est determine")
   public void determineSign() {
      sign = new DiagnosticClassification().classify(value);
   }

   @Then("le signe est {int}")
   public void expectedSign(int expected) {
      assertEquals(expected, sign);
   }
}
