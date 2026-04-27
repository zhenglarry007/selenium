# 🚀 Selenium Java Test Framework - Your UI Testing BFF

Hey there! 👋 Let me introduce you to this cool Selenium Java test framework that I've been working on. It's basically your best friend when it comes to UI automation testing.

---

## 🤔 What Problem Does This Solve?

Let's be real - writing Selenium tests from scratch every time is... well, kinda boring and repetitive. You know the drill:

- "Ugh, I need to set up the WebDriver again..."
- "Wait, how do I handle waits properly?"
- "Why isn't my Page Object working?"
- "Darn, the test failed - where's the screenshot?!"

This framework takes care of all that boilerplate stuff so you can focus on what actually matters: **writing test cases**.

---

## ✨ What's In The Box?

### 🎯 Page Object Model (POM) - But Better

We all know POM is the way to go. But here, it's **actually well-designed**:

```
AbstractPageObject (the OG parent - handles PageFactory magic)
         ↓
    NavigationPage (adds Next/Previous/Finish buttons)
         ↓
AccountPage / RoomPage / DetailPage (your actual pages)
```

And if you have a standalone page like a Dashboard? Just inherit directly from `AbstractPageObject`. **Clean and simple.**

### 🎲 Test Data That Actually Works

Tired of hardcoding test data? Yeah, me too. That's why we have **3 ways** to feed your tests:

| Method | How It Works | Best For |
|--------|-------------|---------|
| **JSON/CSV/YAML** | Load from files | Regression tests, fixed scenarios |
| **DataFaker** | Generate random data on the fly | Exploratory testing, boundary coverage |
| **Mix & Match** | Use both! | Maximum flexibility |

Want to book 5 rooms with **different random data** in a **single browser session**? We got you:

```java
for (int i = 1; i <= 5; i++) {
    Booking booking = BookingDataFactory.createBookingData();  // NEW data every time!
    executeBooking(booking);
    navigateBackToHome();  // Refresh URL for next round
}
```

### ⏰ Smart Waits That Don't Suck

No more `Thread.sleep(5000)` (we've all been there 😅). Check out the `Waits` utility class:

- `waitForVisibility()` - Wait until element is visible
- `waitForClickability()` - Wait until you can actually click it
- `waitForTextToChange()` - Perfect for AJAX updates
- `waitForListToLoad()` - For those dynamic tables that take forever
- `waitForCustomCondition()` - When you need something special

### 📸 Allure Reports That Actually Help

Failed tests should tell you **what went wrong**, not just "it failed".

When a test fails, we automatically attach:
- 🖼️ **Screenshot** - What did the page look like?
- 📄 **Page Source** - The HTML at that moment
- 🖥️ **Console Logs** - Any JS errors?
- 🌐 **Network Errors** - 4xx/5xx responses?

And yes, **retry support** is built-in. Configure different retry counts for local vs CI:

```properties
retry.local = 0    # Local - fail fast so you can debug
retry.ci = 2       # CI - be more forgiving of flakiness
```

---

## 🚀 Quick Start (Like, Really Quick)

### 1. Clone This Bad Boy

```bash
git clone https://github.com/zhenglarry007/selenium.git
cd selenium
```

### 2. Make Sure Your App Is Running

Default URL is `http://localhost:5173/` (change in `general.properties` if needed).

### 3. Run The Tests

```bash
mvn test -Pweb-execution -Dsuite=local
```

### 4. Check The Report

```bash
mvn allure:serve
```

Your browser will pop open with a beautiful report. You're welcome. 🎉

---

## 🎨 Architecture Overview (For The Visual Learners)

```
┌─────────────────────────────────────────────────────────────────┐
│                        Your Test Classes                          │
│  (BookRoomWebTest, DashboardTest, LoginTest)                    │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Page Objects (POM)                          │
│  AccountPage → RoomPage → DetailPage                            │
│  DashboardPage (standalone)                                     │
│  (All inherit from AbstractPageObject for PageFactory magic)    │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                     Utilities & Helpers                          │
│  Waits (smart waiting)                                          │
│  UiAssertions (fluent assertions)                               │
│  TestDataProvider (JSON/CSV/YAML + Faker)                      │
│  AllureFailureAttachment (auto-attach on failure)              │
└─────────────────────────────────────────────────────────────────┘
```

---

## 💡 Why This Framework?

| Feature | Without This Framework | With This Framework |
|---------|-----------------------|---------------------|
| Setting up WebDriver | 20+ lines of code | Handled by `BaseWeb` |
| Page Factory setup | Manual init every time | Automatic in constructor |
| Test data management | Hardcoded strings | JSON/CSV/YAML + Faker |
| Smart waits | `Thread.sleep()` everywhere | `Waits` utility class |
| Failure debugging | "What happened?!" | Auto-attach screenshot/logs/source |
| Report generation | Manual setup | `mvn allure:serve` |

---

## 🤝 Want To Contribute?

This is an open-source project, so feel free to:
- ⭐ Star the repo if you find it useful
- 🐛 Report issues if you find bugs
- 🔧 Submit PRs for new features
- 💬 Give feedback - I'd love to hear your thoughts!

---

## 📝 Final Words

At the end of the day, this framework is all about **making your life easier**. UI testing shouldn't be a chore - it should be straightforward, maintainable, and actually provide value.

Whether you're:
- 🧑‍💻 A beginner learning Selenium
- 👨‍🔬 A QA engineer setting up a test suite
- 🎯 A developer who wants to add UI tests to your project

...this framework has got your back.

Happy testing! 🎉

---

**P.S.** If you like this project, don't forget to give it a star ⭐ It really helps!
