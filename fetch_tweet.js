const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
  });
  const page = await context.newPage();
  
  const url = 'https://x.com/MrLarus/status/2053121448500179018';
  console.log(`Navigating to ${url}`);
  
  try {
    await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 30000 });
    await page.waitForTimeout(5000); // wait for dynamic content
    
    // Extract text content
    const tweetText = await page.evaluate(() => {
      const tweetElement = document.querySelector('[data-testid="tweetText"]');
      return tweetElement ? tweetElement.innerText : null;
    });
    
    // Extract images
    const images = await page.evaluate(() => {
      const imgElements = document.querySelectorAll('div[data-testid="tweetPhoto"] img');
      return Array.from(imgElements).map(img => img.src);
    });
    
    console.log('--- Extracted Text ---');
    console.log(tweetText || 'No text found (might be login-walled)');
    
    console.log('--- Extracted Images ---');
    console.log(images.length > 0 ? images.join('\n') : 'No images found');
    
  } catch (error) {
    console.error('Error during extraction:', error);
  } finally {
    await browser.close();
  }
})();
