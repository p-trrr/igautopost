#!/bin/bash
# Demo script for Instagram Auto Post application
# This script demonstrates how to use the application with mock data

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}Instagram Auto Post - Demo Script${NC}"
echo "======================================"
echo ""

# Check if JAR exists
if [ ! -f "target/igautopost-1.0.0-SNAPSHOT.jar" ]; then
    echo -e "${RED}Error: JAR file not found. Please run 'mvn clean package' first.${NC}"
    exit 1
fi

# Display usage
echo -e "${YELLOW}Available Commands:${NC}"
echo ""
echo "1. Post from URL:"
echo "   java -jar target/igautopost-1.0.0-SNAPSHOT.jar post-url <image-url> <context>"
echo ""
echo "2. Post from local file:"
echo "   java -jar target/igautopost-1.0.0-SNAPSHOT.jar post-local <image-path> <context>"
echo ""
echo "3. Start scheduler:"
echo "   java -jar target/igautopost-1.0.0-SNAPSHOT.jar schedule"
echo ""
echo -e "${YELLOW}Environment Variables Required:${NC}"
echo "  - OPENAI_API_KEY"
echo "  - INSTAGRAM_ACCESS_TOKEN"
echo "  - INSTAGRAM_BUSINESS_ACCOUNT_ID"
echo ""
echo -e "${YELLOW}Optional (for local image uploads):${NC}"
echo "  - AWS_ACCESS_KEY"
echo "  - AWS_SECRET_KEY"
echo "  - AWS_REGION"
echo "  - AWS_S3_BUCKET"
echo ""
echo -e "${GREEN}To set environment variables, run:${NC}"
echo "  export \$(cat .env | xargs)"
echo ""
echo -e "${YELLOW}Note:${NC} This is a demo. Ensure you have valid credentials before running."
echo ""
