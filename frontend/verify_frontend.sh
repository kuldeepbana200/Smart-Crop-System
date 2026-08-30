#!/bin/bash

echo "Verifying Smart Crop System Frontend Setup"
echo "========================================"

# Check if required files exist
echo "1. Checking file structure..."
FILES_TO_CHECK=(
  "src/i18n.ts"
  "src/components/common/LanguageSelector.tsx"
  "src/components/common/__tests__/LanguageSelector.test.tsx"
  "src/App.tsx"
  "src/main.tsx"
  "tailwind.config.js"
  "postcss.config.js"
  "index.html"
  "package.json"
)

all_exist=true
for file in "${FILES_TO_CHECK[@]}"; do
  if [ -f "$file" ]; then
    echo "   ✓ $file exists"
  else
    echo "   ✗ $file missing"
    all_exist=false
  fi
done

if [ "$all_exist" = true ]; then
  echo "   All required files present"
else
  echo "   Some files missing"
fi

# Check TypeScript compilation
echo ""
echo "2. Checking TypeScript compilation..."
if npx tsc --noEmit 2>/dev/null; then
  echo "   ✓ TypeScript compiles without errors"
else
  echo "   ✗ TypeScript compilation failed"
fi

# Check Tailwind configuration
echo ""
echo "3. Checking Tailwind configuration..."
if [ -f "tailwind.config.js" ] && [ -f "postcss.config.js" ]; then
  echo "   ✓ Tailwind and PostCSS configs exist"
else
  echo "   ✗ Missing Tailwind or PostCSS config"
fi

# Check i18n setup
echo ""
echo "4. Checking i18n setup..."
if grep -q "initReactI18next" src/i18n.ts && grep -q "resources" src/i18n.ts; then
  echo "   ✓ i18n configuration looks correct"
else
  echo "   ✗ i18n configuration may be incomplete"
fi

# Check LanguageSelector component
echo ""
echo "5. Checking LanguageSelector component..."
if grep -q "useTranslation" src/components/common/LanguageSelector.tsx && \
   grep -q "i18n.changeLanguage" src/components/common/LanguageSelector.tsx; then
  echo "   ✓ LanguageSelector component looks correct"
else
  echo "   ✗ LanguageSelector component may be incomplete"
fi

# Check App component
echo ""
echo "6. Checking App component setup..."
if grep -q "BrowserRouter" src/App.tsx && \
   grep -q "LanguageSelector" src/App.tsx; then
  echo "   ✓ App component includes routing and language selector"
else
  echo "   ✗ App component may be missing key elements"
fi

echo ""
echo "Verification complete!"
echo "To start the development server, run: npm run dev"
echo "Then visit http://localhost:5173 to see the frontend"
