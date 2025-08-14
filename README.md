## Aiverse Project Setup Instructions

Follow these steps to safely set up the project after cloning:

---

### 1️⃣ Clone the repository
```bash
git clone https://github.com/Chinmaynawghare/Aiverse.git
cd Aiverse
2️⃣ Create local configuration
2.1 Copy example files

Copy the example files to real config files locally only (do NOT commit your real keys):

cp local.properties.example local.properties

Then open local.properties and fill in your real keys:
OPENAI_API_KEY=sk-your-openai-key
GEMINI_API_KEY=your-gemini-key
RELEASE_STORE_FILE=/absolute/path/to/release.keystore
RELEASE_STORE_PASSWORD=your_keystore_password
RELEASE_KEY_ALIAS=release
RELEASE_KEY_PASSWORD=your_key_password
2.2 Firebase configuration

Download google-services.json from Firebase Console → Project Settings → Your Android App.

Place it inside app/ directory.

This file is ignored by Git; everyone adds their own copy locally.

If you have multiple product flavors:

app/src/dev/google-services.json
app/src/prod/google-services.json

3️⃣ Build & run

All API keys are injected for debug builds only via BuildConfig.

Never include real keys in release builds.

Use Gradle to build and run:

./gradlew clean build

4️⃣ Security Notes

Do not commit local.properties or google-services.json.

Keep signing keys out of Git; paths and passwords are in local.properties only.

For release builds, consider using a backend proxy for API keys (optional).

5️⃣ Optional Extras

Add a pre-commit hook to prevent committing secrets.

Use GitHub Actions secrets if building in CI/CD.

Use Firebase Cloud Functions as a proxy for OpenAI/Gemini APIs.
