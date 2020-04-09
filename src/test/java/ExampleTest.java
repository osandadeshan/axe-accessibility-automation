import com.deque.axe.AXE;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.net.URL;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

/**
 * Project Name    : accessibility-demo
 * Developer       : Osanda Deshan
 * Version         : 1.0.0
 * Date            : 4/9/2020
 * Time            : 9:25 AM
 * Description     : This is a sample test for accessibility testing using AXE.
 **/


public class ExampleTest {

    private static final URL scriptUrl = ExampleTest.class.getResource("/axe.min.js");
    private WebDriver driver;

    @Rule
    public TestName testName = new TestName();

    // Instantiate the WebDriver and navigate to the test site
    @BeforeTest
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
    }

    // Ensure we close the WebDriver after finishing
    @AfterTest
    public void tearDown() {
        driver.quit();
    }

    // Basic test
    @Test
    public void testAccessibility() {
        driver.get("http://localhost:5005");
        JSONObject responseJSON = new AXE.Builder(driver, scriptUrl).analyze();
        JSONArray violations = responseJSON.getJSONArray("violations");
        if (violations.length() == 0) {
            assertTrue("No violations found", true);
        } else {
            AXE.writeResults(testName.getMethodName(), responseJSON);
            fail(AXE.report(violations));
        }
    }

    // Test with skip frames
    @Test
    public void testAccessibilityWithSkipFrames() {
        driver.get("http://localhost:5005");
        JSONObject responseJSON = new AXE.Builder(driver, scriptUrl)
                .skipFrames()
                .analyze();
        JSONArray violations = responseJSON.getJSONArray("violations");
        if (violations.length() == 0) {
            assertTrue("No violations found", true);
        } else {
            AXE.writeResults(testName.getMethodName(), responseJSON);
            fail(AXE.report(violations));
        }
    }

    // Test with options
    @Test
    public void testAccessibilityWithOptions() {
        driver.get("http://localhost:5005");
        JSONObject responseJSON = new AXE.Builder(driver, scriptUrl)
                .options("{ rules: { 'accesskeys': { enabled: false } } }")
                .analyze();
        JSONArray violations = responseJSON.getJSONArray("violations");
        if (violations.length() == 0) {
            assertTrue("No violations found", true);
        } else {
            AXE.writeResults(testName.getMethodName(), responseJSON);

            fail(AXE.report(violations));
        }
    }

    @Test
    public void testCustomTimeout() {
        driver.get("http://localhost:5005");
        boolean didTimeout = false;
        try {
            new AXE.Builder(driver, ExampleTest.class.getResource("/timeout.js"))
                    .setTimeout(1)
                    .analyze();
        } catch (Exception e) {
            String msg = e.getMessage();
            if (!msg.contains("1 seconds")) {
                assertTrue("Did not find an error with timeout message", msg.contains("1 seconds"));
            }
            didTimeout = true;
        }
        assertTrue("Did set custom timeout", didTimeout);
    }

    // Test a specific selector or selectors
    @Test
    public void testAccessibilityWithSelector() {
        driver.get("http://localhost:5005");
        JSONObject responseJSON = new AXE.Builder(driver, scriptUrl)
                .include("title")
                .include("p")
                .analyze();
        JSONArray violations = responseJSON.getJSONArray("violations");
        if (violations.length() == 0) {
            assertTrue("No violations found", true);
        } else {
            AXE.writeResults(testName.getMethodName(), responseJSON);

            fail(AXE.report(violations));
        }
    }

    // Test includes and excludes
    @Test
    public void testAccessibilityWithIncludesAndExcludes() {
        driver.get("http://localhost:5005/include-exclude.html");
        JSONObject responseJSON = new AXE.Builder(driver, scriptUrl)
                .include("body")
                .exclude("h1")
                .exclude("h2")
                .analyze();
        JSONArray violations = responseJSON.getJSONArray("violations");
        if (violations.length() == 0) {
            assertTrue("No violations found", true);
        } else {
            AXE.writeResults(testName.getMethodName(), responseJSON);
            fail(AXE.report(violations));
        }
    }

    // Test a WebElement
    @Test
    public void testAccessibilityWithWebElement() {
        driver.get("http://localhost:5005");
        JSONObject responseJSON = new AXE.Builder(driver, scriptUrl)
                .analyze(driver.findElement(By.tagName("p")));
        JSONArray violations = responseJSON.getJSONArray("violations");
        if (violations.length() == 0) {
            assertTrue("No violations found", true);
        } else {
            AXE.writeResults(testName.getMethodName(), responseJSON);
            fail(AXE.report(violations));
        }
    }

    // Test WebElements
    @Test
    public void testAccessibilityWithFewWebElements() {
        driver.get("http://localhost:5005/include-exclude.html");
        JSONObject responseJSON = new AXE.Builder(driver, scriptUrl)
                .analyze(driver.findElement(By.tagName("h1")));
        JSONArray violations = responseJSON.getJSONArray("violations");
        JSONArray nodes = ((JSONObject) violations.get(0)).getJSONArray("nodes");
        JSONArray target1 = ((JSONObject) nodes.get(0)).getJSONArray("target");
        JSONArray target2 = ((JSONObject) nodes.get(1)).getJSONArray("target");
        if (violations.length() == 1) {
            assertEquals(String.valueOf(target1), "[\"h1 > span\"]");
            assertEquals(String.valueOf(target2), "[\"h2 > span\"]");
        } else {
            AXE.writeResults(testName.getMethodName(), responseJSON);
            fail("No violations found");
        }
    }

    // Test a page with Shadow DOM violations\
    @Test
    public void testAccessibilityWithShadowElement() {
        driver.get("http://localhost:5005/shadow-error.html");
        JSONObject responseJSON = new AXE.Builder(driver, scriptUrl).analyze();
        JSONArray violations = responseJSON.getJSONArray("violations");
        JSONArray nodes = ((JSONObject) violations.get(0)).getJSONArray("nodes");
        JSONArray target = ((JSONObject) nodes.get(0)).getJSONArray("target");
        if (violations.length() == 1) {
//			assertTrue(AXE.report(violations), true);
            assertEquals(String.valueOf(target), "[[\"#upside-down\",\"ul\"]]");
        } else {
            AXE.writeResults(testName.getMethodName(), responseJSON);
            fail("No violations found");
        }
    }

    @Test
    public void testAxeErrorHandling() {
        driver.get("http://localhost:5005/");
        URL errorScript = ExampleTest.class.getResource("/axe-error.js");
        AXE.Builder builder = new AXE.Builder(driver, errorScript);
        boolean didError = false;
        try {
            builder.analyze();
        } catch (AXE.AxeRuntimeException e) {
            assertEquals(e.getMessage(), "boom!"); // See axe-error.js
            didError = true;
        }
        assertTrue("Did raise axe-core error", didError);
    }

    // Test few include
    @Test
    public void testAccessibilityWithFewInclude() {
        driver.get("http://localhost:5005/include-exclude.html");
        JSONObject responseJSON = new AXE.Builder(driver, scriptUrl)
                .include("div")
                .include("p")
                .analyze();
        JSONArray violations = responseJSON.getJSONArray("violations");
        if (violations.length() == 0) {
            assertTrue("No violations found", true);
        } else {
            AXE.writeResults(testName.getMethodName(), responseJSON);
            fail(AXE.report(violations));
        }
    }

    // Test includes and excludes with violation
    @Test
    public void testAccessibilityWithIncludesAndExcludesWithViolation() {
        driver.get("http://localhost:5005/include-exclude.html");
        JSONObject responseJSON = new AXE.Builder(driver, scriptUrl)
                .include("body")
                .exclude("div")
                .analyze();
        JSONArray violations = responseJSON.getJSONArray("violations");
        JSONArray nodes = ((JSONObject) violations.get(0)).getJSONArray("nodes");
        JSONArray target = ((JSONObject) nodes.get(0)).getJSONArray("target");
        if (violations.length() == 1) {
            assertEquals(String.valueOf(target), "[\"h1 > span\"]");
        } else {
            AXE.writeResults(testName.getMethodName(), responseJSON);
            fail("No violations found");
        }
    }


}
