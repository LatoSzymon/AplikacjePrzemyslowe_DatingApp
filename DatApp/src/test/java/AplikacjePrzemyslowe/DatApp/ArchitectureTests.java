package AplikacjePrzemyslowe.DatApp;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

@DisplayName("Architecture Tests - ArchUnit")
class ArchitectureTests {

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
                .importPackages("AplikacjePrzemyslowe.DatApp");
    }

    @Test
    @DisplayName("1. Controllers should be in controller package")
    void controllerPackageRule() {
        ArchRule rule = classes()
                .that().haveNameMatching(".*Controller")
                .should().resideInAPackage("AplikacjePrzemyslowe.DatApp.controller")
                .as("Controllers should be in .controller package");

        rule.check(classes);
    }

    @Test
    @DisplayName("2. Services should be in service package")
    void servicePackageRule() {
        ArchRule rule = classes()
                .that().haveNameMatching(".*Service")
                .should().resideInAPackage("AplikacjePrzemyslowe.DatApp.service")
                .as("Services should be in .service package");

        rule.check(classes);
    }

    @Test
    @DisplayName("3. Repositories should be in repository package")
    void repositoryPackageRule() {
        ArchRule rule = classes()
                .that().haveNameMatching(".*Repository")
                .should().resideInAPackage("AplikacjePrzemyslowe.DatApp.repository")
                .as("Repositories should be in .repository package");

        rule.check(classes);
    }

    @Test
    @DisplayName("4. Entities should be in entity package")
    void entityPackageRule() {
        ArchRule rule = classes()
                .that().haveNameMatching(".*Entity|User|Profile|Swipe|Match|Message|.*")
                .and().areAnnotatedWith(jakarta.persistence.Entity.class)
                .should().resideInAPackage("AplikacjePrzemyslowe.DatApp.entity")
                .as("@Entity classes should be in .entity package");

        rule.check(classes);
    }

    @Test
    @DisplayName("5. DTOs should be in dto package")
    void dtoPackageRule() {
        ArchRule rule = classes()
                .that().haveNameMatching(".*(Request|Response|Dto)")
                .should().resideInAnyPackage("AplikacjePrzemyslowe.DatApp.dto..")
                .as("DTOs should be in .dto package or subpackages");

        rule.check(classes);
    }

    @Test
    @DisplayName("6. Controllers should not depend on Entity classes directly")
    void controllerEntityDependencyRule() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("AplikacjePrzemyslowe.DatApp.controller")
                .should().dependOnClassesThat().resideInAPackage("AplikacjePrzemyslowe.DatApp.entity")
                .as("Controllers should use DTOs, not Entity classes");

        rule.check(classes);
    }

    @Test
    @DisplayName("7. Services should not depend on Controller classes")
    void serviceControllerDependencyRule() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("AplikacjePrzemyslowe.DatApp.service")
                .should().dependOnClassesThat().resideInAPackage("AplikacjePrzemyslowe.DatApp.controller")
                .as("Services should not depend on Controllers");

        rule.check(classes);
    }

    @Test
    @DisplayName("8. Services should depend on Repositories (dependency injection)")
    void serviceRepositoryDependencyRule() {
        ArchRule rule = classes()
                .that().resideInAPackage("AplikacjePrzemyslowe.DatApp.service")
                .and().haveNameMatching(".*Service")
                .should().dependOnClassesThat().resideInAPackage("AplikacjePrzemyslowe.DatApp.repository")
                .because("Services need Repositories for data access")
                .allowEmptyShould(true);

        rule.check(classes);
    }

    @Test
    @DisplayName("9. Exception classes should be in exception package")
    void exceptionPackageRule() {
        ArchRule rule = classes()
                .that().haveNameMatching(".*Exception")
                .should().resideInAPackage("AplikacjePrzemyslowe.DatApp.exception")
                .as("Exceptions should be in .exception package");

        rule.check(classes);
    }

    @Test
    @DisplayName("10. Config classes should be in config package")
    void configPackageRule() {
        ArchRule rule = classes()
                .that().haveNameMatching(".*Config")
                .should().resideInAPackage("AplikacjePrzemyslowe.DatApp.config")
                .as("Config classes should be in .config package");

        rule.check(classes);
    }
}

