# 🚀 Push to GitHub - Step by Step

## Quick Commands

### Step 1: Initialize Git (if not done)
```bash
git init
```

### Step 2: Add All Files
```bash
git add .
```

### Step 3: Commit Changes
```bash
git commit -m "Complete Firebase auth setup with email/password and Google Sign-In, notification system implementation"
```

### Step 4: Create GitHub Repository
1. Go to: https://github.com/new
2. Repository name: `mgm-lost-found` (or any name)
3. Make it **Public** or **Private**
4. **DO NOT** initialize with README
5. Click **"Create repository"**

### Step 5: Add Remote and Push
```bash
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/mgm-lost-found.git
git push -u origin main
```

**Replace `YOUR_USERNAME` with your GitHub username!**

## If Repository Already Exists

If you already created the repository, just run:
```bash
git remote add origin https://github.com/YOUR_USERNAME/mgm-lost-found.git
git branch -M main
git push -u origin main
```

## Authentication

If asked for credentials:
- **Username**: Your GitHub username
- **Password**: Use a Personal Access Token (not your password)
  - Go to: https://github.com/settings/tokens
  - Generate new token (classic)
  - Select scopes: `repo`
  - Copy token and use as password

## After Pushing

1. Go to your repository on GitHub
2. Click **"Actions"** tab
3. Click **"Run workflow"** to build APK
4. Download APK from Artifacts!

## Troubleshooting

**"remote origin already exists"**
```bash
git remote remove origin
git remote add origin https://github.com/YOUR_USERNAME/repo-name.git
```

**"Authentication failed"**
- Use Personal Access Token instead of password
- Or use GitHub CLI: `gh auth login`

**"Permission denied"**
- Check repository name is correct
- Verify you have access to the repository

