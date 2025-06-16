# WhatsApp Integration for WebStore

This integration enables customers to browse your WebStore catalog directly through WhatsApp, providing a conversational shopping experience.

## Setup Guide

### Prerequisites
- Meta Developer account
- Business Manager account
- Spring Boot WebStore application
- Ngrok (for local testing)

### 1. Meta Developer Account Setup

1. Create account at [Meta for Developers](https://developers.facebook.com/)
2. Create a new app:
    - Click "Create App"
    - Select "Business" type
    - Enter app name and contact email

3. Add WhatsApp product:
    - From dashboard, find "Add Products"
    - Click "Set Up" next to WhatsApp

### 2. WhatsApp Business API Configuration

1. Register test phone number:
    - Go to WhatsApp > Configuration
    - Add your phone number for testing
    - Verify with code received

2. Collect API credentials:
    - **API URL**: `https://graph.facebook.com/v17.0`
    - **Phone Number ID**: Found in WhatsApp > API Setup
    - **Access Token**: Generate from WhatsApp > API Setup > Temporary access token
    - **Verify Token**: Create your own secure string (e.g., `webstore-whatsapp-verify-12345`)
    - **Business Account ID**: Found in Meta Business Settings > Business Details

### 3. Application Configuration

Add these properties to `application.properties`:

```properties
# WhatsApp API Configuration
whatsapp.api-url=https://graph.facebook.com/v17.0
whatsapp.phone-number-id=YOUR_PHONE_NUMBER_ID
whatsapp.access-token=YOUR_ACCESS_TOKEN
whatsapp.verify-token=YOUR_VERIFY_TOKEN
whatsapp.business-account-id=YOUR_BUSINESS_ACCOUNT_ID
```

### 4. Webhook Setup

1. Start your application
2. For local testing, start Ngrok:
   ```
   ngrok http 8080
   ```

3. Configure webhook in Meta Dashboard:
    - Go to WhatsApp > Configuration > Webhooks
    - Click "Add Webhook" or "Configure"
    - Enter webhook URL: `https://your-ngrok-url/api/webhook/whatsapp`
    - Enter your verify token (same as in application.properties)
    - Subscribe to "messages" field
    - Click "Verify and Save"

### 5. Add Test Numbers

1. Go to WhatsApp > Configuration > Test Numbers
2. Add up to 5 phone numbers for testing
3. Each number will receive a verification code

## Testing

1. Send message from test phone to your WhatsApp Business number
2. Should receive welcome message with browsing options
3. Test full conversation flow:
    - Browse catalogs
    - Navigate through categories
    - View products with prices

### Webhook Verification Fails
- Ensure verify token matches exactly in both Meta Dashboard and application.properties
- Check server is publicly accessible
- Verify logs for incoming verification request

### Messages Not Being Received
- Confirm webhook subscription to "messages" field
- Check application logs for webhook events
- Verify webhook controller is correctly processing payload

### No Response to Messages
- Check access token is valid and not expired
- Verify phone number ID is correct
- Look for API error responses in logs

## Dependencies

- Spring Boot
- OkHttp3 for API calls
- Jackson for JSON processing