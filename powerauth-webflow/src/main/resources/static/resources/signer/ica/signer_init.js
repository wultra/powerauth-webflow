// Include this file into HTML head, the script contains configuration and functions used by the signer
// and the Control Object is initialized. The signer component is not initialized yet, it is pending choice of
// approval by certificate and subsequent call of method loadICASigner().

// Init control object for signer with configuration received from Web Flow backend
const extensionsSettings = new Array({
    name: extensionOwner,
    extensionIDChrome: extensionIDChrome,
    extensionIDEdge: extensionIDOpera,
    extensionIDOpera: extensionIDEdge,
    extensionIDFirefox: extensionIDFirefox,
    extensionInstallURLFirefox: extensionInstallURLFirefox,
    isDefault: true // The isDefault parameter overrides parsing of extensionOwner from URL
});
const configURLs = new Array(
    {library: "ICAClientSign", url: icaConfigurationUrl}
);
ControlObj.init(extensionsSettings, configURLs);

// Logging level: 0 - turned off, 1 - error, 2 - warning, 3 - info, 4 - log/debug
ICAPKIService.loggingLevel = icaLogLevel;

let idTestInitExtension;
let idTestInitHost;

// Supported OS
const supportedOs = [
    {name: "Windows 10"},
    {name: "Windows 8.1"},
    {name: "Windows 8"},
    {name: "Windows 7"},
    {name: "macOS 10.12"},
    {name: "macOS 10.13"},
    {name: "macOS 10.14"},
    {name: "macOS 10.15"},
    {name: "macOS 11"},
    {name: "macOS 12"}
];

// Supported browsers
const supportedBrowser = [
    {name: "Firefox", minimalVersion: "104"},
    {name: "Chrome", minimalVersion: "104"},
    {name: "Edge", minimalVersion: "104"},
    {name: "Opera", minimalVersion: "90"},
    {name: "Safari", minimalVersion: "14"}
];

// The method should be called after HTML is rendered
function loadICASigner(cbSuccess, cbError) {
    let counterEx = 0;
    // Max attempt count for test init extension
    const maxCountEx = 4;

    let counterHost = 0;
    // Max attempt count for test init host
    let maxCountHost = 15;
    // Max test init host when host is being updated
    const maxCountHostUpdate = 30;
    // Test init ICAPKIServiceHost
    const testInitHost = function () {
        // Increase number of attempts when host is being updated
        if (ControlObj.updateStarted) {
            maxCountHost = maxCountHostUpdate;
        }
        counterHost++;
        // If the host init finishes
        if (ControlObj.finishInitHost) {
            clearInterval(idTestInitHost);
            if (ControlObj.initHost) {
                // Host is installed
                console.log("ICAPKIService is initialized");
                cbSuccess();
            }
        }
        // If maxCountHost exceeds, end the check
        if (counterHost > maxCountHost) {
            clearInterval(idTestInitHost);
            // Host is not installed
            cbError("signer.error.init.host.failed");
        }
    };
    // Test init ICAPKIServiceExtension
    const testInitExtension = function () {
        counterEx++;
        if (counterEx > 1) {
            // Show error that communication with plug-in failed early to the user
            cbError("signer.error.init.extension.failed");
        }
        // If extension init finishes
        if (ControlObj.finishInitExtension) {
            // Extension initialization is finished
            clearInterval(idTestInitExtension);
            if (ControlObj.initExtension) {
                // Extension is now installed, control host state every second for maxCountHost
                idTestInitHost = setInterval(testInitHost, 1000);
            } else {
                // Extension is not installed
                cbError("signer.error.init.extension.failed");
            }
        }
        // If maxCountEx exceeds, end the check
        if (counterEx > maxCountEx) {
            clearInterval(idTestInitExtension);
        }
    };
    // If cookies, OS and browser is supported try to initialize ICAPKIService
    if (ControlObj.isSupportedCookie() && ControlObj.isSupportedOs(supportedOs) && ControlObj.isSupportedBrowser(supportedBrowser)) {
        ControlObj.initICAPKI();
        // Control Extension state every second for maxCountEx
        idTestInitExtension = setInterval(testInitExtension, 1000);
    } else {
        cbError("signer.error.init.notSupported");
    }
}

