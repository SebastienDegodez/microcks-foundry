package io.microcks.foundry.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTest {
   private static JavaClasses modules;

   @BeforeAll
   static void importModules() {
      modules = new ClassFileImporter().importPackages("io.microcks.foundry.domain", "io.microcks.foundry.application",
            "io.microcks.foundry.infrastructure");
   }

   @Test
   void domainDoesNotDependOnOuterLayersOrQuarkus() {
      ArchRule rule = noClasses().that().resideInAPackage("io.microcks.foundry.domain..").should().dependOnClassesThat()
            .resideInAnyPackage("io.microcks.foundry.application..", "io.microcks.foundry.infrastructure..",
                  "io.microcks.foundry.app..", "io.microcks.foundry.cli..", "io.quarkus..")
            .as("dépendance interdite : domaine → adaptateur, cas d'usage ou Quarkus");

      rule.check(modules);
   }

   @Test
   void applicationDoesNotDependOnAdaptersOrFrameworks() {
      ArchRule rule = noClasses().that().resideInAPackage("io.microcks.foundry.application..").should()
            .dependOnClassesThat()
            .resideInAnyPackage("io.microcks.foundry.infrastructure..", "io.microcks.foundry.app..",
                  "io.microcks.foundry.cli..", "io.quarkus..")
            .as("dépendance interdite : cas d'usage → adaptateur, point d'entrée ou Quarkus");

      rule.check(modules);
   }

   @Test
   void infrastructureDoesNotDependOnEntryPoints() {
      ArchRule rule = noClasses().that().resideInAPackage("io.microcks.foundry.infrastructure..").should()
            .dependOnClassesThat().resideInAnyPackage("io.microcks.foundry.app..", "io.microcks.foundry.cli..")
            .as("dépendance interdite : infrastructure → point d'entrée");

      rule.check(modules);
   }
}
