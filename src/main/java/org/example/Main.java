package org.example;

import TestCases.*;
import org.testng.TestNG;

public class Main {

    public static void main(String[] args) {
        // before TestNG executes any class. execute setup
        BaseTest bootstrap = new BaseTest();
        bootstrap.suiteSetUp();

        try {
            TestNG testng = new TestNG();
            testng.setTestClasses(new Class[]{
                LoginTest.class,
                    ProfileManagementTest.class,
                HomePageTest.class,
                SearchTest.class,
                NavigationTest.class,
                SubServicesTest.class,
                SettingsTest.class,
                ShowTest.class
            });
            testng.run();
        } finally {
            // Ensure browser is closed exactly once at suite end.
            bootstrap.suiteTearDown();
        }
    }
}