// Load keystore and use the first certificate which is found for signing
// TODO - support for multiple certificates with certificate choice before signing
function loadKeyStoreAndSignMessage(content, cbSuccess, cbError) {
    try {
        if (!IsICAPKIServiceRunning()) {
            ThrowICAPKIServiceNotRunning();
        }
        const cb = function cb(count) {
            if (count === undefined || count < 1) {
                cbError("signer.error.certificate.notFound");
                return;
            }
            // first certificate and use it for signing
            loadCertificateAndSignMessage(content, cbSuccess, cbError);
        };
        // Flags for certificates which can be used for signing
        const flags = ICAClientSign.CERTLOAD_SIGNING_FLAG | ICAClientSign.CERTLOAD_QUALIFIED_FLAG;
        // Load certificates from key store, cardstore is disabled
        ICAClientSign.certificateLoadUserKeyStore(cb, false, flags);
    } catch (ex) {
        ProcException("certificateLoadUserKeyStore error", ex);
        cbError("signer.error.certificate.notFound");
    }
}

// Load first certificate found in the keystore and set it for signing
function loadCertificateAndSignMessage(content, cbSuccess, cbError) {
    const cbCert = function cb(certIndex, pem) {
        if (pem == null) {
            cbError("signer.error.certificate.notFound");
            return;
        }
        const cbCertSet = function cb() {
            signMessage(content, cbSuccess, cbError);
        };
        ICAClientSign.signerSetCertificate(cbCertSet, pem);
    }
    ICAClientSign.certificateEnumerateStore(cbCert, 0);
}

// Sign content using CMS in detached mode, content should be Base-64 encoded
function signMessage(content, cbSuccess, cbError) {
    try {
        if (!IsICAPKIServiceRunning()) {
            ThrowICAPKIServiceNotRunning();
        }
        const cb = function cb(msg) {
            cbSuccess(msg);
        };
        ICAClientSign.signCmsDetached(cb, content);
    } catch (ex) {
        ProcException("signCmsDetached error", ex);
        cbError("signer.result.failed");
    }
}

// Get error number
function GetExceptionNumber(ex) {
    let nNumber;
    if (ex == null || typeof ex == "undefined" || ex.number == null || typeof ex.number == "undefined") {
        nNumber = 0x80000000;
    } else {
        nNumber = (ex.number & 0x7fffffff) + 0x80000000;
    }
    return "0x" + nNumber.toString(16).toUpperCase();
}

// Get error description
function GetExceptionDescription(ex) {
    if (ex == null || typeof ex == "undefined") return null;
    if (ex.description == null || typeof ex.description == "undefined" || !ex.description.length)
        return "error description missing";
    return ex.description;
}

// Get error message
function GetExceptionMessage(ex) {
    if (ex == null || typeof ex == "undefined") return null;
    if (ex.message == null || typeof ex.message == "undefined" || !ex.message.length)
        return "error message missing";
    return ex.message;
}

// Exception thrown in case ICAPKIService is not in ready state
function ICAPKIServiceNotReadyException(err_code, err_msg) {
    this.name = "ICAPKIServiceNotReadyException";
    this.number = err_code;
    this.message = err_msg;
}

// Exception thrown in case ICAPKIService is not running
function ICAPKIServiceNotRunningException() {
    this.name = "ICAPKIServiceNotRunningException";
    this.number = -1;
    this.message = "ICAPKIService is not running";
}

// Error handling callback method triggered by ICA signer
function ProcException(msg, ex) {
    let err_msg = ex.name + "\n";
    err_msg += "Message: " + GetExceptionMessage(ex) + "\n";
    err_msg += "Description: " + GetExceptionDescription(ex) + "\n";
    err_msg += "Error code: " + GetExceptionNumber(ex) + "\n";
    console.log(err_msg);
}

function IsICAPKIServiceRunning() {
    return CheckICAPKIServiceState();
}

// Check ICAPKIService state
function CheckICAPKIServiceState() {
    let running = false;
    if (ControlObj.isObjectLoaded()) {
        running = ICAPKIService.IsReady();
        if (!running) {
            let state = -1;
            try {
                state = ICAClientSign.GetObjectState();
            } catch (ex) {
            }
            const state_msg = state + ":" + GetObjectStateMsg(state);
            const err_code = -1;
            let err_msg = "";
            err_msg += ", " + state_msg;
            ThrowICAPKIServiceNotReady(err_code, err_msg);
        }
    }
    return running;
}

// GetObjectStateMsg - convert object state to message
function GetObjectStateMsg(state) {
    let message;
    switch (state) {
        case 0:
            message = "idle";
        break;
        case 1:
            message = "initialization";
        break;
        case 2:
            message = "extension installed";
        break;
        case 3:
            message = "host installed";
        break;
        case 4:
            message = "native dll loaded";
        break;
        case 5:
            message = "ready";
        break;
        default:
            message = "unknown";
    }
    return message;
}

// Throw exception when ICA PKI service is not ready
function ThrowICAPKIServiceNotReady(code, message) {
    throw new ICAPKIServiceNotReadyException(code, message);
}

// Throw exception when ICA PKI service is not running
function ThrowICAPKIServiceNotRunning() {
    throw new ICAPKIServiceNotRunningException();
}
