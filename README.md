# Planning and Tasks
Add joinLinks to the api for telegram and whatsapp ***
Update clear cache and clear db file by keeping an backup file--IP 
API to stop particular thread by taskType
Improve Performance
try to use Jar with external properties



# WhatsApp & Telegram Automation API

A Spring Boot application that provides parallel automation capabilities for WhatsApp and Telegram using Selenium WebDriver.

## Features

- **Parallel Automation**: Run WhatsApp and Telegram automations simultaneously
- **Asynchronous Processing**: Non-blocking API endpoints for long-running operations
- **Configurable**: Enable/disable features via application properties
- **Robust Error Handling**: Comprehensive error handling and logging
- **REST API**: Easy integration with other services

## Prerequisites

- Java 17 or higher
- Maven 3.6.3 or higher
- Chrome/Chromium browser installed
- ChromeDriver matching your Chrome version
- WhatsApp Web and Telegram Web accounts logged in

## Configuration

Edit `src/main/resources/application.properties` to configure:

```properties
# WebDriver Configuration
webdriver.chrome.driver=path/to/chromedriver
webdriver.chrome.profile.path=path/to/chrome/profile

# Timeout Settings
webdriver.timeout.page-load=30
webdriver.timeout.implicit=10
webdriver.timeout.explicit=30

# Parallel Execution
automation.parallel.threads=2

# WhatsApp Settings
whatsapp.web.url=https://web.whatsapp.com
whatsapp.qr.wait.seconds=60
whatsapp.group.broadcast.enabled=true
whatsapp.fetch.contacts.enabled=true

# Telegram Settings
telegram.web.url=https://web.telegram.org
telegram.login.wait.seconds=60
telegram.group.broadcast.enabled=true
telegram.fetch.contacts.enabled=true

# Retry Configuration
retry.max.attempts=3
retry.initial.delay=1000
retry.multiplier=2.0
```

## API Endpoints

### Start All Automations
```
POST /api/automation/start
```
Starts all enabled WhatsApp and Telegram automations in parallel.

### WhatsApp Endpoints
```
POST /api/automation/whatsapp/start
```
Starts all WhatsApp automations.

```
POST /api/automation/whatsapp/broadcast
```
Starts only the WhatsApp broadcast.

### Telegram Endpoints
```
POST /api/automation/telegram/start
```
Starts all Telegram automations.

```
POST /api/automation/telegram/broadcast
```
Starts only the Telegram broadcast.

### Data Operations
```
POST /api/automation/fetch-contacts
```
Fetches contacts and groups from all services.

## Response Format

All endpoints return a JSON response with the following structure:

```json
{
  "status": "SUCCESS|ERROR",
  "message": "Operation status message",
  "timestamp": "2025-07-03T13:35:30.123Z"
}
```

## Error Handling

- `200 OK`: Operation completed successfully
- `202 Accepted`: Operation started and is running in the background
- `400 Bad Request`: Invalid request parameters
- `500 Internal Server Error`: An error occurred during processing

## Running the Application

1. Clone the repository
2. Configure `application.properties`
3. Build the application:
   ```
   mvn clean install
   ```
4. Run the application:
   ```
   java -jar target/WA_TG_FB_SeleniumFreelancerAppV2.jar
   ```

## Example Usage

### Start All Automations
```bash
curl -X POST http://localhost:8080/api/automation/start
```

### Start WhatsApp Broadcast
```bash
curl -X POST http://localhost:8080/api/automation/whatsapp/broadcast
```

### Fetch Contacts
```bash
curl -X POST http://localhost:8080/api/automation/fetch-contacts
```

## Logging

Logs are written to `logs/application.log` with the following levels:
- `INFO`: General operation information
- `WARN`: Non-critical issues
- `ERROR`: Critical errors that need attention

## Troubleshooting

1. **ChromeDriver Version Mismatch**
   - Ensure ChromeDriver version matches your Chrome browser version
   - Download the correct version from: https://chromedriver.chromium.org/downloads

2. **Login Issues**
   - Make sure you're logged in to both WhatsApp Web and Telegram Web
   - Check if 2FA is enabled and working

3. **Timeout Errors**
   - Increase timeout values in `application.properties` if operations take longer than expected
   - Check network connectivity

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request
