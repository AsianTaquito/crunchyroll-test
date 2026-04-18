package org.example;

import TestCases.*;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        XmlSuite suite = new XmlSuite();
        suite.setName("CrunchyrollSuite");

        // Login test runs first
        XmlTest loginTest = new XmlTest(suite);
        loginTest.setName("LoginTest");
        loginTest.setPreserveOrder(true);
        loginTest.setXmlClasses(Collections.singletonList(
                new XmlClass(LoginTest.class.getName())
        ));

        // All remaining tests run in parallel
        XmlTest parallelTests = new XmlTest(suite);
        parallelTests.setName("ParallelTests");
        parallelTests.setParallel(XmlSuite.ParallelMode.CLASSES);
        parallelTests.setThreadCount(7);
        List<XmlClass> parallelClasses = Arrays.asList(
                new XmlClass(ProfileManagementTest.class.getName()),
                new XmlClass(HomePageTest.class.getName()),
                new XmlClass(SearchTest.class.getName()),
                new XmlClass(NavigationTest.class.getName()),
                new XmlClass(SubServicesTest.class.getName()),
                new XmlClass(SettingsTest.class.getName()),
                new XmlClass(ShowTest.class.getName())
        );
        parallelTests.setXmlClasses(parallelClasses);

        TestNG testng = new TestNG();
        testng.setXmlSuites(Collections.singletonList(suite));
        testng.run();
    }
}
