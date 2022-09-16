/*!
 * main common script, contains generic functions.
 */

function renderPage() {
    if (devMode) {
        console.log("This is dev mode")
    }
    initKeycloak()
    testButtonOnClink()
}