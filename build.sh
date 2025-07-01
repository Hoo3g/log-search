#!/bin/bash

# =============================================================================
# Build Script for KHA Storage Service
# Can be used locally or by CI/CD (Woodpecker)
# =============================================================================

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Helper functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# =============================================================================
# STEP 1: Load Environment Variables from app.env (Single Source of Truth)
# =============================================================================
log_info "Loading environment variables from app.env..."

if [ ! -f "app.env" ]; then
    log_error "app.env file not found!"
    exit 1
fi

# Load environment variables with proper expansion
set -a  # Mark variables for export
source app.env
set +a  # Turn off automatic export

# Alternative method for systems where 'source' doesn't work with .env files
if [ -z "$APP_NAME" ]; then
    while IFS='=' read -r key value; do
        # Skip comments and empty lines
        [[ $key =~ ^#.*$ ]] && continue
        [[ -z $key ]] && continue
        # Remove quotes if present
        value=$(echo "$value" | sed 's/^["'\'']//' | sed 's/["'\'']$//')
        export "$key"="$value"
    done < app.env
fi

# Validate required variables
if [ -z "$APP_NAME" ] || [ -z "$APP_VERSION" ] || [ -z "$APP_JAR" ]; then
    log_error "Required environment variables not set!"
    log_error "APP_NAME: $APP_NAME"
    log_error "APP_VERSION: $APP_VERSION"
    log_error "APP_JAR: $APP_JAR"
    exit 1
fi

log_success "Environment loaded successfully"
log_info "APP_NAME: $APP_NAME"
log_info "APP_VERSION: $APP_VERSION"
log_info "APP_JAR: $APP_JAR"

# =============================================================================
# STEP 2: Gradle Build
# =============================================================================

log_info "Starting Gradle build..."

# Clean previous build
log_info "Cleaning previous build artifacts..."
rm -rf build/app

# Build application and extract dependencies
log_info "Building application and extracting dependencies..."

# Make gradlew executable (for Unix systems)
if [ -f "./gradlew" ]; then
    chmod +x ./gradlew
    GRADLE_CMD="./gradlew"
elif [ -f "./gradlew.bat" ]; then
    GRADLE_CMD="./gradlew.bat"
elif command -v gradle &> /dev/null; then
    GRADLE_CMD="gradle"
else
    log_error "No Gradle wrapper or Gradle installation found!"
    exit 1
fi

# Check if running in CI environment
if [ -n "$CI" ] || [ -n "$CI_WORKSPACE" ] || [ -d "/home/gradle/.gradle" ]; then
    log_info "CI environment detected"

    # Woodpecker uses gradle:jdk21 image which has gradle user with home at /home/gradle
    if [ -d "/home/gradle/.gradle" ]; then
        export GRADLE_USER_HOME="/home/gradle/.gradle"
        log_info "Using Gradle cache at: $GRADLE_USER_HOME"
    else
        export GRADLE_USER_HOME="/root/.gradle"
        log_info "Using Gradle cache at: $GRADLE_USER_HOME"
    fi

    # Run gradle with CI optimizations
    log_info "Running Gradle build with caching enabled..."
    $GRADLE_CMD build extractDependencies -x test --build-cache --no-daemon --info
else
    log_info "Local environment detected"
    log_info "Running Gradle build..."
    $GRADLE_CMD build extractDependencies -x test --info
fi

# Verify JAR file was created
if [ ! -f "build/libs/$APP_JAR" ]; then
    log_error "JAR file not found: build/libs/$APP_JAR"
    exit 1
fi

log_success "Gradle build completed successfully"

# =============================================================================
# STEP 3: Create Application Structure
# =============================================================================
log_info "Creating application structure..."

# Create directory structure
mkdir -p build/app/libs
mkdir -p build/app/logs
mkdir -p build/app/config

# Copy configuration files
log_info "Copying configuration files..."
cp -r config/* build/app/config/

# Copy main JAR
log_info "Copying main JAR: $APP_JAR"
cp build/libs/$APP_JAR build/app/

# Copy dependencies
log_info "Copying dependencies..."
cp -r build/dependencies/* build/app/libs/

# List build artifacts
log_info "Build artifacts created:"
ls -la build/app/
log_info "Dependencies count: $(ls -1 build/app/libs/ | wc -l)"

# =============================================================================
# STEP 4: Deploy to Mount Point (if BASE_DEPLOY_DIR is set)
# =============================================================================
if [ -n "$BASE_DEPLOY_DIR" ]; then
    log_info "Deploying to mount point: $BASE_DEPLOY_DIR/$APP_NAME"

    # Create target directory structure
    mkdir -p $BASE_DEPLOY_DIR/$APP_NAME/app/logs

    # Set proper permissions for logs
    chmod -R 777 $BASE_DEPLOY_DIR/$APP_NAME/app/logs

    # Remove old application files and sync new ones
    log_info "Removing old application files..."
    rm -rf $BASE_DEPLOY_DIR/$APP_NAME/app/*

    log_info "Syncing application files..."
    cp -r build/app/* $BASE_DEPLOY_DIR/$APP_NAME/app/

    log_success "Application deployed to $BASE_DEPLOY_DIR/$APP_NAME/app/"

    # Copy deployment configuration files
    log_info "Copying deployment configuration files..."

    # Copy docker-compose.yaml to release directory
    cp docker-compose.yaml $BASE_DEPLOY_DIR/$APP_NAME/
    log_info "docker-compose.yaml copied to $BASE_DEPLOY_DIR/$APP_NAME/"

    # Copy app.env as .env for docker-compose
    cp app.env $BASE_DEPLOY_DIR/$APP_NAME/.env
    log_info "app.env copied as .env to $BASE_DEPLOY_DIR/$APP_NAME/"

else
    log_warning "BASE_DEPLOY_DIR not set - skipping deployment"
fi

# =============================================================================
# STEP 5: Build Summary
# =============================================================================
log_success "Build completed successfully!"
echo
echo "=================================================="
echo "Build Summary:"
echo "=================================================="
echo "Application: $APP_NAME"
echo "Version: $APP_VERSION"
echo "JAR File: $APP_JAR"
echo "Build Directory: $(pwd)/build/app/"
if [ -n "$BASE_DEPLOY_DIR" ]; then
    echo "Deploy Directory: $BASE_DEPLOY_DIR/$APP_NAME/app/"
    echo "Docker Compose: $BASE_DEPLOY_DIR/$APP_NAME/docker-compose.yaml"
    echo "Environment: $BASE_DEPLOY_DIR/$APP_NAME/.env"
fi
echo "=================================================="
echo