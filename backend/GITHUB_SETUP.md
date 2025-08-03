# GitHub Setup Instructions for SplitSpends Monorepo

Follow these steps to set up your SplitSpends project on GitHub as a monorepo.

## Prerequisites

1. **Install Git** (if not already installed)
   ```bash
   # Check if Git is installed
   git --version
   ```

2. **Create GitHub Account** (if you don't have one)
   - Go to [github.com](https://github.com) and sign up

## Step 1: Create Repository Structure

1. **Navigate to your project root:**
   ```bash
   cd "c:\DasaWorkSpace\projects\splitspends-project"
   ```

2. **Create the monorepo structure:**
   ```bash
   # Create main directories
   mkdir docs
   mkdir backend
   mkdir frontend

   # Move existing backend files
   # (Your current splitspends-backend content goes into backend/)
   
   # Create frontend directory structure
   cd frontend
   # (Your Flutter project will go here)
   ```

## Step 2: Initialize Git Repository

1. **Initialize Git in the project root:**
   ```bash
   cd "c:\DasaWorkSpace\projects\splitspends-project"
   git init
   ```

2. **Create .gitignore file** (copy the content provided in the main setup)

3. **Add all files to Git:**
   ```bash
   git add .
   git commit -m "Initial commit: SplitSpends monorepo setup"
   ```

## Step 3: Create GitHub Repository

1. **Go to GitHub and create a new repository:**
   - Repository name: `splitspends`
   - Description: `Smart expense sharing application with Spring Boot backend and Flutter frontend`
   - Make it Public (or Private if you prefer)
   - Don't initialize with README (we already have one)

2. **Copy the repository URL** (it will look like):
   ```
   https://github.com/yourusername/splitspends.git
   ```

## Step 4: Connect Local Repository to GitHub

1. **Add GitHub as remote origin:**
   ```bash
   git remote add origin https://github.com/yourusername/splitspends.git
   ```

2. **Push to GitHub:**
   ```bash
   git branch -M main
   git push -u origin main
   ```

## Step 5: Organize Your Repository Structure

Your final repository structure should look like this:

```
splitspends/
├── README.md                    # Main project documentation
├── .gitignore                   # Git ignore rules
├── LICENSE                      # Project license
├── .github/                     # GitHub workflows and templates
│   ├── workflows/
│   │   ├── backend-ci.yml      # Backend CI/CD
│   │   └── frontend-ci.yml     # Frontend CI/CD
│   ├── ISSUE_TEMPLATE/
│   └── pull_request_template.md
├── docs/                        # Project documentation
│   ├── API.md                   # API documentation
│   ├── ARCHITECTURE.md          # Architecture guide
│   ├── SETUP.md                 # Setup instructions
│   ├── DEPLOYMENT.md            # Deployment guide
│   └── images/                  # Screenshots and diagrams
├── backend/                     # Spring Boot backend
│   ├── src/
│   ├── target/
│   ├── pom.xml
│   ├── README.md
│   └── ...
└── frontend/                    # Flutter frontend
    ├── lib/
    ├── android/
    ├── ios/
    ├── pubspec.yaml
    ├── README.md
    └── ...
```

## Step 6: Set Up GitHub Repository Features

### 1. Repository Settings
- Go to Settings → General
- Set default branch to `main`
- Enable "Automatically delete head branches"

### 2. Branch Protection
- Go to Settings → Branches
- Add rule for `main` branch:
  - Require pull request reviews
  - Require status checks to pass
  - Restrict pushes to main

### TODO 

### 3. Issues and Projects
- Enable Issues in Settings
- Create issue templates
- Set up project boards for task management

### 4. GitHub Actions (CI/CD)
- Create `.github/workflows/` directory
- Add CI workflows for backend and frontend
- Set up automated testing and deployment

## Step 7: Create Initial Release

1. **Tag your first release:**
   ```bash
   git tag -a v1.0.0 -m "Initial release of SplitSpends"
   git push origin v1.0.0
   ```

2. **Create GitHub Release:**
   - Go to Releases → Create a new release
   - Tag: v1.0.0
   - Title: "SplitSpends v1.0.0 - Initial Release"
   - Describe the features and functionality

## Step 8: Set Up Repository Description and Topics

1. **Add repository description:**
   ```
   Smart expense sharing application built with Spring Boot and Flutter. Features group management, expense splitting, settlement tracking, and real-time notifications.
   ```

2. **Add topics/tags:**
   ```
   spring-boot, flutter, expense-sharing, java, dart, postgresql, jwt, rest-api, mobile-app, web-app
   ```

## Step 9: Create Documentation

Add these files to your `docs/` directory:

- **SETUP.md** - Development environment setup
- **ARCHITECTURE.md** - System architecture overview  
- **DEPLOYMENT.md** - Deployment instructions
- **CONTRIBUTING.md** - Contribution guidelines
- **API.md** - Complete API documentation

## Step 10: Continuous Integration Setup

Create GitHub Actions workflows:

### Backend CI (.github/workflows/backend-ci.yml)
```yaml
name: Backend CI
on:
  push:
    paths: ['backend/**']
  pull_request:
    paths: ['backend/**']

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run tests
        run: |
          cd backend
          ./mvnw test
```

### Frontend CI (.github/workflows/frontend-ci.yml)
```yaml
name: Frontend CI
on:
  push:
    paths: ['frontend/**']
  pull_request:
    paths: ['frontend/**']

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up Flutter
        uses: subosito/flutter-action@v2
        with:
          flutter-version: '3.x'
      - name: Run tests
        run: |
          cd frontend
          flutter pub get
          flutter test
```

## Tips for Success

1. **Commit regularly** with descriptive messages
2. **Use semantic versioning** for releases
3. **Write good commit messages** following conventional commits
4. **Create pull requests** for all changes
5. **Document everything** - README, API, setup instructions
6. **Add screenshots** to showcase your app
7. **Use GitHub Issues** for bug tracking and feature requests
8. **Set up automated testing** with GitHub Actions

## Next Steps

1. Complete the backend implementation
2. Start Flutter frontend development
3. Set up deployment pipelines
4. Add comprehensive testing
5. Create user documentation
6. Plan for production deployment

## Support

If you encounter any issues during setup:
1. Check GitHub's documentation
2. Review Git basics tutorials
3. Ask for help in GitHub community forums

Happy coding! 🚀
