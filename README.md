# STAR BROWSER LEGACY 1.0

**POWERED BY STAR**

An initial Android browser project based on the decisions made during project planning.

## Project profile
- Application ID: `com.star.browserlegacy`
- Version: `1.0` (versionCode 1)
- Minimum Android: 4.2.2 (API 17)
- Target SDK: 28
- Language: Java
- Layout: XML
- Rendering engine: Android WebView
- Default search engine: Google
- Theme colors: navy, blue/cyan, silver

## Included in version 1.0
- WebView-based browsing and address/search bar
- Google search for entered search terms
- STAR start page with Google, YouTube, Wikipedia and GitHub shortcuts
- Back and forward navigation
- Basic multi-tab creation, switching and closing
- Bookmarks stored locally
- Browsing history stored locally, with clear-history action
- DownloadManager integration and download list
- Share current page
- Night-mode preference and basic settings screen
- About STAR page

## Build APK with GitHub Actions
1. Create a GitHub repository (or open your existing STAR browser repository).
2. Upload the contents of this folder to the repository root. The `.github/workflows/android.yml` file must remain in that exact folder structure.
3. Push to `main` or `master`, or open **Actions → Build STAR BROWSER LEGACY APK → Run workflow**.
4. When the workflow finishes, open the run and download the artifact named `STAR-BROWSER-LEGACY-1.0-debug`.
5. The APK inside the artifact is `app-debug.apk`.

## Important compatibility notes
- Android 4.2.2 includes an old system WebView. Many modern websites may render incorrectly or refuse to load. This is a platform limitation, not a browser setting.
- This is a first 1.0 foundation, not a fully hardened production browser. Incognito mode, advanced download controls, bookmark editing/folders, full Vivaldi-style tab cards, and a complete settings suite are not implemented yet.
- The original user-provided STAR brand logo and circular app icon were not present as image files in this build workspace. No replacement artwork is claimed to be the approved final asset; add the approved image assets before publishing.
- The APK has not been compiled or device-tested in this environment because Android SDK and Gradle are unavailable here. Use the included GitHub Actions workflow to compile it.
