package com.college.eventmanager;

/**
 * Master automated test runner that executes all unit tests and outputs pass/fail status.
 */
public class AppTestSuite {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   College Event & Workshop Manager - Test Suite  ");
        System.out.println("==================================================");

        int passed = 0;
        int failed = 0;

        // 1. PasswordUtil Tests
        try {
            System.out.print("[TEST] PasswordUtil.testPasswordHashing() ... ");
            PasswordUtilTest.testPasswordHashing();
            System.out.println("PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            failed++;
        }

        try {
            System.out.print("[TEST] PasswordUtil.testPasswordVerification() ... ");
            PasswordUtilTest.testPasswordVerification();
            System.out.println("PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            failed++;
        }

        // 2. Model Tests
        try {
            System.out.print("[TEST] ModelTest.testUserInheritanceAndPolymorphism() ... ");
            ModelTest.testUserInheritanceAndPolymorphism();
            System.out.println("PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            failed++;
        }

        try {
            System.out.print("[TEST] ModelTest.testEventCapacityLogic() ... ");
            ModelTest.testEventCapacityLogic();
            System.out.println("PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            passed++;
        }

        try {
            System.out.print("[TEST] ModelTest.testRegistrationStatus() ... ");
            ModelTest.testRegistrationStatus();
            System.out.println("PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            failed++;
        }

        // 3. Validation Tests
        try {
            System.out.print("[TEST] ValidationTest.testAuthValidation() ... ");
            ValidationTest.testAuthValidation();
            System.out.println("PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            failed++;
        }

        try {
            System.out.print("[TEST] ValidationTest.testEventValidation() ... ");
            ValidationTest.testEventValidation();
            System.out.println("PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("FAILED: " + t.getMessage());
            failed++;
        }

        System.out.println("==================================================");
        System.out.println("Summary: Total Tests: " + (passed + failed) + " | Passed: " + passed + " | Failed: " + failed);
        System.out.println("==================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }
}
