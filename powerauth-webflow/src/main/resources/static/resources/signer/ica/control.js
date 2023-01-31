/**
 * control.js
 * file version 3.0.0.0
 *
 * provides detection of the operating system and browser and the technology used
 * performs the initial initialization of the signature library
 */
 
/**
 * SignerException - Exception
 * @param num - error code
 * @param msg - error message
 */
function SignerException(num, msg) {
  this.name = "Signer exception";
  this.number = num;
  this.description = msg;
}

/**
 * Throw SignerException
 */
function ThrowSignerException(number, funcName, usedLib) {
  function getLibObjByName(name) {
    var library = (function () {
      if (Array.prototype.find)
        return ControlObj.detectedLibs.find(function (lib) {
          if (lib.name == name) return lib;
        });
      else {
        //IE11
        return ControlObj.detectedLibs.forEach(function (element) {
          if (element.name == name) return element;
        });
      }
    })();
    return library.libObj;
  }

  var cbAfterGetProperty = function (num, msg) {
    throw new SignerException(num, msg);
  };

  var cbGetErrStr = function cb(num) {
    if (usedLib == "ICASigner") {
      ICASigner.GetLastErrorStr(num, cbAfterGetProperty, funcName);
    } else {
      //clientsign a advicasigner, ideálně další budoucí knihovny
      var lib = getLibObjByName(usedLib);
      lib.getErrorMessage(cbAfterGetProperty, num, funcName);
    }
  };

  //z důvodu zpětné kompatibility, pokud nedostanu knihovnu, vezmu si první z pole
  usedLib = usedLib || ControlObj.detectedLibs[0].name;

  //protože ICAPKI2 vrací v názvu knihovny i její verzi
  usedLib = usedLib.indexOf("ICAPKI2") !== -1 ? "ICAPKI2" : usedLib;

  //kontrola na validní knihovnu, pokud není v poli knihoven validní knihovna, vyhodím HostException
  if (
    !ControlObj.detectedLibs.some(function (lib) {
      if (usedLib === lib.name) return true;
      return false;
    })
  ) {
    ThrowHostException(
      -1,
      "Unable to decode Host response message from base64"
    );
  }

  //	nejspíše ani není potřeba kontrolovat, protože v případě splnění podmínky se již použila první knihovna v poli
  //   if (usedLib === null || usedLib === undefined)
  //     ThrowHostException(
  //       -1,
  //       "Unable to decode Host response message from base64"
  //     );

  if (usedLib.indexOf("ICAPKI2") !== -1) {
    ThrowICAPKIException(number, funcName);
    return;
  }

  //pokud není zadané číslo chyby, získáme ho
  //jinak rovnou získáme popis chyby
  if (number !== undefined && number !== null) {
	cbGetErrStr(number);
  }
  else {
    var lib = getLibObjByName(usedLib);
    if (typeof lib.m_lastErrorNumber !== "undefined") {
      //pokud knihovna má definováno m_lastErrorNumber
      number = lib.m_lastErrorNumber;
      cbGetErrStr(number);
    }
	else if (typeof lib.GetLastErrorNumber !== "undefined") {
      //pokud knihovna má definováno GetLastErrorNumber
      lib.GetLastErrorNumber(cbGetErrStr);
    }
    else {
	  //knihovna nemá definováno nic z výše uvedených, vyhodím HostException
      ThrowHostException(-1, "Unable to decode Host response message from base64");
	}
  }
}

/**
 * ICAPKIException - Exception
 * @param num - error code
 * @param msg - error message
 */
function ICAPKIException(num, msg) {
  this.name = "ICAPKI2 exception";
  this.number = num;
  this.description = msg;
}

/**
 * Throw ICAPKIException
 */
function ThrowICAPKIException(number, funcName) {
  var cbAFterGetProperty = function (res) {
    if (typeof res === "undefined") {
      ThrowICAPKIException("GetError()");
    }
    throw new ICAPKIException(-1, res);
  };
  ICAPKI2.GetError(cbAFterGetProperty, funcName);
}

/**
 * HostException - Exception
 * @param num - error code
 * @param msg - error message
 */
function HostException(num, msg) {
  this.name = "Native host exception";
  this.number = num;
  this.description = msg;
}

