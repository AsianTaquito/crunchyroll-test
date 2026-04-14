# Crunchyroll Automated Test Suite

End-to-end UI test suite for [crunchyroll.com](https://www.crunchyroll.com), built with **Java**, **Selenium WebDriver**, and **TestNG**. The suite covers 40 test cases across 8 functional areas, running inside a single shared Chrome session from suite start to finish.

---

## Prerequisites

| Requirement | Version |
|---|---|
| Java (JDK) | 17+ |
| Maven | 3.8+ |
| Google Chrome | Latest stable |
| Git | Any recent |

> ChromeDriver is managed automatically by **WebDriverManager** — no manual download required.

---

## Setup

**1. Clone the repository**
```bash
git clone <repo-url>
cd crunchyroll-test
```

**2. Install dependencies**
```bash
mvn install
```

**3. Configure credentials**

Open `src/main/java/TestCases/BaseTest.java` and set your Crunchyroll account details:
```java
protected static final String VALID_EMAIL    = "your@email.com";
protected static final String VALID_PASSWORD = "yourpassword";
```

---

## Running the Tests

```bash
mvn test
```

Or run directly with TestNG via Maven:
```bash
mvn -Dtest=LoginTest,HomePageTest,NavigationTest,SearchTest,SettingsTest,ShowTest,ProfileManagementTest,SubServicesTest test
```

---

## First Run — CAPTCHA Handling

On the very first run, the suite pauses for **30 seconds** to allow manual CAPTCHA solving if one appears in the browser. After the first successful login, session cookies are saved to `crunchyroll_session.cookies` in the project root. Every subsequent run injects these cookies automatically, bypassing the CAPTCHA entirely.

> **Do not delete** `crunchyroll_session.cookies` unless you want to re-authenticate from scratch.

---

## Test Structure

| Class | Test IDs | Coverage |
|---|---|---|
| `LoginTest` | TC-LGN-01 → 05 | Login/signup buttons, input fields, forgot password, invalid & valid login |
| `HomePageTest` | TC-HP-01 → 05 | Title & logo, hero carousel, continue watching, footer links, content count |
| `NavigationTest` | TC-NAV-01 → 05 | Logo nav, header links, watchlist/crunchylists/history, categories, search icon |
| `SearchTest` | TC-SR-01 → 05 | Search page load, empty search safety, full title, keyword, abbreviation |
| `SettingsTest` | TC-ST-01 → 05 | Settings page, subtitle language, notification toggle, email change, customer support |
| `ShowTest` | TC-VP-01 → 05 | Series info, episode details, season dropdown, video playback, continue watching |
| `ProfileManagementTest` | TC-PM-01 → 05 | Logout/re-login, create profile, edit avatar, delete profile, switch profile |
| `SubServicesTest` | TC-SS-01 → 05 | Manga, Games, News, Store sub-services, account data transfer to store |

**Total: 40 test cases**

---

## Project Architecture

```
crunchyroll-test/
├── src/main/java/
│   └── TestCases/
│       ├── BaseTest.java               ← Shared driver, session, helpers
│       ├── LoginTest.java              ← TC-LGN
│       ├── HomePageTest.java           ← TC-HP
│       ├── NavigationTest.java         ← TC-NAV
│       ├── SearchTest.java             ← TC-SR
│       ├── SettingsTest.java           ← TC-ST
│       ├── ShowTest.java               ← TC-VP
│       ├── ProfileManagementTest.java  ← TC-PM
│       └── SubServicesTest.java        ← TC-SS
├── test-output/                        ← TestNG HTML reports (auto-generated)
├── crunchyroll_session.cookies         ← Saved session (auto-generated on first login)
├── TestingReport.md                    ← Project report
├── TestingWorkflow.drawio              ← Test pipeline diagram
└── pom.xml
```

### BaseTest — Shared Infrastructure

All test classes extend `BaseTest`, which provides:
- A **single Chrome session** opened at suite start (`@BeforeSuite`) and closed at suite end (`@AfterSuite`)
- **Cookie persistence** — saves and injects session cookies to skip CAPTCHA on repeat runs
- **Persistent Chrome profile** stored in `.chrome-test-profile/` to further reduce bot detection
- Shared helper methods: `clickElement()`, `isElementPresent()`, `dismissBanners()`, `scrollToBottom()`, `loginWithValidCredentials()`

---

## Test Reports

TestNG generates HTML reports automatically after every run.

Open `test-output/index.html` in a browser to view the full results, including pass/fail status, test durations, and any failure messages.

---

## Notes

- Tests within each class use `dependsOnMethods` to run in a defined order — if an early test fails, downstream tests in the same class are automatically skipped.
- The suite uses a **real account** (not a mock) and interacts with the live Crunchyroll site, so occasional flakiness due to network latency or site changes is expected.
- `SubServicesTest` and `ShowTest` include extended `Thread.sleep` pauses intentionally — these allow content to visually load for demonstration/presentation purposes.

