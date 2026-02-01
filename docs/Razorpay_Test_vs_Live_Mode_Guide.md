# 🔐 Razorpay Test vs Live Mode - Complete Guide

## ⚠️ **CRITICAL**: Test Mode vs Live Mode

### 🧪 **TEST MODE** (Safe for Development)
- **Key Format**: `rzp_test_xxxxxxxxxx`
- **Money**: ❌ **NO REAL MONEY** is charged
- **Cards**: Use test card numbers only
- **Purpose**: Development & testing

### 💰 **LIVE MODE** (Real Money!)
- **Key Format**: `rzp_live_xxxxxxxxxx`
- **Money**: ✅ **REAL MONEY** is charged from actual bank accounts
- **Cards**: Uses real credit/debit cards
- **Purpose**: Production only

---

## 🛠️ **Your Current Setup**

### ✅ **Fixed Configuration** (Safe for Testing)

**Development (application.properties)**:
```properties
# TEST MODE - Safe for development
razorpay.key.id=rzp_test_YOUR_TEST_KEY_ID
razorpay.key.secret=YOUR_TEST_KEY_SECRET
```

**Production (application-prod.properties)**:
```properties
# LIVE MODE - Only for production deployment
razorpay.key.id=${RAZORPAY_LIVE_KEY_ID}
razorpay.key.secret=${RAZORPAY_LIVE_KEY_SECRET}
```

---

## 🧪 **Test Mode Features**

### **Test Card Numbers** (Use these for testing)
```
Visa: 4111 1111 1111 1111
MasterCard: 5555 5555 5555 4444
Rupay: 6076 6200 0000 0008

CVV: Any 3 digits (e.g., 123)
Expiry: Any future date (e.g., 12/25)
Name: Any name
```

### **Test UPI IDs**
```
success@razorpay: Success payment
failure@razorpay: Failed payment
```

### **Test Bank Accounts**
- No real money is deducted
- Transactions appear in test dashboard only
- Can simulate success/failure scenarios

---

## 🔍 **How to Identify Current Mode**

### **Application Logs** (Added in your service)
```
🧪 RAZORPAY TEST MODE ACTIVE - No real money will be charged!
💰 RAZORPAY LIVE MODE ACTIVE - REAL MONEY WILL BE CHARGED!
```

### **Razorpay Dashboard**
- Test Dashboard: `https://dashboard.razorpay.com/app/dashboard`
- Live Dashboard: Separate section in the same portal

### **Payment IDs**
- Test: `pay_testxxxxxxxxxx`
- Live: `pay_livexxxxxxxxxx`

---

## 📋 **Getting Your Test Keys**

1. **Login to Razorpay Dashboard**
2. **Navigate to Settings → API Keys**
3. **Generate Test Keys**:
   - Download test key ID and secret
   - Use in your `application.properties`

4. **Example Test Keys Structure**:
   ```properties
   razorpay.key.id=rzp_test_1DP5mmOlF5G5ag
   razorpay.key.secret=abcd1234efgh5678ijkl9012mnop3456
   ```

---

## 🚀 **Running in Test Mode**

### **Start Your Application**
```bash
# Default profile (test mode)
mvn spring-boot:run

# Explicitly specify test/dev profile
mvn spring-boot:run -Dspring.profiles.active=dev
```

### **Verify Test Mode**
Check logs for:
```
🧪 RAZORPAY TEST MODE ACTIVE - No real money will be charged!
```

---

## 💳 **Testing Payment Flows**

### **Successful Payment Test**
1. Use test card: `4111 1111 1111 1111`
2. CVV: `123`, Expiry: `12/25`
3. Complete payment
4. Verify booking creation

### **Failed Payment Test**
1. Use test card: `4000 0000 0000 0002`
2. This will simulate a declined card
3. Verify error handling

### **UPI Payment Test**
1. Use UPI ID: `success@razorpay`
2. Verify UPI flow

---

## 🔒 **Security Best Practices**

### **Environment Variables** (Production)
```bash
# Set in your server environment
export RAZORPAY_LIVE_KEY_ID="rzp_live_your_actual_key"
export RAZORPAY_LIVE_KEY_SECRET="your_actual_secret"
```

### **Never Commit Live Keys**
```bash
# Add to .gitignore
*.properties
!application.properties.example
```

### **Local Development**
- Always use test keys in local development
- Create `application-local.properties` for personal test keys
- Never share test keys in public repositories

---

## 🐛 **Troubleshooting**

### **Problem**: "Invalid API Key"
**Solution**: 
- Check key format (`rzp_test_` vs `rzp_live_`)
- Verify key copied correctly (no extra spaces)
- Ensure secret matches the key ID

### **Problem**: "Real money getting charged"
**Solution**:
- ❌ **STOP IMMEDIATELY**
- Check if using `rzp_live_` keys
- Switch to `rzp_test_` keys
- Contact Razorpay support for refund if needed

### **Problem**: "Payment not working in test"
**Solution**:
- Use only test card numbers
- Check network connectivity
- Verify test keys are active in dashboard

---

## 📊 **Monitoring & Logs**

### **Enhanced Logging** (Added to your service)
```java
// Your service now logs:
🧪 RAZORPAY TEST MODE ACTIVE - No real money will be charged!
Using test key: rzp_test_1DP5***

💰 RAZORPAY LIVE MODE ACTIVE - REAL MONEY WILL BE CHARGED!
Using live key: rzp_live_S4r9***
```

### **Test vs Live Verification**
```java
// Check in application startup logs
private void logPaymentMode() {
    if (razorpayKeyId.startsWith("rzp_test_")) {
        log.info("✅ Safe: Running in TEST mode");
    } else {
        log.warn("⚠️ CAUTION: Running in LIVE mode");
    }
}
```

---

## 🎯 **Quick Action Items**

### **Immediate (Development)**
1. ✅ Replace live keys with test keys in `application.properties`
2. ✅ Get test keys from Razorpay dashboard
3. ✅ Test payment flow with test cards
4. ✅ Verify logs show "TEST MODE ACTIVE"

### **Before Production**
1. Set up environment variables for live keys
2. Test in staging with test mode
3. Switch to live mode only in production
4. Monitor first few live transactions

### **Regular Maintenance**
1. Rotate API keys every 6 months
2. Monitor for unauthorized transactions
3. Keep test and live environments separate
4. Regular backup of payment data

---

## 📞 **Emergency Actions**

### **If Real Money Was Charged**
1. **Immediately disable live keys** in Razorpay dashboard
2. **Contact Razorpay support**: support@razorpay.com
3. **Document transactions** for refund requests
4. **Switch to test keys** for development
5. **Review all recent transactions**

### **Prevention**
- Always start with test keys
- Use environment-specific configurations
- Never test with live keys
- Regular code reviews for key usage

---

*Remember: Test mode is your friend during development. Live mode should only be used when you're 100% ready for production with real customers and real money transactions.*