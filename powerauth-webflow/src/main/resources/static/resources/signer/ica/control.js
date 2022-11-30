/**
 * Detect used library (ICASigner, ICAClientSign, ICAPKI2)
 */
function detectUsedLibrary() {
	if (typeof (ICAClientSign) !== "undefined") {
		return "ICAClientSign";
	}
	else if (typeof (ICAPKI2) !== "undefined") {
		return "ICAPKI2";
	}
	else {
		return "ICASigner";
	}
}

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
function ThrowSignerException(number, funcName) {
	var cbAfterGetProperty = function (num, msg){
        throw new SignerException(num, msg);
    }
	
	var cbGetErrStr = function cb(num) {
		if (detectUsedLibrary() == "ICAClientSign") {
			ICAClientSign.getErrorMessage(cbAfterGetProperty, num, funcName);
		}
		else {
			ICASigner.GetLastErrorStr(num, cbAfterGetProperty, funcName);
		}
    };
	
	//pokud neni zadano cislo chyby, nejprve ho ziskame
    if(number != undefined && number != null) {
		cbGetErrStr(number);
	}
	else {
		if (detectUsedLibrary() == "ICAClientSign") {
			number = ICAClientSign.m_lastErrorNumber;
			cbGetErrStr(number);
		}
		else {
			ICASigner.GetLastErrorNumber(cbGetErrStr);
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
	var cbAFterGetProperty = function (res){
        if (typeof(res) === "undefined"){
            ThrowICAPKIException("GetError()");
        }
        throw new ICAPKIException(-1, res);
    }
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

function ThrowHostException(num, msg, funcName) { //funcName - zprava z funkce, ktera vyvolava vyjimku
    var cbThrowHostEpx = function cb(n, m) {
        throw new HostException(n, m);
    };
	
	if (typeof(ControlObj.cbAfterHostException) !== "undefined") {
		ControlObj.cbAfterHostException();
	}
	
    //pokud neni zadano cislo chyby
    if (num == undefined || num == null){
        num = ICAPKIService.m_lastErrorNumber;
    }
    //pokud je zadan popis chyby tzn. volani z ICAPKIService.onMessage error directive
    if (msg != undefined && msg != null){
        cbThrowHostEpx(num, msg);
    }
    else{ //je uvedeno jen cislo tzn. volani z ICAPKIService.checkHostResponse
		switch (detectUsedLibrary()) {
			case "ICAClientSign":
				ICAClientSign.GetHostErrorString(num, cbThrowHostEpx, funcName);
				break;
			case "ICAPKI2":
				ICAPKI2.GetHostErrorString(num, cbThrowHostEpx, funcName);
				break;
			default:
				ICASigner.GetHostErrorString(num, cbThrowHostEpx, funcName);
				break;
		}
    }
}

/**
 * Control object
 */
var ControlObj = {
	osName: "Unknown",			//OS name
	browserName: "Unknown",		//Browser name
	browserVersion: "0.0",		//Browser version
	cookie: false,				//Enabled cookies
	finishInitExtension: false,	//detect extension finished
	initExtension: false,		//extension init
	finishInitHost: false,		//detect host finished
	updateStarted: false,		//host update started
	updateFinished: false,		//host update finished
	initHost: false,			//init host
	ICAPKIBrowserEnum: ICAPKIService.m_browserEnum.OTHER,	//browser type
	cbAfterLibInitialize: undefined,	//optional callback
	cbAfterHostUpdateStarted: undefined, //optional callback run when host starts updating
	cbAfterHostUpdateFinished: undefined, //optional callback run when host update finishes
	cbOnStateChanged: undefined, //optional callback run when object ICAPKIService changes its status
	cbAfterHostException: undefined, //optional callback run after host exception
	
	/**
	 *	Inicialize object. Set OS, browser and cookies
	 */
	init: function(){
		if(typeof(window.navigator)!=="undefined" && typeof(window.navigator.userAgent)!=="undefined"){
			
			Log("log", "Property window.navigator.userAgent='"+window.navigator.userAgent+"'");
			
			if (window.navigator.userAgent.indexOf("Windows NT 10.0")!= -1){
				ControlObj.osName="Windows 10";
			}
			else if (window.navigator.userAgent.indexOf("Windows NT 6.4") != -1){
				ControlObj.osName="Windows 10";
			}
			else if (window.navigator.userAgent.indexOf("Windows NT 6.3") != -1){
				ControlObj.osName="Windows 8.1";
			}
			else if (window.navigator.userAgent.indexOf("Windows NT 6.2") != -1){
				ControlObj.osName="Windows 8";
			}
			else if (window.navigator.userAgent.indexOf("Windows NT 6.1") != -1){
				ControlObj.osName="Windows 7";
			}
			else if (window.navigator.userAgent.indexOf("Windows NT 6.0") != -1){
				ControlObj.osName="Windows Vista";
			}
			else if (window.navigator.userAgent.indexOf("Windows NT 5.1") != -1){
				ControlObj.osName="Windows XP";
			}
			else if (window.navigator.userAgent.indexOf("Windows NT 5.0") != -1){
				ControlObj.osName="Windows 2000";
			}
			else if (window.navigator.userAgent.indexOf("Mac OS X") != -1){
				ControlObj.osName="macOS";
				var version_match = window.navigator.userAgent.match(/Mac OS X (\d+)[_.](\d+)/);
				ControlObj.osName+=" " + version_match[1] + "." + version_match[2];
			}
			else if (window.navigator.userAgent.indexOf("FreeBSD") != -1){
				ControlObj.osName="FreeBSD";
			}
			else if (window.navigator.userAgent.indexOf("OpenBSD") != -1){
				ControlObj.osName="OpenBSD";
			}
			else if (window.navigator.userAgent.indexOf("HP-UX") != -1){
				ControlObj.osName="HP-UX";
			}
			else if (window.navigator.userAgent.indexOf("IRIX64") != -1){
				ControlObj.osName="IRIX64";
			}
			else if (window.navigator.userAgent.indexOf("SunOS") != -1){
				ControlObj.osName="SunOS";
			}
			else if (window.navigator.userAgent.indexOf("X11") != -1){
				ControlObj.osName="UNIX";
			}
			else if (window.navigator.userAgent.indexOf("Linux") != -1){
				ControlObj.osName="Linux";
			}
			else if (window.navigator.userAgent.indexOf("Android") != -1){
				ControlObj.osName="Android";
			}
            
            Log("log", "Detected OS='"+ControlObj.osName+"'");
            
            var match;
        	if ((match=window.navigator.userAgent.match(/Firefox\/(\d+.?\d*)/))){
        		ControlObj.browserName="Firefox";
        		ControlObj.browserVersion=match[1];
        		ControlObj.ICAPKIBrowserEnum = ICAPKIService.m_browserEnum.FIREFOX;
            	ICAPKIService.m_usedTech = ICAPKIService.m_techEnum.extension;
        	}
        	else if((match=window.navigator.userAgent.match(/OPR\/(\d+.?\d*)/))){
        		ControlObj.browserName="Opera";
        		ControlObj.browserVersion=match[1];	
        		ControlObj.ICAPKIBrowserEnum = ICAPKIService.m_browserEnum.OPERA;
            	ICAPKIService.m_usedTech = ICAPKIService.m_techEnum.extension;	
        	}
        	else if((match=window.navigator.userAgent.match(/Opera\/(\d+.?\d*)/))){
        		ControlObj.browserName="Opera";
        		ControlObj.browserVersion=match[1];
        		ControlObj.ICAPKIBrowserEnum = ICAPKIService.m_browserEnum.OPERAold;	
        	}
        	else if((match=window.navigator.userAgent.match(/Edg\/(\d+.?\d*)/))){
        		ControlObj.browserName="Edge";
        		ControlObj.browserVersion=match[1];	
        		ControlObj.ICAPKIBrowserEnum = ICAPKIService.m_browserEnum.EDGEnew;
            	ICAPKIService.m_usedTech = ICAPKIService.m_techEnum.extension;	
        	}
        	else if((match=window.navigator.userAgent.match(/Edge\/(\d+.?\d*)/))){
        		ControlObj.browserName="Edge";
        		ControlObj.browserVersion=match[1];
        		ControlObj.ICAPKIBrowserEnum = ICAPKIService.m_browserEnum.EDGE;	
        	}
        	else if((match=window.navigator.userAgent.match(/Chrome\/(\d+.?\d*)/))){
        		ControlObj.browserName="Chrome";
        		ControlObj.browserVersion=match[1];	
        		ControlObj.ICAPKIBrowserEnum = ICAPKIService.m_browserEnum.CHROME;
            	ICAPKIService.m_usedTech = ICAPKIService.m_techEnum.extension;	
        	}
        	else if((match=window.navigator.userAgent.match(/Version\/(\d+.?\d*).*Safari\//))){
        		ControlObj.browserName="Safari";
        		ControlObj.browserVersion=match[1];	
        		ControlObj.ICAPKIBrowserEnum = ICAPKIService.m_browserEnum.SAFARI;	
        	}
        	else if((match=window.navigator.userAgent.match(/Version\/(\d+.?\d*).*Safari\//))){
        		ControlObj.browserName="Safari";
        		ControlObj.browserVersion=match[1];
        		ControlObj.ICAPKIBrowserEnum = ICAPKIService.m_browserEnum.SAFARI;		
        	}
        	else if((match=window.navigator.userAgent.match(/MSIE (\d+.?\d*)/))){
        		ControlObj.browserName="IE";
        		ControlObj.browserVersion=match[1];
        		ControlObj.ICAPKIBrowserEnum = ICAPKIService.m_browserEnum.IE;
            	ICAPKIService.m_usedTech = ICAPKIService.m_techEnum.activeX;		
        	}
        	else if((match=window.navigator.userAgent.match(/Trident\/7\.0/))){
        		ControlObj.browserName="IE";
        		ControlObj.browserVersion="11.0";	
        		ControlObj.ICAPKIBrowserEnum = ICAPKIService.m_browserEnum.IE;
            	ICAPKIService.m_usedTech = ICAPKIService.m_techEnum.activeX;	
        	}
        	
        	Log("log", "Detected Browser='"+ControlObj.browserName+"' version='"+ControlObj.browserVersion+"'");
		}
		else{
			Log("error", "Property window.navigator.userAgent not set.");
		}
		
		if(typeof(navigator.cookieEnabled) !== "undefined"){
			ControlObj.cookie=navigator.cookieEnabled;
			Log("log", "Detected cookie='"+ControlObj.cookie+"'");
		}
		else{
			Log("error", "Property navigator.cookieEnabled not set.");
		}
		
	},
    
    /**
     * Init extension and host.
     * Call only cookies are enabled, OS and browser are supported.
     * @param cb Optionol callback function. Run if ICAPKIService is ready for used.
     */          
	initICAPKI: function(cb){
		ControlObj.cbAfterLibInitialize=cb;
		ControlObj.onStateChanged(0);

        ICAPKIService.setHostExceptionCallbacks(ThrowHostException, ProcException);
        ICAPKIService.setExceptionCallbacks((detectUsedLibrary() == "ICAPKI2") ? ThrowICAPKIException : ThrowSignerException, ProcException);
        ICAPKIService.m_stateChangedCallback = ControlObj.onStateChanged;
        ICAPKIService.m_hostUpdateStartedCallback = ControlObj.onHostUpdateStarted;
        ICAPKIService.m_curBrowser = ControlObj.ICAPKIBrowserEnum;
        
		//initialize state
        ICAPKIService.SetObjectState(1);
		
		//connect extension/ActiveX     
        switch (ICAPKIService.m_usedTech) {
            case ICAPKIService.m_techEnum.extension:
                Log("log", "InitObject(): calling ConnectExtension");
                setTimeout(function () {
                        if (ControlObj.initExtension == false || !ICAPKIService.IsReady()) {
                            ControlObj.initExtension=false;
							ControlObj.finishInitExtension=true;
                        }
                    }, 15000
                );
                try {
                    if (ICAPKIService.m_curBrowser == ICAPKIService.m_browserEnum.FIREFOX) {
                        var i = 1;
                        var int = setInterval(function () {
                            try {
                                if (ICAPKIService.GetObjectState() != 1) {
                                    clearInterval(int);
                                } else {
                                    Log("log", "InitObject(): calling ConnectExtension");
                                    ICAPKIService.ConnectExtension(ControlObj.onExtensionConnected);
                                    i = i + 1;
                                    if (i == 8)
                                        clearInterval(int);
                                }
                            } catch (ex) {
                                i = i + 1;
                                if (i == 8)
                                    clearInterval(int);
                            }
                        }, 80);
                    } else {
                        Log("log", "InitObject(): calling ConnectExtension");
                        ICAPKIService.ConnectExtension(ControlObj.onExtensionConnected);
                    }
                } catch (ex) {
                    ControlObj.initExtension=false;
					ControlObj.finishInitExtension=true;
                }
                break;
            case ICAPKIService.m_techEnum.activeX:
                Log("log", "InitObject(): getting ActiveX object and calling ConnectActiveX");
                ControlObj.loadActiveX();
                ICAPKIService.m_object = document.getElementById("ICAPKIServiceControl");
                ICAPKIService.m_object.onreadystatechange = ICAPKIService.ConnectActiveX(ControlObj.onExtensionConnected);

                setTimeout(function () {
                        if (ControlObj.initExtension == false || !ICAPKIService.IsReady()) {
                            ControlObj.initExtension=false;
							ControlObj.finishInitExtension=true;
                        }
                    }, 15000
                );
                break;
        }
    },
	
	/**
	 * PKI library is inicialized and ready for used.
	 */
	onLibInitialize: function () {
        ControlObj.finishInitHost=true;
		ControlObj.initHost=true;
		if(typeof(ControlObj.cbAfterLibInitialize) !== "undefined"){
			ControlObj.cbAfterLibInitialize();
		}
    },
    
    /**
     *	Host is inicialized
     */                    
    onHostInit: function () {
        Log("log", "onHostInit(): start");
        //host installed state
        ICAPKIService.SetObjectState(3);
		
		if (typeof(ControlObj.cbAfterHostUpdateFinished) !== "undefined" && ControlObj.updateStarted == true) {
			ControlObj.hostUpdateFinished = true;
			ControlObj.cbAfterHostUpdateFinished();
		}
		
        //initialize library
        Log("log", "onExtensionConnected(): calling Initialize");
		
		switch (detectUsedLibrary()) {
			case "ICAClientSign":
				ICAClientSign.Initialize(ControlObj.onLibInitialize);
				break;
			case "ICAPKI2":
				ICAPKI2.Initialize(ControlObj.onLibInitialize);
				break;
			default:
				ICASigner.Initialize(ControlObj.onLibInitialize);
				break;
		}
    },
          
    /**
     *	Extension is connected
     */                        
    onExtensionConnected: function () {
        Log("log", "onExtensionConnected(): start");
        
        ControlObj.initExtension=true;
		ControlObj.finishInitExtension=true;
        
        //extension installed state
        ICAPKIService.SetObjectState(2);
        
        //call host init
        Log("log", "onExtensionConnected(): calling InitializeHost");
		
		switch (detectUsedLibrary()) {
			case "ICAClientSign":
				ICAClientSign.InitializeHost(ControlObj.onHostInit);
				break;
			case "ICAPKI2":
				ICAPKI2.InitializeHost(ControlObj.onHostInit);
				break;
			default:
				ICASigner.InitializeHost(ControlObj.onHostInit);
				break;
		}
    },
    
    /**
     *	Start update host.
     */
	onHostUpdateStarted: function (closeBrowserBeforeUpdate) {
        Log("log", "onHostUpdateStarted(): started");
		ControlObj.updateStarted = true;
		
		if (typeof(ControlObj.cbAfterHostUpdateStarted) !== "undefined") {
			ControlObj.cbAfterHostUpdateStarted(closeBrowserBeforeUpdate);
		}

        Log("info", "onHostUpdateStarted: setting object state to 1 on update started");
        ICAPKIService.SetObjectState(1);

        if (ICAPKIService.m_usedTech == ICAPKIService.m_techEnum.activeX) {
            ControlObj.unloadActiveX();
        }

        if (closeBrowserBeforeUpdate) {
            Log("info", "onHostUpdateStarted(): user must closed the browser first");
        } else {
            Log("info", "onHostUpdateStarted(): user don't need to close the browser");

            var firstTimeInterval = 11; //[s]
            var nextTimeInterval = 3;  //[s]

            //reinicializace v pravidelnem intervalu (prvni cekani 'firstTimeInterval' vterin, pak po 'nextTimeInterval' vterinach)
            var firsttimeout = setTimeout(function () {
                var nexttimeout = setInterval(function () {
                    if (ICAPKIService.GetObjectState() < 3)
                        ControlObj.reInitHostAfterUpdate();
                    else
                        clearInterval(nexttimeout);
                }, nextTimeInterval * 1000);
            }, firstTimeInterval * 1000);
        }
    },

	/**
     *	Re init after host update
     */
 	reInitHostAfterUpdate: function() {
        //connect extension/activeX
        switch (ICAPKIService.m_usedTech) {
            case ICAPKIService.m_techEnum.extension:
                Log("log", "InitObject(): calling ConnectExtension");
                ICAPKIService.ConnectExtension(ControlObj.onExtensionConnected);
                break;
            case ICAPKIService.m_techEnum.activeX:
                Log("log", "InitObject(): getting ActiveX object and calling ConnectActiveX");
                ICAPKIService.m_object = document.getElementById("ICAPKIServiceControl");
                ICAPKIService.m_object.onreadystatechange = ICAPKIService.ConnectActiveX(ControlObj.onExtensionConnected);
                break;
        }
    },
    
    /**
     *	Create ActiveX object in HTML.
     */
    loadActiveX: function (){
        var axobj = document.getElementById("ICAPKIServiceControl");
        if(axobj == undefined || axobj == null) {
            axobj = document.createElement('object')
            axobj.setAttribute('id', "ICAPKIServiceControl")
            axobj.setAttribute('CLASSID', "CLSID:0F7C9894-B9D8-4C3A-86E6-425F49A4EC02");
            document.body.appendChild(axobj);
            ICAPKIService.m_activeXObject = axobj
        }
    },

	/**
     *	Unload ActiveX object in HTML.
     */
    unloadActiveX: function () {
        if(ICAPKIService.m_activeXObject != null) {
            ICAPKIService.m_activeXObject.parentNode.removeChild(ICAPKIService.m_activeXObject);
            delete ICAPKIService.m_activeXObject;
            ICAPKIService.m_activeXObject = null;
            Log("debug","OnHostUpdateStarted: ActiveX object deleted before update");
        }
    },
    
    /**
     *	Test if PKIService and libraly are loaded.
     */
    isObjectLoaded: function () {
		if (typeof (ICAPKIService) !== "undefined" && ICAPKIService != null) {
			return (typeof (ICAClientSign) !== "undefined" && ICAClientSign != null)
				|| (typeof (ICAPKI2) !== "undefined" && ICAPKI2 != null)
				|| (typeof (ICASigner) !== "undefined" && ICASigner != null);
		}
		else {
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
			
			if (typeof(ControlObj.cbOnStateChanged) !== "undefined") {
				ControlObj.cbOnStateChanged(state, isReady);
			}
	    } catch (ex) {
	        ProcException("onStateChanged()", ex);
	    }
	},
    
    /**
     * Return cookies support.
     */
	isSupportedCookie: function(){
		return navigator.cookieEnabled;	
	},
	
	/**
	 * Return OS support.
	 * @param supportedOs Which OS do you support
	 */
	isSupportedOs: function(supportedOs){
		for (var i = 0; i < supportedOs.length; i++) {
			if(ControlObj.osName.indexOf(supportedOs[i].name) !== -1){
				return true;
			}
		}
		return false;
	},
	
	/**
	 * Return browser support.
	 * @param supportedBrowser Which browsers do you support
	 */
	isSupportedBrowser: function(supportedBrowser){
		for (var i = 0; i < supportedBrowser.length; i++) {
			if(supportedBrowser[i].name==ControlObj.browserName && parseFloat(ControlObj.browserVersion)>=parseFloat(supportedBrowser[i].minimalVersion)){
				return true;
			}
		}
		return false;
	},	
};