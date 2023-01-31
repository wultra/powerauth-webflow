/**
 * ICAPKIService.js
 * file version 3.0.1.0
 *
 * contains functions for exchanging messages between the extension
 * in the browser and the desktop application ICAPKIServiceHost
 */

//-------------ICAPKISERVICE OBJECT------------------------
var ICAPKIService = {
    libHost: "ICAPKIServiceHost",
	
	// extension ID I.CA
	extensionIDChrome: "fdolcjnejgbpoadihncaggiicpkhjchl",
	extensionIDOpera: "lbdolmcpegiinogjamobnidmhoggcmho",
	extensionIDEdge: "kchhpancoebhkdgdafnifpkcacaopncp",
	extensionIDFirefox: "icapkiservice@ica.cz",
	extensionInstallURLFirefox: "https://addons.mozilla.org/firefox/addon/i-ca-pki-service-component/",
	
	loggingLevel: 4, //0 - turned off, 1 - error, 2 - warning, 3 - info, 4 - log/debug

    m_messageTypeEnum: {
        CALL: "call",
        RESPONSE: "response",
        CONFIRM: "confirm",
        DIRECTIVE: "directive",
        ERROR: "error"
    },

    m_curBrowser: 0,
    m_usedTech: 0,
	m_curOs: 0,
    m_object: null,

    m_callbackMap: {},
    m_messageBuffer: {},
    m_port: null,
    m_objectState: 0, //0-iddle, 1-initialize, 2-extension installed, 3-host installed, 4-native dll loaded, 5-ready
    m_lastErrorNumber: 0,

    m_throwHostExceptionCallback: null,
    m_processHostExceptionCallback: null,
    m_throwSignerExceptionCallback: null,
    m_processSignerExceptionCallback: null,

    m_extensionConnectedCallback: null,
    m_hostUpdateStartedCallback: null,
    m_stateChangedCallback: null,
	
	m_forceDisconnect: false,
	
	//Safari App Extension only
    m_commandObject: null,
    m_responseObject: null,

    m_browserEnum: {
        OTHER: 0,
        CHROME: 1,
        CHROMEold: 2,
        OPERA: 3,
        OPERAold :4,
        FIREFOX: 5,
        FIREFOXold: 6,
        SAFARI: 7,
        IE: 8,
        IEold: 9,
        EDGE: 10,
		EDGEnew: 11
    },

    m_techEnum : {
        none: 0,
        applet: 1,
        extension: 2,
        activeX: 3,
        plugin: 4,
		safariAppExtension: 5
    },
	
	m_osEnum : {
		Windows: 0,
		MacOS: 1
	},
	
	ICAPKIHostException: function (num, msg) {
		this.name = "Native host exception";
		this.number = num;
		this.description = msg;
	},

    DIR_CONNECT_EXTENSION: "connectExtension",
    DIR_HOST_UPDATE_STARTED: "upgrading",
    DIR_HOST_AND_AX_UPDATE_STARTED: "upgradingAx",
    DIR_EXTENSION_VERSION: "extensionVersion",
	DIR_EXTENSION_FORCE_DISCONNECT: "extensionForceDisconnect",

    DIROBJ_CONNECT_EXTENSION: function() {
        return {"type": this.m_messageTypeEnum.DIRECTIVE,"content": this.DIR_CONNECT_EXTENSION};
    },

    DIROBJ_EXTENSION_VERSION: function() {
        return {"type": this.m_messageTypeEnum.DIRECTIVE,"content": this.DIR_EXTENSION_VERSION};
    },
	
	Log: function (level, message) {
		switch (level) {
			case "log":
				if(ICAPKIService.loggingLevel >= 4) console.log(message);
				break;
			case "info":
				if(ICAPKIService.loggingLevel >= 3) console.info(message);
				break;
			case "warn":
				if(ICAPKIService.loggingLevel >= 2) console.warn(message);
				break;
			case "error":
				if(ICAPKIService.loggingLevel >= 1) console.error(message);
				break;
			default:
				if(ICAPKIService.loggingLevel >= 4) console.log(message);
				break;
		}
	},
	
	getExtensionOwnerFromURL: function() {
		var gul = function GetUrlParam( name, url ) {
			if (!url) url = location.href;
			name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
			var regexS = "[\\?&]"+name+"=([^&#]*)";
			var regex = new RegExp( regexS );
			var results = regex.exec( url );
			return results == null ? null : results[1];
		};

		return gul('extensionOwner',location.search);
	},
	
	Init: function(extensionsSettings) {
		var extensionOwner = ICAPKIService.getExtensionOwnerFromURL();
		
		if (extensionOwner && typeof(extensionsSettings) !== "undefined" && extensionsSettings.length !== 0) {
			for (let i = 0; i < extensionsSettings.length; i++) {
				if (extensionsSettings[i].name === extensionOwner) {
					ICAPKIService.extensionIDChrome = extensionsSettings[i].extensionIDChrome;
					ICAPKIService.extensionIDOpera = extensionsSettings[i].extensionIDOpera;
					ICAPKIService.extensionIDEdge = extensionsSettings[i].extensionIDEdge;
					ICAPKIService.extensionIDFirefox = extensionsSettings[i].extensionIDFirefox;
					ICAPKIService.extensionInstallURLFirefox = extensionsSettings[i].extensionInstallURLFirefox;
					break;
				}
			}
		}
		else if (typeof(extensionsSettings) !== "undefined" && extensionsSettings.length !== 0) {
			for (let i = 0; i < extensionsSettings.length; i++) {
				if (extensionsSettings[i].isDefault === true) {
					ICAPKIService.extensionIDChrome = extensionsSettings[i].extensionIDChrome;
					ICAPKIService.extensionIDOpera = extensionsSettings[i].extensionIDOpera;
					ICAPKIService.extensionIDEdge = extensionsSettings[i].extensionIDEdge;
					ICAPKIService.extensionIDFirefox = extensionsSettings[i].extensionIDFirefox;
					ICAPKIService.extensionInstallURLFirefox = extensionsSettings[i].extensionInstallURLFirefox;
					break;
				}
			}
		}
	},

    //extension only, return link to extension at browser store
    InstallExtension: function (onSuccess, onFailed) {
        if (ICAPKIService.m_curBrowser == ICAPKIService.m_browserEnum.CHROME) {
            var extensionChromeInstallURL = "https://chrome.google.com/webstore/detail/" + ICAPKIService.extensionIDChrome;
            window.open(extensionChromeInstallURL, '_blank');
        }
        else if (ICAPKIService.m_curBrowser == ICAPKIService.m_browserEnum.OPERA) {
			var extensionOperaInstallURL = "https://addons.opera.com/extensions/details/app_id/" + ICAPKIService.extensionIDOpera;
            window.open(extensionOperaInstallURL, '_blank');
        }
		else if (ICAPKIService.m_curBrowser == ICAPKIService.m_browserEnum.EDGEnew) {
			var extensionEdgeInstallURL = "https://microsoftedge.microsoft.com/addons/detail/" + ICAPKIService.extensionIDEdge;
            window.open(extensionEdgeInstallURL, '_blank');
        }
        else if (ICAPKIService.m_curBrowser == ICAPKIService.m_browserEnum.FIREFOX) {
			window.open(ICAPKIService.extensionInstallURLFirefox, '_blank');
        }
    },

    //extension only, return link to extension at browser store
    InstallExtensionFirefoxWebstoreURL: function () {
        return ICAPKIService.extensionInstallURLFirefox;
    },

    //extension only, connect browser extension to page
    ConnectExtension: function (cb) {
        var extensionId = ICAPKIService.extensionIDChrome;

        if(ICAPKIService.m_curBrowser == ICAPKIService.m_browserEnum.OPERA)
            extensionId = ICAPKIService.extensionIDOpera;
		
		if(ICAPKIService.m_curBrowser == ICAPKIService.m_browserEnum.EDGEnew)
            extensionId = ICAPKIService.extensionIDEdge;

        if(ICAPKIService.m_curBrowser == ICAPKIService.m_browserEnum.FIREFOX)
            extensionId = ICAPKIService.extensionIDFirefox;

        if(cb != undefined)
            ICAPKIService.m_extensionConnectedCallback = cb;

        if (ICAPKIService.m_port == null) {
            ICAPKIService.Log("log","ConnectExtension: Connection 'm_port == null', establishing connection to the background page of extension " + extensionId);

            if(ICAPKIService.m_curBrowser == ICAPKIService.m_browserEnum.FIREFOX) {
				
                if (!window.csConnectExtension(extensionId, ICAPKIService.onMessage, ICAPKIService.onExtensionDisconnect, ICAPKIService.DIROBJ_CONNECT_EXTENSION())) {
                    ICAPKIService.m_port = null;
                    return false;
                }
                ICAPKIService.m_port = true;
            }
            else {
                ICAPKIService.m_port = chrome.runtime.connect(extensionId);
                if (ICAPKIService.m_port == null) {
                    ICAPKIService.Log("error","ConnectExtension: Unable to establish connection");
                    return false;
                }
                ICAPKIService.m_port.onMessage.addListener(ICAPKIService.onMessage);
                ICAPKIService.m_port.onDisconnect.addListener(ICAPKIService.onExtensionDisconnect);
                ICAPKIService.m_port.postMessage(ICAPKIService.DIROBJ_CONNECT_EXTENSION());
            }
        }
        else {
            // ICAPKIService.Log("log","ConnectExtension: Connection 'm_port != null', connection to the background page of extension " + extensionId + " already established");
        }
        return true;
    },

    //ActiveX only, connect ActiveX to page
    ConnectActiveX: function(cb) {

        var acxObj = ICAPKIService.m_object;

        var onLoaded = function() {
            ICAPKIService.Log("log", "ConnectActiveX: connecting ActiveX component.");

            if(cb != undefined)
                ICAPKIService.m_extensionConnectedCallback = cb;

            var connectMsg = JSON.stringify(ICAPKIService.DIROBJ_CONNECT_EXTENSION());

            try {
                ICAPKIService.Log("log", "ConnectActiveX: sending message to connect ActiveX.");
                acxObj.SendMessage(connectMsg);
            }
            catch (ex) {
                ICAPKIService.Log("log", "ConnectActiveX: error on ActiveX.SendMessage(connectMsg). Exception message: " + ex.message);
            }
        };

        var state = acxObj.readyState;

        if(state != 4) {
            ICAPKIService.Log("error", "ConnectActiveX: object state: " + state);
        }
        else {
            ICAPKIService.Log("info", "ConnectActiveX: object state: " + state);
            onLoaded();
        }
    },
    
    //Safari App Extension only
    ConnectSafariAppExtension: function(cb) {

        ICAPKIService.Log("log", "ConnectSafariAppExtension: connecting Safari App Extension component.");

        try {	    	    
            if(cb != undefined)
                ICAPKIService.m_extensionConnectedCallback = cb;

	    ICAPKIService.m_responseObject.addEventListener("ICAPKIServiceResponse", function(event) {
		ICAPKIService.Log("log", "ConnectSafariAppExtension: ICAPKIServiceResponse handler." + JSON.stringify(event.detail));
		ICAPKIService.receiveSafariAppExtensionMessage(event.detail);
	    });
	    
            ICAPKIService.Log("log", "ConnectSafariAppExtension: sending message to connect plugin.");
            var connectMsg = JSON.stringify(ICAPKIService.DIROBJ_CONNECT_EXTENSION());
            ICAPKIService.sendSafariAppExtensionMessage(connectMsg);

        }
        catch (ex) {
            ICAPKIService.Log("error", "ConnectSafariAppExtension: error. Exception message: " + ex.message);
        }
    },

    QueryExtensionVersion: function(cb) {
        ICAPKIService.Log("log","QueryExtensionVersion: sending message to get extension version.");
        ICAPKIService.m_callbackMap[ICAPKIService.DIR_EXTENSION_VERSION] = cb;
        ICAPKIService.sendMessage(ICAPKIService.DIROBJ_EXTENSION_VERSION());
    },

    //extension only, callback when host (messaging app) disconnects from page
    onExtensionDisconnect: function () {
        ICAPKIService.Log("info","onExtensionDisconnect: Messaging disconnected from extension. Setting 'm_port = null'");
		
        ICAPKIService.m_port = null;
		
		if (ICAPKIService.GetObjectState() == 5) {
			//ICAPKIService.SetObjectState(1);
		}
		
		if (ICAPKIService.m_usedTech == ICAPKIService.m_techEnum.extension) {	
			ICAPKIService.Log("info", (ICAPKIService.m_forceDisconnect === true) ? 
				"forceDisconnected: true, reconnecting to extension" : "forceDisconnected: false, keeping dead");
			
			if (ICAPKIService.m_forceDisconnect === true) {
				ICAPKIService.ConnectExtension();
			}
		}
    },
	
	//Safari App Extension only
    m_commandObject: null,
    m_responseObject: null,
  
    //Safari App Extension only
    ConnectSafariAppExtension: function(cb) {
		ICAPKIService.Log("log", "ConnectSafariAppExtension: connecting Safari App Extension component.");
		
		try {               
			if(cb != undefined)
				ICAPKIService.m_extensionConnectedCallback = cb;
			
			ICAPKIService.m_responseObject.addEventListener("ICAPKIServiceResponse", function(event) {
				ICAPKIService.Log("log", "ConnectSafariAppExtension: ICAPKIServiceResponse handler." + JSON.stringify(event.detail));
				ICAPKIService.receiveSafariAppExtensionMessage(event.detail);
			});
        
			ICAPKIService.Log("log", "ConnectSafariAppExtension: sending message to connect plugin.");
			var connectMsg = JSON.stringify(ICAPKIService.DIROBJ_CONNECT_EXTENSION());
			ICAPKIService.sendSafariAppExtensionMessage(connectMsg);
		}
		catch (ex) {
			ICAPKIService.Log("error", "ConnectSafariAppExtension: error. Exception message: " + ex.message);
		}
	},

	//prepare message structure, encode message to base64 and call send function
    sendCallMessage: function (m, responseCallback) {
        if (m == undefined) {
            ICAPKIService.Log("error","sendCallMessage: Message to be send is empty!");
            return;
        }
        var msgB64 = ICAPKIService.encodeToBase64(JSON.stringify(m));
        var msgId = ICAPKIService.HashCode.value(msgB64);
        var wrappedMsg = {"type": ICAPKIService.m_messageTypeEnum.CALL, "id": msgId, "content": msgB64};

        ICAPKIService.Log("log","sendCallMessage: Registering msgId(hash)=" + msgId + " to the map of callbacks");
        ICAPKIService.Log("info","sendCallMessage: Decoded call content: " + JSON.stringify(m).substring(0, 10e6));
        ICAPKIService.m_callbackMap[msgId] = responseCallback;
        ICAPKIService.sendMessage(wrappedMsg);
    },

    sendConfirmMessage: function (id, length, number) {
        if (id == undefined || length == undefined || number == undefined) {
            ICAPKIService.Log("error","sendConfirmMessage: Do not know what to confirm!");
            return;
        }
        var wrappedMsg = {"type": ICAPKIService.m_messageTypeEnum.CONFIRM, "length": length, "number": number, "id": id};
        ICAPKIService.Log("log","sendConfirmMessage: Sending confirm of " + id + " with number: " + number + " out of: " + length);
        ICAPKIService.sendMessage(wrappedMsg);
    },

	//send message through extension/ActiveX/plugin to messaging native app
    sendMessage: function (m) {

        switch (ICAPKIService.m_usedTech) {
            case ICAPKIService.m_techEnum.extension:
                ICAPKIService.sendExtensionMessage(m);
                break;

            case ICAPKIService.m_techEnum.activeX:
                ICAPKIService.sendActiveXMessage(m);
                break;

            case ICAPKIService.m_techEnum.plugin:
                ICAPKIService.sendPluginMessage(m);
                break;
				
			case ICAPKIService.m_techEnum.safariAppExtension:
				ICAPKIService.sendSafariAppExtensionMessage(m);
				break;
        }
    },

    //extension only
    sendExtensionMessage: function (m) {

        if (!ICAPKIService.ConnectExtension()) {
            ICAPKIService.Log("error","sendExtensionMessage: No connection to the background page of extension");
            return;
        }

        if(ICAPKIService.m_curBrowser == ICAPKIService.m_browserEnum.FIREFOX) {
            ICAPKIService.Log("log","sendExtensionMessage: sending through content script");
            window.csPostMessage(m);
        }
        else {
            ICAPKIService.Log("info", "sendExtensionMessage: Sending wrapped message to the background page: " + JSON.stringify(m).substring(0, 10e6));
            ICAPKIService.m_port.postMessage(m);
        }
    },

    //ActiveX only
    sendActiveXMessage: function (m) {
        var strM = JSON.stringify(m);
        ICAPKIService.Log("info","sendMessage: Sending wrapped message to the ActiveX component: " + strM.substring(0, 10e6));
        this.m_object.SendMessage(strM);
    },

    //ActiveX only
    LoadActiveXPendingMessages: function() {
        ICAPKIService.Log("log", "LoadActiveXPendingMessages: started.");
        while (this.m_object != null && this.m_object.MessageAvailable() > 0) {
            var axcMsg = this.m_object.GetMessage();
            ICAPKIService.Log("log", "loadActiveXPendingMessages: received message from ActiveX component: " + axcMsg);
            var axcMsgObj = JSON.parse(axcMsg);
            ICAPKIService.onMessage(axcMsgObj);
        }
    },
	
	//Safari App Extension only
    sendSafariAppExtensionMessage: function(m) {
		var strM = JSON.stringify(m);
		ICAPKIService.Log("info","sendSafariAppExtensionMessage: Sending wrapped message to S.A.E: " + strM.substring(0, 10e6));
		var ev = new CustomEvent("ICAPKIServiceCommand", { detail: strM } );
		this.m_commandObject.dispatchEvent(ev);
	},

    //Safari App Extension only
    receiveSafariAppExtensionMessage: function(strM) {
		ICAPKIService.Log("info","receiveSafariAppExtensionMessage: Received wrapped message from S.A.E: " + strM);
		var ev = new CustomEvent("ICAPKIServiceCommand", { detail: strM } );
		var msgObj = JSON.parse(strM);
		ICAPKIService.onMessage(msgObj);
    },

	//called when new message arrives from host (messaging app)
    onMessage: function (m, sender, sendResponse) {
        if(ICAPKIService.m_curBrowser == ICAPKIService.m_browserEnum.FIREFOX) {
            m = window.contentMessage;
        }

        ICAPKIService.Log("info", "onMessage: Received message content: " + JSON.stringify(m));
        var msgId = m.id;
        var msgType = m.type;
        var msgLen = m.length
        var msgNum = m.number;

        if (msgLen <= 0 || msgNum <= 0 || msgLen < msgNum) {
            ICAPKIService.Log("error", "onMessage: wrong message 'length' or 'number'!");
            return;
        }

        try {
            switch (msgType) {
                case ICAPKIService.m_messageTypeEnum.RESPONSE:
                    if (msgLen == 1) {
                        var callback = ICAPKIService.getFncCallBackFromMap(msgId);

                        try {
                            var decodedContentString = ICAPKIService.decodeFromBase64(m.content);
                            ICAPKIService.Log("info", "onMessage: Decoded response content: " + decodedContentString);
                            var content = JSON.parse(decodedContentString);
							try {
								callback(content);
							}
							catch (ex) {
								ICAPKIService.Log("error", "onMessage: Uncaught error in callback function: " + ex.message);
							}
                        }
                        catch (ex) {
                            ICAPKIService.Log("error", "onMessage: Unable to decode the response message: " + ex.message);
                            ICAPKIService.m_throwHostExceptionCallback(-1, "Unable to decode Host response message from base64");
                        }
                    }
                    else {
                        if (ICAPKIService.m_messageBuffer[msgId] == undefined) {
                            ICAPKIService.m_messageBuffer[msgId] = new Array(0);
                            ICAPKIService.m_messageBuffer[msgId][0] = 0;
                        }

                        ICAPKIService.m_messageBuffer[msgId][msgNum] = m.content;
                        ICAPKIService.m_messageBuffer[msgId][0]++;

                        if (ICAPKIService.m_messageBuffer[msgId][0] == msgLen) {
                            var encodedContent = "";
                            for (var i = 1; i <= msgLen; i++) {
                                encodedContent += ICAPKIService.m_messageBuffer[msgId][i];
                            }
                            delete ICAPKIService.m_messageBuffer[msgId];
                            var callback = ICAPKIService.getFncCallBackFromMap(msgId);
                            try {
                                var decodedContentString = ICAPKIService.decodeFromBase64(encodedContent);
                                ICAPKIService.Log("info", "onMessage: Decoded response content: " + decodedContentString.substring(0, 10e6));
                                var content = JSON.parse(decodedContentString);
								try {
									callback(content);
								}
                                catch (ex) {
									ICAPKIService.Log("error", "onMessage: Uncaught error in callback function: " + ex.message);
								}
                            }
                            catch (ex) {
                                ICAPKIService.Log("error", "onMessage: Unable to decode the response message.");
                                ICAPKIService.m_throwHostExceptionCallback(-1, "Unable to decode Host response message from base64");
                            }
                        }
                    }
                    ICAPKIService.sendConfirmMessage(msgId, msgLen, msgNum);
                    break;

                case ICAPKIService.m_messageTypeEnum.ERROR:
                    var errorStr = ICAPKIService.decodeFromBase64(m.content);
                    ICAPKIService.Log("info", "onMessage: Decoded error content: " + errorStr);
                    ICAPKIService.m_throwHostExceptionCallback(m.number, errorStr);
                    break;

                case ICAPKIService.m_messageTypeEnum.CONFIRM:
                    break;

                case ICAPKIService.m_messageTypeEnum.DIRECTIVE:
                    var dirContent = m.content;

                    //DIR_CONNECT_EXTENSION
                    if (dirContent == ICAPKIService.DIR_CONNECT_EXTENSION) {
						//do not run extensionConnectedCallback when reconnecting to extension
                        if (ICAPKIService.m_forceDisconnect != true)
							ICAPKIService.m_extensionConnectedCallback();
						else {
							ICAPKIService.Log("info", "forceDisconnected: extension successfully reconnected");
							ICAPKIService.m_forceDisconnect = false;
						}
                    }
                    //DIR_HOST_UPDATE_STARTED
                    else if (dirContent == ICAPKIService.DIR_HOST_UPDATE_STARTED) {
                        ICAPKIService.m_hostUpdateStartedCallback(false);
                    }
                    //DIR_HOST_AND_AX_UPDATE_STARTED
                    else if (dirContent == ICAPKIService.DIR_HOST_AND_AX_UPDATE_STARTED) {
                        ICAPKIService.m_hostUpdateStartedCallback(true);
                    }
                    //DIR_EXTENSION_VERSION
                    else if (dirContent[ICAPKIService.DIR_EXTENSION_VERSION] != null) {
                        var callback = ICAPKIService.getFncCallBackFromMap(ICAPKIService.DIR_EXTENSION_VERSION);
                        callback(dirContent[ICAPKIService.DIR_EXTENSION_VERSION]);
                    }
					//DIR_EXTENSION_FORCE_DISCONNECT
					else if (dirContent[ICAPKIService.DIR_EXTENSION_FORCE_DISCONNECT] != null) {
                        ICAPKIService.m_forceDisconnect = true;
                    }
                    else {
                        ICAPKIService.Log("error", "onMessage: unknown directive message!");
                    }

                    break;

                default:
                    ICAPKIService.Log("error", "onMessage: unknown message type!");
                    break;
            }
        }
        catch (ex) {
            ICAPKIService.m_processHostExceptionCallback("", ex);
        }
    },

    getFncCallBackFromMap: function (msgId) {
        if (msgId == undefined) {
            ICAPKIService.Log("error","onMessage: Received message without 'id'");
            return null;
        }
        var callback = ICAPKIService.m_callbackMap[msgId];
        if (callback == undefined || callback == null) {
            ICAPKIService.Log("error","onMessage: The given msgId is not registered in the callback map. Id: " + msgId);
            return null;
        }
        else {
            ICAPKIService.Log("info","onMessage: Getting callback with Id: " + msgId);
            delete ICAPKIService.m_callbackMap[msgId];
            return callback;
        }
    },

    callMsgTemp: function () {
        this.library = ICAPKIService.libHost;
        this.function = {
            "name": "",
            "return": "int",
            "returnVal": "",
            "inParams": [],
            "inParamsVal": [],
            "outParams": []
        };
        var d = new Date();
        var timeStr = d.getHours() + ":" + d.getMinutes() + ":" + d.getSeconds() + "." + d.getMilliseconds();
        this.time = timeStr;
		//to decrease probability of collision when same messages are sent in the same time
		this.tokenNum = Math.floor(Math.random() * 10000);
    },

	//check returnCode of response from host (messaging native app), it should be equal to 0, otherwise throw exception
    checkHostResponse: function (fnName, rsp) {
        var result;
        var returnCode;
        if (rsp == undefined) {
            ICAPKIService.Log("error","checkResponse: response == 'undefined' of the function: '" + fnName + "'");
        }
        else {
            returnCode = rsp.function.returnVal;
            ICAPKIService.Log("info","checkResponse: 'returnCode' of '" + fnName + "'  == " + returnCode);
        }
        ICAPKIService.m_lastErrorNumber = returnCode;

        result = returnCode == 0;

        if (!result)
            ICAPKIService.m_throwHostExceptionCallback(returnCode, null, fnName);

        return result;
    },
	
	checkHostResponseAsync: function (fnName, rsp) {
        var result;
        var returnCode;
        if (rsp == undefined) {
            ICAPKIService.Log("error","checkResponse: response == 'undefined' of the function: '" + fnName + "'");
        }
        else {
            returnCode = rsp.function.returnVal;
            ICAPKIService.Log("info","checkResponse: 'returnCode' of '" + fnName + "'  == " + returnCode);
        }
        ICAPKIService.m_lastErrorNumber = returnCode;

        return returnCode;
    },

	//check returnCode of response from library (messaging native app), it should be equal to 0, otherwise throw exception
    checkResponse: function (fnName, rsp) {
        var result;
        var returnCode;
        var usedLibrary;
		
        if (rsp == undefined) {
            ICAPKIService.Log("error","checkResponse: response == 'undefined' of the function: '" + fnName + "'");
        }
        else {
			usedLibrary = rsp.library;
            returnCode = rsp.function.returnVal;
            ICAPKIService.Log("info","checkResponse: 'returnCode' of '" + fnName + "'  == " + returnCode);
        }
		
		if (typeof (ICAClientSign) !== "undefined" && usedLibrary == "ICAClientSign")
			ICAClientSign.m_lastErrorNumber = returnCode;

        result = returnCode == 0;

        if (!result)
            ICAPKIService.m_throwSignerExceptionCallback(returnCode, fnName, usedLibrary);

        return result;
    },
	
	checkResponseAsync: function (fnName, rsp) {
        var result;
        var returnCode;
        var usedLibrary;
		
        if (rsp == undefined) {
            ICAPKIService.Log("error","checkResponse: response == 'undefined' of the function: '" + fnName + "'");
        }
        else {
			usedLibrary = rsp.library;
            returnCode = rsp.function.returnVal;
            ICAPKIService.Log("info","checkResponse: 'returnCode' of '" + fnName + "'  == " + returnCode);
        }
		
		if (typeof (ICAClientSign) !== "undefined" && usedLibrary == "ICAClientSign")
			ICAClientSign.m_lastErrorNumber = returnCode;

        return returnCode;
    },

	//set expetion callbacks - exceptions from library
    setExceptionCallbacks: function (throwCb, proccessCb) {
        ICAPKIService.m_throwSignerExceptionCallback = throwCb;
        ICAPKIService.m_processSignerExceptionCallback = proccessCb;
    },

	//set exception callbacks - exceptions from host (native app)
    setHostExceptionCallbacks: function (throwCb, proccessCb) {
        ICAPKIService.m_throwHostExceptionCallback = throwCb;
        ICAPKIService.m_processHostExceptionCallback = proccessCb;
    },

    IsReady: function () {
        return ICAPKIService.m_objectState == 5;
    },

	//set state of object
    SetObjectState: function (newState) {
        ICAPKIService.m_objectState = newState;
        ICAPKIService.m_stateChangedCallback(newState);
    },
	
	//get state of object
    GetObjectState: function () {
        return ICAPKIService.m_objectState;
    },
	
	//------------------------HASH FUNCTION--------------------

	HashCode: function () {

		var serialize = function (object) {
			// Private
			var type, serializedCode = "";

			type = typeof object;

			if (type === 'object') {
				var element;

				for (element in object) {
					serializedCode += "[" + type + ":" + element + serialize(object[element]) + "]";
				}
			}
			else if (type === 'function') {
				serializedCode += "[" + type + ":" + object.toString() + "]";
			}
			else {
				serializedCode += "[" + type + ":" + object + "]";
			}

			return serializedCode.replace(/\s/g, "");
		};

		var makehash = function (input) {
			var hash = 0, i, chr, len;
			if (input.length == 0) return hash;
			
			for (i = 0, len = input.length; i < len; i++) {
				chr = input.charCodeAt(i);
				hash = ((hash << 5) - hash) + chr;
				hash |= 0; // Convert to 32bit integer
			}
			return Math.abs(hash);
		};

		// Public, API
		return {
			value: function (object) {
				return makehash(serialize(object));
			}
		};
	}(),

	decodeFromBase64: function (encoded) {
		try {
			return decodeURIComponent(escape(window.atob(encoded)));
		}
		catch (ex) {
			ICAPKIService.Log("error", "decodeFromBase64: Unable to decode message from base64.");
			ICAPKIService.m_throwHostExceptionCallback(-1, "Unable to decode message from base64");
		}
	},

	encodeToBase64: function (decoded) {
		return window.btoa(unescape(encodeURIComponent(decoded)));
	},

	decode_utf8: function (s) {
		return decodeURIComponent(escape(s));
	},

	//-----------------END OF--HASH FUNCTION-------------------
	
	paramExists: function (param) {
		return param !== undefined;
	}
};
