/// ICAClientSign.js
/// Intended to use with native libraries:
///     - ICAClientSign     v3.1.0.0
///     - ICAClientSignExt  v2.1.0.0

//----------ICACLIENTSIGN OBJECT-------------------------------

var ICAClientSign = {
  libICAClientSign: "ICAClientSign",

  libICAClientSignURL: function () {
    // Use Web Flow for ICA configuration URL setting, automatic configuration is skipped
    return icaConfigurationUrl;

    /*
    function getEnvironmentFromURL() {
      var url = window.location.href;
      var domain;
      //find & remove protocol (http, ftp, etc.) and get domain
      if (url.indexOf("://") > -1) domain = url.split("/")[2];
      else domain = url.split("/")[0];

      //find & remove port number
      domain = domain.split(":")[0];

      var domainEndsWith = function (domain, endsWithStr) {
        return (
          domain.indexOf(endsWithStr, domain.length - endsWithStr.length) !== -1
        );
      };

      if (domain == "s.ica.cz") return "product";
      if (domain == "ca.ica.cz" || domain == "download.ica.cz") return "ca";
      else if (domain == "tests.ica.cz") return "test";
      else if (domain == "s.dev.ica.cz") return "dev";
      else if (domainEndsWith(domain, "proebiz.com")) return "proebiz";
      else if (
        domainEndsWith(domain, "sberbank.cz") ||
        domainEndsWith(domain, "sbcz.cz") ||
        domainEndsWith(domain, "trysbcz.cz")
      )
        return "sberbank";
      else if (domainEndsWith(domain, "cnb.cz")) return "cnb";
      else if (domain.substring(0) == "localhost") return "localhost";
      else return "unknown";
    }

    var environment = getEnvironmentFromURL();

    switch (environment) {
      case "localhost":
      case "dev":
      case "test":
      case "product":
        return (
          window.location.protocol +
          "//" +
          window.location.host +
          "/pkiservice/" +
          icaConfigURL
        );
        break;
      case "ca":
        return (
          window.location.protocol +
          "//" +
          window.location.host +
          "/pub/ICAPKIService/ICAClientSign/pkiservice/" +
          icaConfigURL
        );
        break;
      case "proebiz":
        return proebizConfigURL;
        break;
      case "sberbank":
        return sberbankConfigURL;
        break;
      case "cnb":
        return cnbConfigURL;
        break;
      default:
        return csobConfigURL;
        break;
    }*/
  },

  m_langEnum: {
    CZ: 0,
    SK: 1,
    EN: 2,
  },

  m_curLanguage: 0,
  m_lastErrorNumber: 0,

  callMsgTemp: function () {
    var temp = new ICAPKIService.callMsgTemp();
    temp.library = ICAClientSign.libICAClientSign;
    temp.function.return = "long";

    return temp;
  },

  PROFILE_CAdES_BES: 0,
  PROFILE_CAdES_T: 1,
  PROFILE_PAdES_BES: 0,
  PROFILE_XAdES_BES: 0,

  PdfTextOnly: 0,
  PdfGraphicOnly: 1,
  PdfGraphicAndText: 2,
  PdfNotVisible: 3,

  CERTLOAD_ALL_TIME_VALID: 0,
  CERTLOAD_SIGNING_FLAG: 1,
  CERTLOAD_ENCIPHERMENT_FLAG: 2,
  CERTLOAD_QUALIFIED_FLAG: 4,
  CERTLOAD_TWINS_QUALIFIED_FLAG: 8,
  CERTLOAD_IGNORE_TIME_VALIDITY_FLAG: 16,
  CERTLOAD_QSCD_QUALIFIED_FLAG: 32,

  CERTVAL_SN_DEC: 1,
  CERTVAL_SN_HEX: 2,
  CERTVAL_CN: 3,
  CERTVAL_SUBJECT_DN: 4,
  CERTVAL_ISSUER_DN: 5,
  CERTVAL_CHECK_VALIDITY: 6,
  CERTVAL_DAYS_TO_NOTAFTER: 7,
  CERTVAL_NOTBEFORE: 8,
  CERTVAL_NOTAFTER: 9,
  CERTVAL_SHA1_HASH_HEX: 10,
  CERTVAL_QUALIFIED: 11,
  CERTVAL_QUALIFIED_ON_QSCD: 12,

  //----------ICACLIENTSIGN LIBRARY FUNCTIONS----------------

  //initialize native messaging app
  InitializeHost: function (cb) {
    Log("log", "InitializeHost(): start");
    var msg = new ICAPKIService.callMsgTemp();
    msg.function.name = "InitHost";
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      if (ICAPKIService.checkHostResponse("InitializeHost()", rsp)) {
        ICAPKIService.SetObjectState(3); //host installed state
        cb();
      }
    });
  },

  //download ICF config file, download files of library (or check file present on disk), cbInit = callback code run after successfully loaded libraries
  LoadLibrary: function (cbInit) {
    Log("log", "LoadLibrary(): start");

    var uninitMsg = new ICAClientSign.callMsgTemp();
    uninitMsg.function.name = "IcsxFinalizeLibrary";

    var msg = new ICAPKIService.callMsgTemp();
    msg.function.name = "LoadLibrary";
    msg.function.inParams = [
      "const char[]",
      "const char[]",
      "char[#4]",
      "int*",
      "const char[]",
      "const char[]",
    ];
    msg.function.inParamsVal = [
      ICAClientSign.libICAClientSign,
      ICAClientSign.libICAClientSignURL(),
      null,
      260,
      JSON.stringify(uninitMsg),
      "IcsxFreeBuffer",
    ];
    msg.function.outParams = ["#3 char[#4] string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      if (ICAPKIService.checkHostResponse("LoadLibrary()", rsp)) {
        var libFolderPath = rsp.function.outParamsVal[0];
        ICAPKIService.SetObjectState(4); //host installed state
        cbInit(libFolderPath);
      }
    });
  },

  //get text message for error number, exception in native host app
  GetHostErrorString: function (num, cb, funcName) {
    var fnName = "GetHostErrorString";
    Log("log", fnName + ": start");
    var msg = new ICAPKIService.callMsgTemp();
    msg.function.name = "GetHostErrorString";
    msg.function.inParams = ["int", "char[#3]", "int*"];
    msg.function.inParamsVal = [num, null, 1000];
    msg.function.outParams = ["#2 char[#3] string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      try {
        ICAPKIService.checkHostResponse("GetHostErrorString()", rsp);
        var str = rsp.function.outParamsVal[0];
        cb(num, str);
      } catch (ex) {
        ICAPKIService.m_processHostExceptionCallback(funcName, ex);
      }
    });
  },

  //ICAClientSign - library version info
  getAbout: function (cb) {
    Log("log", "getAbout: start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxGetAbout";
    msg.function.inParams = ["char**"];
    msg.function.outParams = ["#1 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxGetAbout()", rsp);
      var about = rsp.function.outParamsVal[0];
      cb(about);
    });
  },

  //ICAClientSign - initialize library
  Initialize: function (cb) {
    var cbInit = function cbInit(libraryFolderPath) {
      Log("log", "Initialize: start");
      var msg = new ICAClientSign.callMsgTemp();
      msg.function.name = "IcsxInitializeLibrary";
      msg.function.inParams = ["const char[]"];
      msg.function.inParamsVal = [libraryFolderPath];
      ICAPKIService.sendCallMessage(msg, function (rsp) {
        if (ICAPKIService.checkResponse("IcsxInitializeLibrary()", rsp)) {
          ICAPKIService.SetObjectState(5); //ready
          if (paramExists(cb)) cb();
        }
      });
    };

    ICAClientSign.LoadLibrary(cbInit);
  },

  //ICAClientSign - get text message for last error
  getErrorMessage: function (cb, num, funcName) {
    var fnName = "getErrorMessage";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxGetErrorMessage";
    msg.function.inParams = ["long", "int", "char**"];
    msg.function.inParamsVal = [num, ICAClientSign.m_curLanguage];
    msg.function.outParams = ["#3 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      try {
        //nevolat checkResponse, hrozi zacykleni v pripade chyby
        // ICAPKIService.checkResponse("IcsxGetErrorMessage()", rsp);
        var str = rsp.function.outParamsVal[0];
        cb(num, str);
      } catch (ex) {
        ICAPKIService.m_processSignerExceptionCallback(funcName, ex);
      }
    });
  },

  //ICAClientSign - clear log
  logClear: function (cb) {
    var fnName = "LogClear";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxLogClear";
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      var stat = ICAPKIService.checkResponse("IcsxLogClear()", rsp);
      cb(stat);
    });
  },

  //ICAClientSign - get log
  logGetContent: function (cb) {
    var fnName = "logGetContent";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxLogGetContent";
    msg.function.inParams = ["char**"];
    msg.function.outParams = ["#1 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxLogGetContent()", rsp);
      var logString = rsp.function.outParamsVal[0];
      cb(logString);
    });
  },

  //ICAClientSign - obtain a specific value from the certificate - eg. SN, SUBJECT
  certificateGetValue: function (cb, partText, certIndex, pem, partEnum) {
    var fnName = "certificateGetValue";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxCertificateGetValue";
    msg.function.inParams = ["char[]", "int", "char**"];
    msg.function.inParamsVal = [pem, partEnum];
    msg.function.outParams = ["#3 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxCertificateGetValue()", rsp);
      var value = rsp.function.outParamsVal[0];
      cb(partText, value, certIndex, pem);
    });
  },

  //ICAClientSign - in: index = index of certificate to return, returns the index of the certificate and certificate in PEM format, then run a callback for further PEM processing
  certificateEnumerateStore: function (cb, index) {
    var fnName = "certificateEnumerateStore";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxCertificateEnumerateStore";
    msg.function.inParams = ["int", "char**"];
    msg.function.inParamsVal = [index];
    msg.function.outParams = ["#2 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxCertificateEnumerateStore()", rsp);
      var pem = rsp.function.outParamsVal[0];
      cb(index, pem);
    });
  },

  //ICAClientSign - returns the number of certificates in storage, cardStore (true/false) - whether to get certificates from Windows / PFX file or from the smart card, flags - flags for selecting specific certificates (optional)
  certificateLoadUserKeyStore: function (cb, cardStore, flags) {
    var fnName = "certificateLoadUserKeyStore";
    Log("log", fnName + ": start");

    cardStore = cardStore ? 1 : 0;
    flags = paramExists(flags)
      ? flags
      : ICAClientSign.CERTLOAD_SIGNING_FLAG |
        ICAClientSign.CERTLOAD_TWINS_QUALIFIED_FLAG;

    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxCertificateLoadUserKeyStore";
    msg.function.inParams = ["int", "unsigned long*", "unsigned long"];
    msg.function.inParamsVal = [cardStore, null, flags];
    msg.function.outParams = ["#2 unsigned long* int"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxCertificateLoadUserKeyStore()", rsp);
      var count = rsp.function.outParamsVal[0];
      cb(count);
    });
  },

  //ICAClientSign - returns the number of certificates in storage, in: cardStore (true/false) - whether to get certificates from Windows / PFX file or from the smart card, serialNumber - which certificates' SN to get, hexadecimal (true/false) - false = DEC format
  certificateLoadUserKeyStoreSN: function (
    cb,
    cardStore,
    serialNumbers,
    hexadecimal
  ) {
    var fnName = "certificateLoadUserKeyStoreSN";
    Log("log", fnName + ": start");
    cardStore = cardStore ? 1 : 0;
    hexadecimal = hexadecimal ? 1 : 0;
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxCertificateLoadUserKeyStoreSN";
    msg.function.inParams = ["int", "const char[]", "int", "unsigned long*"];
    msg.function.inParamsVal = [cardStore, serialNumbers, hexadecimal];
    msg.function.outParams = ["#4 unsigned long* int"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxCertificateLoadUserKeyStoreSN()", rsp);
      var count = rsp.function.outParamsVal[0];
      cb(count);
    });
  },

  //ICAClientSign - returns the number of certificates in storage, in: cardStore (true/false) - whether to get certificates from Windows / PFX file or from the smart card, jsonFilter - JSON filter containing GivenNames, Surnames and Emails, flags - flags for selecting specific certificates (optional)
  certificateLoadUserKeyStoreJSON: function (cb, cardStore, jsonFilter, flags) {
    var fnName = "certificateLoadUserKeyStoreJSON";
    Log("log", fnName + ": start");

    cardStore = cardStore ? 1 : 0;
    flags = paramExists(flags)
      ? flags
      : ICAClientSign.CERTLOAD_SIGNING_FLAG |
        ICAClientSign.CERTLOAD_QSCD_QUALIFIED_FLAG;

    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxCertificateLoadUserKeyStoreJSON";
    msg.function.inParams = [
      "int",
      "const char[]",
      "unsigned long*",
      "unsigned long",
    ];
    msg.function.inParamsVal = [cardStore, jsonFilter, null, flags];
    msg.function.outParams = ["#3 unsigned long* int"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxCertificateLoadUserKeyStoreJSON()", rsp);
      var count = rsp.function.outParamsVal[0];
      cb(count);
    });
  },

  //ICAClientSign - get certificate used for signing, returns PEM of the certificate
  signerGetCertificate: function (cb) {
    var fnName = "signerGetCertificate";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxSignerGetCertificate";
    msg.function.inParams = ["char**"];
    msg.function.outParams = ["#1 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxSignerGetCertificate()", rsp);
      var pemCert = rsp.function.outParamsVal[0];
      cb(pemCert);
    });
  },

  //ICAClientSign - set certificate used for signing, in: cb - callback to run after setting the certificate, pem - certificate in PEM format
  signerSetCertificate: function (cb, pem) {
    var fnName = "signerSetCertificate";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxSignerSetCertificate";
    msg.function.inParams = ["char[]"];
    msg.function.inParamsVal = [pem];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxSignerSetCertificate()", rsp);
      cb();
    });
  },

  //ICAClientSign - set browser cookie
  setCookie: function (cb, cookieObjects) {
    //pole URL a pole k nim odpovídajících jmen proměnných
    var fnName = "setCookie";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxSetCookie";
    msg.function.inParams = ["const char[]"];
    msg.function.inParamsVal = [JSON.stringify(cookieObjects)];

    var responseCallback = function (rsp) {
      ICAPKIService.checkResponse("IcsxSetCookie()", rsp);
      cb();
    };

    var msgB64 = encodeToBase64(JSON.stringify(msg));
    var msgId = HashCode.value(msgB64);
    var wrappedMsg = {
      type: ICAPKIService.m_messageTypeEnum.DIRECTIVE,
      purpose: "cookie",
      id: msgId,
      content: msgB64,
    };

    Log(
      "log",
      "sendCallMessage: Registering msgId(hash)=" +
        msgId +
        " to the map of callbacks"
    );
    Log(
      "info",
      "sendCallMessage: Decoded call content: " + JSON.stringify(msg)
    );
    ICAPKIService.m_callbackMap[msgId] = responseCallback;
    ICAPKIService.sendMessage(wrappedMsg);
  },

  //ICAClientSign - download document from server, in: url - address of document to download
  contentDownload: function (cb, url) {
    var fnName = "contentDownload";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxContentDownload";
    msg.function.inParams = ["const char[]", "char**", "long*", "char**"];
    msg.function.inParamsVal = [url];
    msg.function.outParams = [
      "#2 char[]* string",
      "#3 long* long",
      "#4 char[]* string",
    ];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      if (!ICAPKIService.checkResponse("IcsxContentDownload()", rsp)) return;
      var contentId = rsp.function.outParamsVal[0];
      var httpStatus = rsp.function.outParamsVal[1];
      var httpResponse = rsp.function.outParamsVal[2];
      cb(httpStatus, httpResponse, contentId);
    });
  },

  //ICAClientSign - upload document to server, in: contentId - signed content to upload, url - address of server/script for uploading
  contentUpload: function (cb, contentId, url) {
    var fnName = "contentUpload";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxContentUpload";
    msg.function.inParams = ["const char[]", "const char[]", "long*", "char**"];
    msg.function.inParamsVal = [contentId, url];
    msg.function.outParams = ["#3 long* long", "#4 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      if (!ICAPKIService.checkResponse("IcsxContentUpload()", rsp)) return;
      var httpStatus = rsp.function.outParamsVal[0];
      var httpResponse = rsp.function.outParamsVal[1];
      cb(httpStatus, httpResponse);
    });
  },

  //ICAClientSign - display a preview of the document in the user's default desktop application, in: content - data to show, file_ext: extension of file (ex. pdf)
  contentPreview: function (cb, content, file_ext) {
    var fnName = "contentPreview";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxContentPreview";
    msg.function.inParams = ["const char[]", "const char[]"];
    msg.function.inParamsVal = [content, file_ext];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      var stat = ICAPKIService.checkResponse("IcsxContentPreview()", rsp);
      cb(stat);
    });
  },

  //ICAClientSign - save a file to disk, in: content - data to save, full_fill_name - default file name including extension, dialogTitle - title of save dialog
  saveToDisk: function (cb, content, full_fill_name, dialogTitle) {
    var fnName = "saveToDisk";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxSaveToDisk";
    msg.function.inParams = ["const char[]", "const char[]", "const char[]"];
    msg.function.inParamsVal = [content, full_fill_name, dialogTitle];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      var stat = ICAPKIService.checkResponse("IcsxSaveToDisk()", rsp);
      cb(stat);
    });
  },

  getSignature: function (cb, signedId) {
    var fnName = "getSignature";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxGetSignature";
    msg.function.inParams = ["const char[]", "char**"];
    msg.function.inParamsVal = [signedId];
    msg.function.outParams = ["#2 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxGetSignature()", rsp);
      var signedContent = rsp.function.outParamsVal[0];
      cb(signedContent);
    });
  },

  //ICAClientSign - calculate hashes of the file, in: hashAlgs - hashes to return (SHA-1;SHA-256;SHA-512), full_fill_name - default file name including extension, dialogTitle - title of save dialog
  getFileHash: function (cb, hashAlgs, full_fill_name, dialogTitle) {
    var fnName = "getFileHash";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxGetFileHash";
    msg.function.inParams = [
      "const char[]",
      "const char[]",
      "const char[]",
      "char**",
    ];
    msg.function.inParamsVal = [hashAlgs, full_fill_name, dialogTitle];
    msg.function.outParams = ["#4 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxGetFileHash()", rsp);
      var hashValues = rsp.function.outParamsVal[0];
      cb(hashValues);
    });
  },

  //ICAClientSign - get serial number of timestamp from file, in: timestampValType - integer of type of SN to return, full_fill_name - default file name including extension, dialogTitle - title of save dialog
  getTimeStampSNFromSignedFile: function (
    cb,
    timestampValType,
    full_fill_name,
    dialogTitle
  ) {
    var fnName = "getTimeStampSNFromSignedFile";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxTimeStampGetValuesFromSignedFile";
    msg.function.inParams = ["int", "const char[]", "const char[]", "char**"];
    msg.function.inParamsVal = [timestampValType, full_fill_name, dialogTitle];
    msg.function.outParams = ["#4 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse(
        "IcsxTimeStampGetValuesFromSignedFile()",
        rsp
      );
      var snValues = rsp.function.outParamsVal[0];
      cb(snValues);
    });
  },

  //ICAClientSign - CMS internal signature
  signCms: function (cb, content) {
    var fnName = "signCms";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxCmsSign";
    msg.function.inParams = ["const char[]", "char**"];
    msg.function.inParamsVal = [content];
    msg.function.outParams = ["#2 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxCmsSign()", rsp);
      var signedContent = rsp.function.outParamsVal[0];
      cb(signedContent);
    });
  },

  //ICAClientSign - CMS external signature
  signCmsDetached: function (cb, content) {
    var fnName = "signCmsDetached";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxCmsSignDetached";
    msg.function.inParams = ["const char[]", "char**"];
    msg.function.inParamsVal = [content];
    msg.function.outParams = ["#2 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxCmsSignDetached()", rsp);
      var signedContent = rsp.function.outParamsVal[0];
      cb(signedContent);
    });
  },

  //ICAClientSign - add signature to existing CMS internal signature
  coSignCms: function (cb, content) {
    var fnName = "coSignCms";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxCmsCoSign";
    msg.function.inParams = ["const char[]", "char**"];
    msg.function.inParamsVal = [content];
    msg.function.outParams = ["#2 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxCmsCoSign()", rsp);
      var signedContent = rsp.function.outParamsVal[0];
      cb(signedContent);
    });
  },

  //ICAClientSign - add signature to existing CMS external signature
  coSignCmsDetached: function (cb, content, originalContent) {
    var fnName = "coSignCmsDetached";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxCmsCoSignDetached";
    msg.function.inParams = ["const char[]", "const char[]", "char**"];
    msg.function.inParamsVal = [content, originalContent];
    msg.function.outParams = ["#3 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxCmsCoSignDetached()", rsp);
      var signedContent = rsp.function.outParamsVal[0];
      cb(signedContent);
    });
  },

  //ICAClientSign - CAdES internal signature
  signCades: function (cb, content, profile) {
    var fnName = "signCades";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxCadesSign";
    msg.function.inParams = ["const char[]", "int", "char**"];
    msg.function.inParamsVal = [content, profile];
    msg.function.outParams = ["#3 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxCadesSign()", rsp);
      var signedContent = rsp.function.outParamsVal[0];
      cb(signedContent);
    });
  },

  //ICAClientSign - CAdES external signature
  signCadesDetached: function (cb, content, profile) {
    var fnName = "signCadesDetached";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxCadesSignDetached";
    msg.function.inParams = ["const char[]", "int", "char**"];
    msg.function.inParamsVal = [content, profile];
    msg.function.outParams = ["#3 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxCadesSignDetached()", rsp);
      var signedContent = rsp.function.outParamsVal[0];
      cb(signedContent);
    });
  },

  //ICAClientSign - add signature to existing CAdES internal signature
  coSignCades: function (cb, content, profile) {
    var fnName = "coSignCades";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxCadesCoSign";
    msg.function.inParams = ["const char[]", "int", "char**"];
    msg.function.inParamsVal = [content, profile];
    msg.function.outParams = ["#3 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxCadesCoSign()", rsp);
      var signedContent = rsp.function.outParamsVal[0];
      cb(signedContent);
    });
  },

  //ICAClientSign - add signature to existing CAdES external signature
  coSignCadesDetached: function (cb, content, originalContent, profile) {
    var fnName = "coSignCadesDetached";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxCadesCoSignDetached";
    msg.function.inParams = ["const char[]", "const char[]", "int", "char**"];
    msg.function.inParamsVal = [content, originalContent, profile];
    msg.function.outParams = ["#4 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxCadesCoSignDetached()", rsp);
      var signedContent = rsp.function.outParamsVal[0];
      cb(signedContent);
    });
  },

  //ICAClientSign - PAdES (PDF) signature
  signPades: function (cb, content, profile) {
    var fnName = "signPades";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPadesSign";
    msg.function.inParams = ["const char[]", "int", "char**"];
    msg.function.inParamsVal = [content, profile];
    msg.function.outParams = ["#3 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPadesSign()", rsp);
      var signedContent = rsp.function.outParamsVal[0];
      cb(signedContent);
    });
  },

  //ICAClientSign - get description used for PDF signature
  pdfOptionsGetDescription: function (cb) {
    var fnName = "pdfOptionsGetDescription";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsGetDescription";
    msg.function.inParams = ["char**"];
    msg.function.outParams = ["#1 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsGetDescription()", rsp);
      var desc = rsp.function.outParamsVal[0];
      cb(desc);
    });
  },

  //ICAClientSign - set description used for PDF signature
  pdfOptionsSetDescription: function (cb, desc) {
    var fnName = "pdfOptionsSetDescription";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsSetDescription";
    msg.function.inParams = ["const char[]"];
    msg.function.inParamsVal = [desc];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsSetDescription()", rsp);
      cb(fnName);
    });
  },

  //ICAClientSign - get reason text used for PDF signature
  pdfOptionsGetPdfSignReason: function (cb) {
    var fnName = "pdfOptionsGetPdfSignReason";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsGetPdfSignReason";
    msg.function.inParams = ["char**"];
    msg.function.outParams = ["#1 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsGetPdfSignReason()", rsp);
      var desc = rsp.function.outParamsVal[0];
      cb(desc);
    });
  },

  //ICAClientSign - set reason text used for PDF signature
  pdfOptionsSetPdfSignReason: function (cb, reason) {
    var fnName = "pdfOptionsSetPdfSignReason";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsSetPdfSignReason";
    msg.function.inParams = ["const char[]"];
    msg.function.inParamsVal = [reason];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsSetPdfSignReason()", rsp);
      cb(fnName);
    });
  },

  //ICAClientSign - get number of page where to place visible signature
  pdfOptionsGetSignaturePage: function (cb) {
    var fnName = "pdfOptionsGetSignaturePage";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsGetSignaturePage";
    msg.function.inParams = ["int*"];
    msg.function.outParams = ["#1 int* int"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsGetSignaturePage()", rsp);
      var page = rsp.function.outParamsVal[0];
      cb(page);
    });
  },

  //ICAClientSign - set number of page where to place visible signature
  pdfOptionsSetSignaturePage: function (cb, page) {
    var fnName = "pdfOptionsSetSignaturePage";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsSetSignaturePage";
    msg.function.inParams = ["int"];
    msg.function.inParamsVal = [page];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsSetSignaturePage()", rsp);
      cb(fnName);
    });
  },

  //ICAClientSign - get type (visible, invisible, ...) of PDF signature
  pdfOptionsGetSignatureType: function (cb) {
    var fnName = "pdfOptionsGetSignatureType";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsGetSignatureType";
    msg.function.inParams = ["int*"];
    msg.function.outParams = ["#1 int* int"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsGetSignatureType()", rsp);
      var type = rsp.function.outParamsVal[0];
      cb(type);
    });
  },

  //ICAClientSign - set type (visible, invisible, ...) of PDF signature
  pdfOptionsSetSignatureType: function (cb, type) {
    var fnName = "pdfOptionsSetSignatureType";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsSetSignatureType";
    msg.function.inParams = ["int"];
    msg.function.inParamsVal = [type];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsSetSignatureType()", rsp);
      cb(fnName);
    });
  },

  //ICAClientSign - get name of signer (signing person) used for signing
  pdfOptionsGetSignerName: function (cb) {
    var fnName = "pdfOptionsGetSignerName";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsGetSignerName";
    msg.function.inParams = ["char**"];
    msg.function.outParams = ["#1 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsGetSignerName()", rsp);
      var name = rsp.function.outParamsVal[0];
      cb(name);
    });
  },

  //ICAClientSign - set name of signer (signing person) used for signing
  pdfOptionsSetSignerName: function (cb, name) {
    var fnName = "pdfOptionsSetSignerName";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsSetSignerName";
    msg.function.inParams = ["const char[]"];
    msg.function.inParamsVal = [name];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsSetSignerName()", rsp);
      cb(fnName);
    });
  },

  //ICAClientSign - get location text (not position) used for signing
  pdfOptionsGetSignLocation: function (cb) {
    var fnName = "pdfOptionsGetSignLocation";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsGetSignLocation";
    msg.function.inParams = ["char**"];
    msg.function.outParams = ["#1 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsGetSignLocation()", rsp);
      var loc = rsp.function.outParamsVal[0];
      cb(loc);
    });
  },

  //ICAClientSign - set location text (not position) used for signing
  pdfOptionsSetSignLocation: function (cb, loc) {
    var fnName = "pdfOptionsSetSignLocation";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsSetSignLocation";
    msg.function.inParams = ["const char[]"];
    msg.function.inParamsVal = [loc];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsSetSignLocation()", rsp);
      cb(fnName);
    });
  },

  //ICAClientSign - get horizontal position of signature (PDF file)
  pdfOptionsGetPosX: function (cb) {
    var fnName = "pdfOptionsGetPosX";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsGetPosX";
    msg.function.inParams = ["float*"];
    msg.function.outParams = ["#1 float* float"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsGetPosX()", rsp);
      var pos = rsp.function.outParamsVal[0];
      cb(pos);
    });
  },

  //ICAClientSign - set horizontal position of signature (PDF file)
  pdfOptionsSetPosX: function (cb, pos) {
    var fnName = "pdfOptionsSetPosX";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsSetPosX";
    msg.function.inParams = ["float"];
    msg.function.inParamsVal = [pos];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsSetPosX()", rsp);
      cb(fnName);
    });
  },

  //ICAClientSign - get vertical position of visible signature (PDF file)
  pdfOptionsGetPosY: function (cb) {
    var fnName = "pdfOptionsGetPosY";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsGetPosY";
    msg.function.inParams = ["float*"];
    msg.function.outParams = ["#1 float* float"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsGetPosY()", rsp);
      var pos = rsp.function.outParamsVal[0];
      cb(pos);
    });
  },

  //ICAClientSign - set vertical position of visible signature (PDF file)
  pdfOptionsSetPosY: function (cb, pos) {
    var fnName = "pdfOptionsSetPosY";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsSetPosY";
    msg.function.inParams = ["float"];
    msg.function.inParamsVal = [pos];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsSetPosY()", rsp);
      cb(fnName);
    });
  },

  //ICAClientSign - get width of visible signature (PDF file)
  pdfOptionsGetWidth: function (cb) {
    var fnName = "pdfOptionsGetWidth";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsGetWidth";
    msg.function.inParams = ["float*"];
    msg.function.outParams = ["#1 float* float"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsGetWidth()", rsp);
      var pos = rsp.function.outParamsVal[0];
      cb(pos);
    });
  },

  //ICAClientSign - set width of visible signature (PDF file)
  pdfOptionsSetWidth: function (cb, pos) {
    var fnName = "pdfOptionsSetWidth";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsSetWidth";
    msg.function.inParams = ["float"];
    msg.function.inParamsVal = [pos];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsSetWidth()", rsp);
      cb(fnName);
    });
  },

  //ICAClientSign - get height of visible signature (PDF file)
  pdfOptionsGetHeight: function (cb) {
    var fnName = "pdfOptionsGetHeight";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsGetHeight";
    msg.function.inParams = ["float*"];
    msg.function.outParams = ["#1 float* float"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsGetHeight()", rsp);
      var pos = rsp.function.outParamsVal[0];
      cb(pos);
    });
  },

  //ICAClientSign - set height of visible signature (PDF file)
  pdfOptionsSetHeight: function (cb, pos) {
    var fnName = "pdfOptionsSetHeight";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsSetHeight";
    msg.function.inParams = ["float"];
    msg.function.inParamsVal = [pos];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsSetHeight()", rsp);
      cb(fnName);
    });
  },

  //ICAClientSign - set background image for visible signature (PDF file), in: imgB64 - image encoded to base64
  pdfOptionsLoadBackgroundImage: function (cb, imgB64) {
    var fnName = "pdfOptionsLoadBackgroundImage";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsLoadBackgroundImage";
    msg.function.inParams = ["const char[]"];
    msg.function.inParamsVal = [imgB64];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsLoadBackgroundImage()", rsp);
      cb(fnName);
    });
  },

  //ICAClientSign - set signer image for visible signature (PDF file), in: imgB64 - image encoded to base64
  pdfOptionsLoadSignatureImage: function (cb, imgB64) {
    var fnName = "pdfOptionsLoadSignatureImage";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsLoadSignatureImage";
    msg.function.inParams = ["const char[]"];
    msg.function.inParamsVal = [imgB64];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsLoadSignatureImage()", rsp);
      cb(fnName);
    });
  },

  //ICAClientSign - get name of user property that includes information about anchor (PDF file)
  pdfOptionsGetAnchorPropertyName: function (cb) {
    var fnName = "pdfOptionsGetAnchorPropertyName";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsGetAnchorPropertyName";
    msg.function.inParams = ["char**"];
    msg.function.outParams = ["#1 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsGetAnchorPropertyName()", rsp);
      var anchorPropertyName = rsp.function.outParamsVal[0];
      cb(anchorPropertyName);
    });
  },

  //ICAClientSign - get placeholder text for anchor (PDF file)
  pdfOptionsGetAnchorPlaceholderText: function (cb) {
    var fnName = "pdfOptionsGetAnchorPlaceholderText";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsGetAnchorPlaceholderText";
    msg.function.inParams = ["char**"];
    msg.function.outParams = ["#1 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse(
        "IcsxPdfOptionsGetAnchorPlaceholderText()",
        rsp
      );
      var anchorPlaceholderText = rsp.function.outParamsVal[0];
      cb(anchorPlaceholderText);
    });
  },

  //ICAClientSign - set name of user property that includes information about anchor and placeholder text (PDF file)
  pdfOptionsSetAnchor: function (cb, propertyName, placeholderText) {
    var fnName = "pdfOptionsSetAnchor";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsSetAnchor";
    msg.function.inParams = ["const char[]", "const char[]"];
    msg.function.inParamsVal = [propertyName, placeholderText];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsSetAnchor()", rsp);
      cb(fnName);
    });
  },

  //ICAClientSign - get flag if add timestamp with signature (PDF file)
  pdfOptionsGetAddTimeStamp: function (cb) {
    var fnName = "pdfOptionsGetAddTimeStamp";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsGetAddTimeStamp";
    msg.function.inParams = ["int*"];
    msg.function.outParams = ["#1 int* int"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsGetAddTimeStamp()", rsp);
      var addTimeStamp = rsp.function.outParamsVal[0];
      cb(addTimeStamp);
    });
  },

  //ICAClientSign - set flag if add timestamp with signature (PDF file)
  pdfOptionsSetAddTimeStamp: function (cb, addTimeStamp) {
    var fnName = "pdfOptionsSetAddTimeStamp";
    Log("log", fnName + ": start");
    addTimeStamp = addTimeStamp ? 1 : 0;
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsSetAddTimeStamp";
    msg.function.inParams = ["int"];
    msg.function.inParamsVal = [addTimeStamp];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsSetAddTimeStamp()", rsp);
      cb(fnName);
    });
  },

  //ICAClientSign - reset PDF parameters
  pdfOptionsReset: function (cb) {
    var fnName = "pdfOptionsReset";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsReset";
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsReset()", rsp);
      if (cb) cb();
    });
  },

  //ICAClientSign - get timestamp server URL
  timeStampOptionsGetUrl: function (cb) {
    var fnName = "timeStampOptionsGetUrl";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxTimeStampOptionsGetUrl";
    msg.function.inParams = ["char**"];
    msg.function.outParams = ["#1 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxTimeStampOptionsGetUrl()", rsp);
      var urlTSA = rsp.function.outParamsVal[0];
      cb(urlTSA);
    });
  },

  //ICAClientSign - set timestamp server URL
  timeStampOptionsSetUrl: function (cb, urlTSA) {
    var fnName = "timeStampOptionsSetUrl";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxTimeStampOptionsSetUrl";
    msg.function.inParams = ["const char[]"];
    msg.function.inParamsVal = [urlTSA];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxTimeStampOptionsSetUrl()", rsp);
      cb(fnName);
    });
  },

  //ICAClientSign - get timestamp authentication username
  timeStampOptionsGetAuthUser: function (cb) {
    var fnName = "timeStampOptionsGetAuthUser";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxTimeStampOptionsGetAuthUser";
    msg.function.inParams = ["char**"];
    msg.function.outParams = ["#1 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxTimeStampOptionsGetAuthUser()", rsp);
      var authUser = rsp.function.outParamsVal[0];
      cb(authUser);
    });
  },

  //ICAClientSign - set timestamp authentication username
  timeStampOptionsSetAuthUser: function (cb, authUser) {
    var fnName = "timeStampOptionsSetAuthUser";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxTimeStampOptionsSetAuthUser";
    msg.function.inParams = ["const char[]"];
    msg.function.inParamsVal = [authUser];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxTimeStampOptionsSetAuthUser()", rsp);
      cb(fnName);
    });
  },

  //ICAClientSign - get timestamp authentication password
  timeStampOptionsGetAuthPassword: function (cb) {
    var fnName = "timeStampOptionsGetAuthPassword";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxTimeStampOptionsGetAuthPassword";
    msg.function.inParams = ["char**"];
    msg.function.outParams = ["#1 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxTimeStampOptionsGetAuthPassword()", rsp);
      var authPassword = rsp.function.outParamsVal[0];
      cb(authPassword);
    });
  },

  //ICAClientSign - set timestamp authentication password
  timeStampOptionsSetAuthPassword: function (cb, authPassword) {
    var fnName = "timeStampOptionsSetAuthPassword";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxTimeStampOptionsSetAuthPassword";
    msg.function.inParams = ["const char[]"];
    msg.function.inParamsVal = [authPassword];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxTimeStampOptionsSetAuthPassword()", rsp);
      cb(fnName);
    });
  },

  //ICAClientSign - get timestamp authentication certificate
  timeStampOptionsGetAuthCertificate: function (cb) {
    var fnName = "timeStampOptionsGetAuthCertificate";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxTimeStampOptionsGetAuthCertificate";
    msg.function.inParams = ["char**"];
    msg.function.outParams = ["#1 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse(
        "IcsxTimeStampOptionsGetAuthCertificate()",
        rsp
      );
      var authCertificate = rsp.function.outParamsVal[0];
      cb(authCertificate);
    });
  },

  //ICAClientSign - set timestamp authentication certificate
  timeStampOptionsSetAuthCertificate: function (cb, authCertificate) {
    var fnName = "timeStampOptionsSetAuthCertificate";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxTimeStampOptionsSetAuthCertificate";
    msg.function.inParams = ["const char[]"];
    msg.function.inParamsVal = [authCertificate];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse(
        "IcsxTimeStampOptionsSetAuthCertificate()",
        rsp
      );
      cb(fnName);
    });
  },

  //ICAClientSign - reset TimeStamp parameters
  timeStampOptionsReset: function (cb) {
    var fnName = "timeStampOptionsReset";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxTimeStampOptionsReset";
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxTimeStampOptionsReset()", rsp);
      if (cb) cb();
    });
  },

  //ICAClientSign - initialization, create session for encryption
  encryptCreateSession: function (cb, algorithm) {
    var fnName = "encryptCreateSession";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxEncryptCreateSession";
    msg.function.inParams = ["const char[]", "char**"];
    msg.function.inParamsVal = [algorithm];
    msg.function.outParams = ["#2 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxEncryptCreateSession()", rsp);
      var sessionId = rsp.function.outParamsVal[0];
      cb(sessionId);
    });
  },

  //ICAClientSign - setting the initialization vector
  encryptSetIV: function (cb, sessionId, initVector) {
    var fnName = "encryptSetIV";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxEncryptSetIV";
    msg.function.inParams = ["const char[]", "const char[]"];
    msg.function.inParamsVal = [sessionId, initVector];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxEncryptSetIV()", rsp);
      cb();
    });
  },

  //ICAClientSign - generate a symmetric key in the session
  encryptGenerateSecretKey: function (cb, sessionId, keyLength) {
    var fnName = "encryptGenerateSecretKey";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxEncryptGenerateSecretKey";
    msg.function.inParams = ["const char[]", "int"];
    msg.function.inParamsVal = [sessionId, keyLength];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxEncryptGenerateSecretKey()", rsp);
      cb();
    });
  },

  //ICAClientSign - export symmetric key
  encryptExportSecretKey: function (cb, sessionId, exportCertPem) {
    var fnName = "encryptExportSecretKey";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxEncryptExportSecretKey";
    msg.function.inParams = ["const char[]", "const char[]", "char**"];
    msg.function.inParamsVal = [sessionId, exportCertPem];
    msg.function.outParams = ["#3 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxEncryptExportSecretKey()", rsp);
      var encryptedKey = rsp.function.outParamsVal[0];
      cb(encryptedKey);
    });
  },

  //ICAClientSign - data block encryption
  encryptUpdate: function (cb, sessionId, plainData) {
    var fnName = "encryptUpdate";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxEncryptUpdate";
    msg.function.inParams = ["const char[]", "const char[]", "char**"];
    msg.function.inParamsVal = [sessionId, plainData];
    msg.function.outParams = ["#3 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxEncryptUpdate()", rsp);
      var encryptedData = rsp.function.outParamsVal[0];
      cb(encryptedData);
    });
  },

  //ICAClientSign - finalize encryption (encrypt last block of data, add padding, end session, delete encrypt key)
  encryptFinal: function (cb, sessionId) {
    var fnName = "encryptFinal";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxEncryptFinal";
    msg.function.inParams = ["const char[]", "char**"];
    msg.function.inParamsVal = [sessionId];
    msg.function.outParams = ["#2 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxEncryptFinal()", rsp);
      var encryptedData = rsp.function.outParamsVal[0];
      cb(encryptedData);
    });
  },

  //ICAClientSign - end session for encryption
  encryptCloseSession: function (cb, sessionId) {
    var fnName = "encryptCloseSession";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxEncryptCloseSession";
    msg.function.inParams = ["const char[]"];
    msg.function.inParamsVal = [sessionId];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxEncryptCloseSession()", rsp);
      cb();
    });
  },

  //ICAClientSign - decrypt symmetric key
  decryptSecretKey: function (
    cb,
    encryptedKey,
    decryptionCertPem,
    exportCertPem
  ) {
    var fnName = "decryptSecretKey";
    Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxDecryptSecretKey";
    msg.function.inParams = [
      "const char[]",
      "const char[]",
      "const char[]",
      "char**",
    ];
    if (paramExists(exportCertPem))
      msg.function.inParamsVal = [
        encryptedKey,
        decryptionCertPem,
        exportCertPem,
      ];
    else msg.function.inParamsVal = [encryptedKey, decryptionCertPem, null];
    msg.function.outParams = ["#4 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxDecryptSecretKey()", rsp);
      var decryptedKey = rsp.function.outParamsVal[0];
      cb(decryptedKey);
    });
  },

  //----------XAdES (XML) related FUNCTIONS----------------------

  compressWithGzip: function (cb, toCompress) {
    var fname = "compressWithGzip";
    Log("log", fname + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxContentGzip";
    msg.function.inParams = ["const char[]", "char**"];
    msg.function.inParamsVal = [toCompress];
    msg.function.outParams = ["#2 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxContentGzip()", rsp);
      var contentId = rsp.function.outParamsVal[0];
      if (cb) cb(contentId);
    });
  },

  getBase64OfGzip: function (cb, contentId) {
    var fname = "getBase64OfGzip";
    Log("log", fname + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxGetContent";
    msg.function.inParams = ["const char[]", "char**"];
    msg.function.inParamsVal = [contentId];
    msg.function.outParams = ["#2 char[]* string"];

    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxGetContent()", rsp);
      var b64 = rsp.function.outParamsVal[0];
      if (cb) cb(b64);
    });
  },

  signXml: function (cb, contentIdOrBase64Xml, contentURI, profile) {
    var fname = "signXml";
    Log("log", fname + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxXadesSignDetached";
    msg.function.inParams = ["const char[]", "const char[]", "int", "char**"];
    msg.function.inParamsVal = [contentIdOrBase64Xml, contentURI, profile];
    msg.function.outParams = ["#4 char[]* string"];

    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxXadesSignDetached()", rsp);
      var signature = rsp.function.outParamsVal[0];
      if (cb) cb(signature);
    });
  },

  xadesVerifyHashDetached: function (
    cb,
    unsignedBase64OrContentId,
    base64OrContentIdOfSignature
  ) {
    var fname = "xadesVerifyHashDetached";
    Log("log", fname + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxXadesVerifyHashDetached";
    msg.function.inParams = ["const char[]", "const char[]"];
    msg.function.inParamsVal = [
      unsignedBase64OrContentId,
      base64OrContentIdOfSignature,
    ];

    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxXadesVerifyHashDetached()", rsp);
      if (cb) cb(rsp.function.returnVal);
    });
  },

  //----------NATIVE HOST FUNCTIONS----------------------

  //Get operating system version
  GetOsVersion: function (cb) {
    var fnName = "GetOsVersion";
    Log("log", fnName + ": start");
    var msg = new ICAPKIService.callMsgTemp();
    msg.function.name = "GetOsVersion";
    msg.function.inParams = ["char[#2]", "int*"];
    msg.function.inParamsVal = [null, 100];
    msg.function.outParams = ["#1 char[#2] string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAClientSign.checkHostResponse("GetOsVersion()", rsp);
      var version = rsp.function.outParamsVal[0];
      cb(version);
    });
  },

  //Get installed service pack of Windows
  GetSpVersion: function (cb) {
    var fnName = "GetSpVersion";
    Log("log", fnName + ": start");
    var msg = new ICAPKIService.callMsgTemp();
    msg.function.name = "GetSpVersion";
    msg.function.inParams = ["char[#2]", "int*"];
    msg.function.inParamsVal = [null, 100];
    msg.function.outParams = ["#1 char[#2] string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAClientSign.checkHostResponse("GetSpVersion()", rsp);
      var version = rsp.function.outParamsVal[0];
      cb(version);
    });
  },

  //Get native host app version
  GetNativeHostVersion: function (cb) {
    var fnName = "GetNativeHostVersion";
    Log("log", fnName + ": start");
    var msg = new ICAPKIService.callMsgTemp();
    msg.function.name = "GetNativeHostVersion";
    msg.function.inParams = ["char[#2]", "int*"];
    msg.function.inParamsVal = [null, 10];
    msg.function.outParams = ["#1 char[#2] string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAClientSign.checkHostResponse("GetNativeHostVersion()", rsp);
      var version = rsp.function.outParamsVal[0];
      cb(version);
    });
  },

  //Get whether USB devices are supported
  GetUsbSupport: function (cb) {
    var fnName = "GetUsbSupport";
    Log("log", fnName + ": start");
    var msg = new ICAPKIService.callMsgTemp();
    msg.function.name = "GetUsbSupport";
    msg.function.inParams = ["int*"];
    msg.function.inParamsVal = [null];
    msg.function.outParams = ["#1 int* int"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAClientSign.checkHostResponse("GetUsbSupport()", rsp);
      var usb = rsp.function.outParamsVal[0];
      cb(parseInt(usb));
    });
  },
};
//-----------END OF ICACLIENTSIGN OBJECT FUNCTIONS-------------
