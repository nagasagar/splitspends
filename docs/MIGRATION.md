# Step-by-Step Guide: Reorganizing SplitSpends to Monorepo Structure

This guide will help you safely move your `splitspends-backend` content into a proper monorepo structure.

## Current Structure
```
c:\DasaWorkSpace\projects\splitspends-project\
├── splitspends-backend\     # ← Current backend location
│   ├── src\
│   ├── pom.xml
│   ├── target\
│   └── ...
└── splitspends_ui\          # ← Current frontend location
    ├── lib\
    ├── pubspec.yaml
    └── ...
```

## Target Structure
```
c:\DasaWorkSpace\projects\splitspends-project\
├── README.md                # ← Main project docs
├── .gitignore              # ← Monorepo gitignore
├── LICENSE
├── backend\                # ← Moved from splitspends-backend\
│   ├── src\
│   ├── pom.xml
│   ├── target\
│   └── ...
├── frontend\               # ← Moved from splitspends_ui\
│   ├── lib\
│   ├── pubspec.yaml
│   └── ...
└── docs\                   # ← New documentation folder
    ├── API.md
    └── ...
```

## Step 1: Navigate to Project Root

Open Command Prompt or PowerShell and navigate to your project root:

```cmd
cd "c:\DasaWorkSpace\projects\splitspends-project"
```

## Step 2: Create New Directory Structure

Create the main directories for your monorepo:

```cmd
mkdir backend
mkdir frontend
mkdir docs
mkdir .github
mkdir .github\workflows
```

## Step 3: Move Backend Files

Move all content from `splitspends-backend` to `backend`:

```cmd
# Move all files and folders from splitspends-backend to backend
xcopy "splitspends-backend\*" "backend\" /E /H /Y

# Verify the move was successful
dir backend
```

Alternative using robocopy (more reliable):
```cmd
robocopy "splitspends-backend" "backend" /E /MOVE
```

## Step 4: Move Frontend Files

Move all content from `splitspends_ui` to `frontend`:

```cmd
# Move all files and folders from splitspends_ui to frontend
xcopy "splitspends_ui\*" "frontend\" /E /H /Y

# Verify the move was successful
dir frontend
```

Alternative using robocopy:
```cmd
robocopy "splitspends_ui" "frontend" /E /MOVE
```

## Step 5: Create Root-Level Files

Copy important files to the root level:

```cmd
# Copy main README to root (we'll update it to be the main project README)
copy "backend\README.md" "README.md"

# Copy the monorepo gitignore we created
copy "backend\.gitignore" ".gitignore"
```

## Step 6: Create Documentation Files

Move documentation files to the docs folder:

```cmd
# Move documentation files to docs folder
move "backend\API_DOCUMENTATION.md" "docs\API.md"
move "backend\ENTITY_ARCHITECTURE.md" "docs\ARCHITECTURE.md"
move "backend\REPOSITORY_LAYER.md" "docs\DATABASE.md"
copy "backend\GITHUB_SETUP.md" "docs\SETUP.md"
```

## Step 7: Clean Up Old Directories

After verifying everything moved correctly, remove the old directories:

```cmd
# Only do this AFTER verifying everything moved correctly!
rmdir /s "splitspends-backend"
rmdir /s "splitspends_ui"
```

## Step 8: Update Path References

Update any path references in your files:

### Update backend\pom.xml (if needed)
- Check for any absolute paths that might need updating

### Update VS Code workspace settings
- Update `.vscode\settings.json` paths if they exist

## Step 9: Initialize Git Repository

Now initialize Git in the project root:

```cmd
# Initialize Git
git init

# Add all files
git add .

# Create initial commit
git commit -m "Initial commit: Reorganized to monorepo structure"
```

## Step 10: Verify Structure

Check that your new structure looks correct:

```cmd
# List the root directory
dir

# Check backend structure
dir backend

# Check frontend structure  
dir frontend

# Check docs structure
dir docs
```

Your final structure should look like:
```
splitspends-project\
├── .git\
├── .gitignore
├── README.md
├── backend\
│   ├── .mvn\
│   ├── src\
│   ├── target\
│   ├── pom.xml
│   ├── mvnw
│   ├── mvnw.cmd
│   └── HELP.md
├── frontend\
│   ├── android\
│   ├── ios\
│   ├── lib\
│   ├── web\
│   ├── pubspec.yaml
│   └── pubspec.lock
└── docs\
    ├── API.md
    ├── ARCHITECTURE.md
    ├── DATABASE.md
    └── SETUP.md
```

## Step 11: Test Backend

Verify the backend still works:

```cmd
cd backend
mvn clean compile
cd ..
```

## Step 12: Test Frontend

Verify the frontend still works:

```cmd
cd frontend
flutter pub get
flutter analyze
cd ..
```

## Troubleshooting

### If files didn't move correctly:
1. Check if any files are still in the old directories
2. Use `xcopy` with `/E /H /Y` flags to copy everything
3. Manually copy any missed files

### If Git doesn't recognize changes:
1. Make sure you're in the right directory
2. Use `git status` to see what Git detects
3. Use `git add .` to add all files

### If VS Code workspace is broken:
1. Close VS Code
2. Open the new project root directory
3. Reconfigure workspace settings if needed

## Next Steps

After reorganizing:
1. ✅ Verify both backend and frontend work
2. ✅ Update README.md for the main project
3. ✅ Create GitHub repository
4. ✅ Push to GitHub
5. ✅ Set up CI/CD workflows

## Commands Summary

Here's the complete command sequence:

```cmd
cd "c:\DasaWorkSpace\projects\splitspends-project"
mkdir backend frontend docs .github .github\workflows
robocopy "splitspends-backend" "backend" /E /MOVE
robocopy "splitspends_ui" "frontend" /E /MOVE
move "backend\API_DOCUMENTATION.md" "docs\API.md"
move "backend\ENTITY_ARCHITECTURE.md" "docs\ARCHITECTURE.md"
move "backend\REPOSITORY_LAYER.md" "docs\DATABASE.md"
git init
git add .
git commit -m "Initial commit: Reorganized to monorepo structure"
```

That's it! Your project is now organized as a proper monorepo. 🎉
