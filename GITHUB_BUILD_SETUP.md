# GitHub Actions APK Build Setup

## Required Setup Steps

To build your APK on GitHub Actions, you need to add your `google-services.json` as a secret:

### 1. Get your google-services.json content
Run this command in your project directory:
```powershell
Get-Content app\google-services.json | Out-String
```

### 2. Add it as a GitHub Secret
1. Go to your repository: https://github.com/sarang-sketch/founders
2. Click on **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Name: `GOOGLE_SERVICES_JSON`
5. Value: Paste the entire content of your `google-services.json` file
6. Click **Add secret**

### 3. Trigger the Build
After adding the secret, you can:
- Push code to trigger automatic build
- Or go to **Actions** tab → **Build APK** → **Run workflow**

### 4. Download the APK
Once the build completes:
1. Go to the **Actions** tab
2. Click on the completed workflow run
3. Download the **app-debug** artifact

## Files Fixed
- ✅ Created `gradlew` (Linux executable for GitHub Actions)
- ✅ Updated `.github/workflows/build-apk.yml` to handle google-services.json
- ✅ Added proper error handling and stacktrace output

## Troubleshooting
If the build fails, check the Actions logs for detailed error messages with stacktrace.
