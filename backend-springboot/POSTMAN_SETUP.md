# Postman Setup Instructions

## Fix Content-Type Error

If you're getting this error:
```
Content-Type 'text/plain;charset=UTF-8' is not supported
```

### Solution 1: Set Content-Type Header in Postman

1. Open your request in Postman
2. Go to **Headers** tab
3. Add or modify this header:
   - **Key:** `Content-Type`
   - **Value:** `application/json`
4. Make sure it's enabled (checkbox is checked)

### Solution 2: Use Body Tab Correctly

1. Go to **Body** tab
2. Select **raw** radio button
3. In the dropdown next to "raw", select **JSON** (not Text)
4. This automatically sets Content-Type to `application/json`

### Solution 3: Remove Content-Type if Using Body JSON

If you select "raw" and "JSON" in Body tab, you can remove the Content-Type header - Postman will add it automatically.

---

## Step-by-Step Postman Setup

### For Registration Endpoint:

1. **Method:** POST
2. **URL:** `http://localhost:8081/api/auth/register`
3. **Headers Tab:**
   - `Content-Type`: `application/json`
4. **Body Tab:**
   - Select: **raw**
   - Dropdown: **JSON**
   - Paste this:
   ```json
   {
     "ntid": "testuser",
     "email": "test@example.com",
     "account": "Test Account",
     "accountId": 12345
   }
   ```

### For Login Endpoint:

1. **Method:** POST
2. **URL:** `http://localhost:8081/api/auth/login`
3. **Headers Tab:**
   - `Content-Type`: `application/json`
4. **Body Tab:**
   - Select: **raw**
   - Dropdown: **JSON**
   - Paste this:
   ```json
   {
     "ntid": "mukund",
     "password": "18July2022"
   }
   ```

---

## Common Postman Mistakes

❌ **Wrong:** Body tab → raw → Text (sends as text/plain)
✅ **Right:** Body tab → raw → JSON (sends as application/json)

❌ **Wrong:** Body tab → form-data or x-www-form-urlencoded
✅ **Right:** Body tab → raw → JSON

❌ **Wrong:** No Content-Type header and Body is Text
✅ **Right:** Either set Content-Type header OR use Body → raw → JSON

---

## Quick Test

After setting up correctly, you should see in response:
- Status: `201 Created` for registration
- Status: `200 OK` for login
- JSON response with user data

If you still get Content-Type error, check:
1. Headers tab has `Content-Type: application/json`
2. Body tab is set to `raw` and `JSON` (not Text)