function ThrowHostException(num, msg, funcName) {
  //funcName - zprava z funkce, ktera vyvolava vyjimku
  var cbThrowHostEpx = function cb(n, m) {
    throw new HostException(n, m);
  };

  if (typeof ControlObj.cbAfterHostException !== "undefined") {
    ControlObj.cbAfterHostException();
  }

  //pokud neni zadano cislo chyby
  if (num == undefined || num == null) {
    num = ICAPKIService.m_lastErrorNumber;
  }
  //pokud je zadan popis chyby tzn. volani z ICAPKIService.onMessage error directive
  if (msg != undefined && msg != null) {
    cbThrowHostEpx(num, msg);
  } else {
    //je uvedeno jen cislo tzn. volani z ICAPKIService.checkHostResponse
    //jelikoz je jedno, nad kterym objektem knihovny funkci zavolame, pouzijeme prvni objekt v poli
    ControlObj.detectedLibs[0].libObj.GetHostErrorString(
      num,
      cbThrowHostEpx,
      funcName
    );
  }
}

/**
 * Control object
 */
var ControlObj = {
  osName: "Unknown", //OS name
  browserName: "Unknown", //Browser name
  browserVersion: "0.0", //Browser version
  extensionsSettings: undefined,
  configURLs: undefined,
  detectedLibs: [], //Detected libraries
  cookie: false, //Enabled cookies
  finishInitExtension: false, //detect extension finished
  initExtension: false, //extension init
  finishInitHost: false, //detect host finished
  updateStarted: false, //host update started
  updateFinished: false, //host update finished
  initHost: false, //init host
  ICAPKIBrowserEnum: ICAPKIService.m_browserEnum.OTHER, //browser type
  cbAfterLibInitialize: undefined, //optional callback
  cbAfterHostUpdateStarted: undefined, //optional callback run when host starts updating
  cbAfterHostUpdateFinished: undefined, //optional callback run when host update finishes
  cbOnStateChanged: undefined, //optional callback run when object ICAPKIService changes its status
  cbAfterHostException: undefined, //optional callback run after host exception

  detectUsedLibraries: function () {
    ControlObj.detectedLibs = [];
    if (typeof ICAClientSign === "object")
      ControlObj.detectedLibs.push({
        name: "ICAClientSign",
        libObj: ICAClientSign,
      });
    if (typeof ICAPKI2 === "object")
      ControlObj.detectedLibs.push({ name: "ICAPKI2", libObj: ICAPKI2 });
    if (typeof ICASigner === "object")
      ControlObj.detectedLibs.push({ name: "ICASigner", libObj: ICASigner });
  
	return true;
  },

  /**
   *	Inicialize object. Set OS, browser and cookies
   */
  init: function (extensionsSettings, configURLs) {
	ControlObj.extensionsSettings = extensionsSettings;
	ControlObj.configURLs = configURLs;
	
    if (
      typeof window.navigator !== "undefined" &&
      typeof window.navigator.userAgent !== "undefined"
    ) {
      ICAPKIService.Log(
        "log",
        "Property window.navigator.userAgent='" +
          window.navigator.userAgent +
          "'"
      );

      if (window.navigator.userAgent.indexOf("Windows NT 10.0") != -1) {
        ControlObj.osName = "Windows 10";
      } else if (window.navigator.userAgent.indexOf("Windows NT 6.4") != -1) {
        ControlObj.osName = "Windows 10";
      } else if (window.navigator.userAgent.indexOf("Windows NT 6.3") != -1) {
        ControlObj.osName = "Windows 8.1";
      } else if (window.navigator.userAgent.indexOf("Windows NT 6.2") != -1) {
        ControlObj.osName = "Windows 8";
      } else if (window.navigator.userAgent.indexOf("Windows NT 6.1") != -1) {
        ControlObj.osName = "Windows 7";
      } else if (window.navigator.userAgent.indexOf("Windows NT 6.0") != -1) {
        ControlObj.osName = "Windows Vista";
      } else if (window.navigator.userAgent.indexOf("Windows NT 5.1") != -1) {
        ControlObj.osName = "Windows XP";
      } else if (window.navigator.userAgent.indexOf("Windows NT 5.0") != -1) {
        ControlObj.osName = "Windows 2000";
      } else if (window.navigator.userAgent.indexOf("Mac OS X") != -1) {
        ControlObj.osName = "macOS";
        var version_match = window.navigator.userAgent.match(
          /Mac OS X (\d+)[_.](\d+)/
        );
        ControlObj.osName += " " + version_match[1] + "." + version_match[2];
      } else if (window.navigator.userAgent.indexOf("FreeBSD") != -1) {
        ControlObj.osName = "FreeBSD";
      } else if (window.navigator.userAgent.indexOf("OpenBSD") != -1) {
        ControlObj.osName = "OpenBSD";
      } else if (window.navigator.userAgent.indexOf("HP-UX") != -1) {
        ControlObj.osName = "HP-UX";
      } else if (window.navigator.userAgent.indexOf("IRIX64") != -1) {
        ControlObj.osName = "IRIX64";
      } else if (window.navigator.userAgent.indexOf("SunOS") != -1) {
        ControlObj.osName = "SunOS";
      } else if (window.navigator.userAgent.indexOf("X11") != -1) {
        ControlObj.osName = "UNIX";
      } else if (window.navigator.userAgent.indexOf("Linux") != -1) {
        ControlObj.osName = "Linux";
      } else if (window.navigator.userAgent.indexOf("Android") != -1) {
        ControlObj.osName = "Android";
      }

      ICAPKIService.Log("log", "Detected OS='" + ControlObj.osName + "'");

      var match;
      if ((match = window.navigator.userAgent.match(/Firefox\/(\d+.?\d*)/))) {
        ControlObj.browserName = "Firefox";
        ControlObj.browserVersion = match[1];
        ControlObj.ICAPKIBrowserEnum = ICAPKIService.m_browserEnum.FIREFOX;
        ICAPKIService.m_usedTech = ICAPKIService.m_techEnum.extension;
      } else if (
        (match = window.navigator.userAgent.match(/OPR\/(\d+.?\d*)/))
      ) {
        ControlObj.browserName = "Opera";
        ControlObj.browserVersion = match[1];
        ControlObj.ICAPKIBrowserEnum = ICAPKIService.m_browserEnum.OPERA;
        ICAPKIService.m_usedTech = ICAPKIService.m_techEnum.extension;
      } else if (
        (match = window.navigator.userAgent.match(/Opera\/(\d+.?\d*)/))
      ) {
        ControlObj.browserName = "Opera";
        ControlObj.browserVersion = match[1];
        ControlObj.ICAPKIBrowserEnum = ICAPKIService.m_browserEnum.OPERAold;
      } else if (
        (match = window.navigator.userAgent.match(/Edg\/(\d+.?\d*)/))
      ) {
        ControlObj.browserName = "Edge";
        ControlObj.browserVersion = match[1];
        ControlObj.ICAPKIBrowserEnum = ICAPKIService.m_browserEnum.EDGEnew;
        ICAPKIService.m_usedTech = ICAPKIService.m_techEnum.extension;
      } else if (
        (match = window.navigator.userAgent.match(/Edge\/(\d+.?\d*)/))
      ) {
        ControlObj.browserName = "Edge";
        ControlObj.browserVersion = match[1];
        ControlObj.ICAPKIBrowserEnum = ICAPKIService.m_browserEnum.EDGE;
      } else if (
        (match = window.navigator.userAgent.match(/Chrome\/(\d+.?\d*)/))
      ) {
        ControlObj.browserName = "Chrome";
        ControlObj.browserVersion = match[1];
        ControlObj.ICAPKIBrowserEnum = ICAPKIService.m_browserEnum.CHROME;
        ICAPKIService.m_usedTech = ICAPKIService.m_techEnum.extension;
      } else if (
        (match = window.navigator.userAgent.match(/Version\/(\d+.?\d*).*Safari\//))
      ) {
        ControlObj.browserName = "Safari";
        ControlObj.browserVersion = match[1];
        ControlObj.ICAPKIBrowserEnum = ICAPKIService.m_browserEnum.SAFARI;
		ICAPKIService.m_usedTech = ICAPKIService.m_techEnum.safariAppExtension;
      } else if (
        (match = window.navigator.userAgent.match(/MSIE (\d+.?\d*)/))
      ) {
        ControlObj.browserName = "IE";
        ControlObj.browserVersion = match[1];
        ControlObj.ICAPKIBrowserEnum = ICAPKIService.m_browserEnum.IE;
        ICAPKIService.m_usedTech = ICAPKIService.m_techEnum.activeX;
      } else if ((match = window.navigator.userAgent.match(/Trident\/7\.0/))) {
        ControlObj.browserName = "IE";
        ControlObj.browserVersion = "11.0";
        ControlObj.ICAPKIBrowserEnum = ICAPKIService.m_browserEnum.IE;
        ICAPKIService.m_usedTech = ICAPKIService.m_techEnum.activeX;
      }

      ICAPKIService.Log(
        "log",
        "Detected Browser='" +
          ControlObj.browserName +
          "' version='" +
          ControlObj.browserVersion +
          "'"
      );
    } else {
      ICAPKIService.Log("error", "Property window.navigator.userAgent not set.");
    }

    if (typeof navigator.cookieEnabled !== "undefined") {
      ControlObj.cookie = navigator.cookieEnabled;
      ICAPKIService.Log("log", "Detected cookie='" + ControlObj.cookie + "'");
    } else {
      ICAPKIService.Log("error", "Property navigator.cookieEnabled not set.");
    }
  },

  /**
   * Init extension and host.
   * Call only cookies are enabled, OS and browser are supported.
   * @param cb Optional callback function. Run if ICAPKIService is ready for used.
   */
  initICAPKI: function (cb) {
    ControlObj.cbAfterLibInitialize = cb;
    ControlObj.onStateChanged(0);

    ICAPKIService.setHostExceptionCallbacks(ThrowHostException, ProcException);
    ICAPKIService.setExceptionCallbacks(ThrowSignerException, ProcException);
    ICAPKIService.m_stateChangedCallback = ControlObj.onStateChanged;
    ICAPKIService.m_hostUpdateStartedCallback = ControlObj.onHostUpdateStarted;
    ICAPKIService.m_curBrowser = ControlObj.ICAPKIBrowserEnum;
	
	ICAPKIService.Init(ControlObj.extensionsSettings);

    //initialize state
    ICAPKIService.SetObjectState(1);
	
	if (ControlObj.detectUsedLibraries()) {
		if (typeof(ControlObj.configURLs) !== "undefined") {
			ControlObj.detectedLibs.forEach(function (lib) {
				for (var i = 0; i < ControlObj.configURLs.length; i++) {
					if (lib.name === ControlObj.configURLs[i].library)
						lib.libObj.Init(ControlObj.configURLs[i].url);
				}
			});
		}
	}

    //connect extension/ActiveX
    switch (ICAPKIService.m_usedTech) {
      case ICAPKIService.m_techEnum.extension:
        ICAPKIService.Log("log", "InitObject(): calling ConnectExtension");
        setTimeout(function () {
          if (ControlObj.initExtension == false || !ICAPKIService.IsReady()) {
            ControlObj.initExtension = false;
            ControlObj.finishInitExtension = true;
          }
        }, 15000);
        try {
          if (ICAPKIService.m_curBrowser == ICAPKIService.m_browserEnum.FIREFOX) {
            var i = 1;
            var intLoadFirefoxExt = setInterval(function () {
              try {
                if (ICAPKIService.GetObjectState() != 1) {
                  clearInterval(intLoadFirefoxExt);
                }
				else {
                  ICAPKIService.Log("log", "InitObject(): calling ConnectExtension");
                  ICAPKIService.ConnectExtension(ControlObj.onExtensionConnected);
				  
                  i++;
                  if (i === 8)
					  clearInterval(intLoadFirefoxExt);
                }
              } catch (ex) {
                i++;
                if (i === 8)
					clearInterval(intLoadFirefoxExt);
              }
            }, 80);
          } else {
            ICAPKIService.Log("log", "InitObject(): calling ConnectExtension");
            ICAPKIService.ConnectExtension(ControlObj.onExtensionConnected);
          }
        } catch (ex) {
          ControlObj.initExtension = false;
          ControlObj.finishInitExtension = true;
        }
        break;
      case ICAPKIService.m_techEnum.activeX:
        ICAPKIService.Log(
          "log",
          "InitObject(): getting ActiveX object and calling ConnectActiveX"
        );
        ControlObj.loadActiveX();
        ICAPKIService.m_object = document.getElementById(
          "ICAPKIServiceControl"
        );
        ICAPKIService.m_object.onreadystatechange =
          ICAPKIService.ConnectActiveX(ControlObj.onExtensionConnected);

        setTimeout(function () {
          if (ControlObj.initExtension == false || !ICAPKIService.IsReady()) {
            ControlObj.initExtension = false;
            ControlObj.finishInitExtension = true;
          }
        }, 15000);
        break;
	  case ICAPKIService.m_techEnum.safariAppExtension:
		ICAPKIService.Log("log","InitObject(): calling ConnectSafariAppExtension");
		ControlObj.loadSafariHelpherObjects();
		
		var safariEvent = new Event('ICAPKIServiceObjLoaded');
		document.dispatchEvent(safariEvent);
		
		var i = 1;
        var intLoadSafariExt = setInterval(function () {
			if (i === 8)
				clearInterval(intLoadSafariExt);
					
			try {
				if (document.getElementById("ICAPKIServiceCommandObj").className === "ICAPKIServiceScriptLoaded") {
					clearInterval(intLoadSafariExt);
					ICAPKIService.Log("log", "InitObject(): calling ConnectExtension");
					ICAPKIService.ConnectSafariAppExtension(ControlObj.onExtensionConnected);
				}
				else {
					i++;
				}
			}
			catch (ex) {
				i++;
			}
        }, 1000);

		setTimeout(function () {
			if (ControlObj.initExtension == false || !ICAPKIService.IsReady()) {
				ControlObj.initExtension = false;
				ControlObj.finishInitExtension = true;
			}
		}, 15000);
        break;
    }
  },

  /**
   * PKI library is inicialized and ready for used.
   */
  onLibInitialize: function () {
    ControlObj.finishInitHost = true;
    ControlObj.initHost = true;
    if (typeof ControlObj.cbAfterLibInitialize !== "undefined") {
      ControlObj.cbAfterLibInitialize();
    }
  },

  /**
   *	Host is inicialized
   */
  onHostInit: function () {
    var cb = (function () {
      var initializedLibsCount = 0;

      return function () {
        initializedLibsCount++;
        if (initializedLibsCount === ControlObj.detectedLibs.length)
          ControlObj.onLibInitialize();
      };
    })();

    ICAPKIService.Log("log", "onHostInit(): start");
    //host installed state
    ICAPKIService.SetObjectState(3);

    if (
      typeof ControlObj.cbAfterHostUpdateFinished !== "undefined" &&
      ControlObj.updateStarted == true
    ) {
      ControlObj.hostUpdateFinished = true;
      ControlObj.cbAfterHostUpdateFinished();
    }

    //initialize library
    ICAPKIService.Log("log", "onExtensionConnected(): calling Initialize");

    ControlObj.detectedLibs.forEach(function (lib) {
      lib.libObj.Initialize(cb);
    });
  },

  /**
   *	Extension is connected
   */
  onExtensionConnected: function () {
    ICAPKIService.Log("log", "onExtensionConnected(): start");

    ControlObj.initExtension = true;
    ControlObj.finishInitExtension = true;

    //extension installed state
    ICAPKIService.SetObjectState(2);

    //call host init
    ICAPKIService.Log("log", "onExtensionConnected(): calling InitializeHost");
	
	//init host one time for the first library
	if (ControlObj.detectedLibs.length >= 1)
		ControlObj.detectedLibs[0].libObj.InitializeHost(ControlObj.onHostInit);
  },

  /**
   *	Start update host.
   */
  onHostUpdateStarted: function (closeBrowserBeforeUpdate) {
    ICAPKIService.Log("log", "onHostUpdateStarted(): started");
    ControlObj.updateStarted = true;

    if (typeof ControlObj.cbAfterHostUpdateStarted !== "undefined") {
      ControlObj.cbAfterHostUpdateStarted(closeBrowserBeforeUpdate);
    }

    ICAPKIService.Log(
      "info",
      "onHostUpdateStarted: setting object state to 1 on update started"
    );
    ICAPKIService.SetObjectState(1);

    if (ICAPKIService.m_usedTech == ICAPKIService.m_techEnum.activeX) {
      ControlObj.unloadActiveX();
    }

    if (closeBrowserBeforeUpdate) {
      ICAPKIService.Log("info", "onHostUpdateStarted(): user must closed the browser first");
    } else {
      ICAPKIService.Log(
        "info",
        "onHostUpdateStarted(): user don't need to close the browser"
      );

      var firstTimeInterval = 11; //[s]
      var nextTimeInterval = 3; //[s]

      //reinicializace v pravidelnem intervalu (prvni cekani 'firstTimeInterval' vterin, pak po 'nextTimeInterval' vterinach)
      var firsttimeout = setTimeout(function () {
        var nexttimeout = setInterval(function () {
          if (ICAPKIService.GetObjectState() < 3)
            ControlObj.reInitHostAfterUpdate();
          else clearInterval(nexttimeout);
        }, nextTimeInterval * 1000);
      }, firstTimeInterval * 1000);
    }
  },

  /**
   *	Re init after host update
   */
  reInitHostAfterUpdate: function () {
    //connect extension/activeX
    switch (ICAPKIService.m_usedTech) {
      case ICAPKIService.m_techEnum.extension:
        ICAPKIService.Log("log", "InitObject(): calling ConnectExtension");
        ICAPKIService.ConnectExtension(ControlObj.onExtensionConnected);
        break;
      case ICAPKIService.m_techEnum.activeX:
        ICAPKIService.Log(
          "log",
          "InitObject(): getting ActiveX object and calling ConnectActiveX"
        );
        ICAPKIService.m_object = document.getElementById(
          "ICAPKIServiceControl"
        );
        ICAPKIService.m_object.onreadystatechange =
          ICAPKIService.ConnectActiveX(ControlObj.onExtensionConnected);
        break;
	  case ICAPKIService.m_techEnum.safariAppExtension:
		ICAPKIService.Log("log","InitObject(): calling ConnectSafariAppExtension");
		ICAPKIService.ConnectSafariAppExtension(onExtensionConnected);
		break;
    }
  },

  /**
   *	Create ActiveX object in HTML.
   */
  loadActiveX: function () {
    var axobj = document.getElementById("ICAPKIServiceControl");
    if (axobj == undefined || axobj == null) {
      axobj = document.createElement("object");
      axobj.setAttribute("id", "ICAPKIServiceControl");
      axobj.setAttribute(
        "CLASSID",
        "CLSID:0F7C9894-B9D8-4C3A-86E6-425F49A4EC02"
      );
      document.body.appendChild(axobj);
      ICAPKIService.m_activeXObject = axobj;
    }
  },

  /**
   *	Unload ActiveX object in HTML.
   */
  unloadActiveX: function () {
    if (ICAPKIService.m_activeXObject != null) {
      ICAPKIService.m_activeXObject.parentNode.removeChild(
        ICAPKIService.m_activeXObject
      );
      delete ICAPKIService.m_activeXObject;
      ICAPKIService.m_activeXObject = null;
      ICAPKIService.Log("debug", "OnHostUpdateStarted: ActiveX object deleted before update");
    }
  },
  
  /**
   *	Create helper objects for Safari in HTML.
   */
  loadSafariHelpherObjects: function () {
	var safCommandObj = document.getElementById("ICAPKIServiceCommandObj");
	var safResponseObj = document.getElementById("ICAPKIServiceResponseObj");
	
    if (safCommandObj == undefined || safCommandObj == null) {
      safCommandObj = document.createElement("object");
      safCommandObj.setAttribute("id", "ICAPKIServiceCommandObj");
      document.body.appendChild(safCommandObj);
    }
	
	if (safResponseObj == undefined || safResponseObj == null) {
      safResponseObj = document.createElement("object");
      safResponseObj.setAttribute("id", "ICAPKIServiceResponseObj");
      document.body.appendChild(safResponseObj);
    }
	
	ICAPKIService.m_commandObject = safCommandObj;
    ICAPKIService.m_responseObject = safResponseObj;
  },

  /**
   *	Test if PKIService and libraly are loaded.
   */
  isObjectLoaded: function () {
    if (typeof ICAPKIService !== "undefined" && ICAPKIService != null) {
      return (
        (typeof ICAClientSign !== "undefined" && ICAClientSign != null) ||
        (typeof ICAPKI2 !== "undefined" && ICAPKI2 != null) ||
        (typeof ICASigner !== "undefined" && ICASigner != null)
      );
    } else {
      return false;
    }
  },

  /**
   * Change PKIService state
   */
  onStateChanged: function (state) {
    try {
      var isReady = false;
      if (ControlObj.isObjectLoaded()) {
        isReady = ICAPKIService.IsReady();
      }

      if (typeof ControlObj.cbOnStateChanged !== "undefined") {
        ControlObj.cbOnStateChanged(state, isReady);
      }
    } catch (ex) {
      ProcException("onStateChanged()", ex);
    }
  },

  /**
   * Return cookies support.
   */
  isSupportedCookie: function () {
    return navigator.cookieEnabled;
  },

  /**
   * Return OS support.
   * @param supportedOs Which OS do you support
   */
  isSupportedOs: function (supportedOs) {
    for (var i = 0; i < supportedOs.length; i++) {
      if (ControlObj.osName.indexOf(supportedOs[i].name) !== -1) {
        return true;
      }
    }
    return false;
  },

  /**
   * Return browser support.
   * @param supportedBrowser Which browsers do you support
   */
  isSupportedBrowser: function (supportedBrowser) {
    for (var i = 0; i < supportedBrowser.length; i++) {
      if (
        supportedBrowser[i].name == ControlObj.browserName &&
        parseFloat(ControlObj.browserVersion) >=
          parseFloat(supportedBrowser[i].minimalVersion)
      ) {
        return true;
      }
    }
    return false;
  },
};
