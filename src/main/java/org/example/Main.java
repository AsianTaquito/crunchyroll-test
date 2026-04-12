package org.example;

import TestCases.BaseTest;
import org.testng.TestNG;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        // Force startup handling (cookies, premium/error recovery, banner dismiss)
        // before TestNG executes any class.
        BaseTest bootstrap = new BaseTest();
        bootstrap.suiteSetUp();

        try {
            // Single source of truth for suite/test order: testng.xml
            TestNG testng = new TestNG();
            testng.setTestSuites(List.of("testng.xml"));
            testng.run();
        } finally {
            // Ensure browser is closed exactly once at suite end.
            bootstrap.suiteTearDown();
        }
    }
}
