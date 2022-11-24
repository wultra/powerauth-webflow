// Web Flow is used for ICA configuration, automatic configuration is skipped

/*
function getExtensionOwnerFromURL() {

    var gul = function GetUrlParam( name, url ) {
        if (!url) url = location.href;
        name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
        var regexS = "[\\?&]"+name+"=([^&#]*)";
        var regex = new RegExp( regexS );
        var results = regex.exec( url );
        return results == null ? null : results[1];
    };

    return gul('extensionOwner',location.search);
}

var extensionOwner = getExtensionOwnerFromURL();

// extension ID I.CA
var extensionIDChrome = "fdolcjnejgbpoadihncaggiicpkhjchl";
var extensionIDOpera  = "lbdolmcpegiinogjamobnidmhoggcmho";
var extensionIDEdge = "kchhpancoebhkdgdafnifpkcacaopncp";
var extensionIDFirefox = "icapkiservice@ica.cz";
var extensionInstallURLFirefox = "https://addons.mozilla.org/firefox/addon/i-ca-pki-service-component/";

// change extension IDs depending on extensionOwner
switch (extensionOwner) {
	case "CSOB":
		extensionIDChrome = "eahecpanklnlonjjlojnjjcigcbflego";
		extensionIDOpera  = "onoplndjhnkihmalpbmlolgadonjpifh";
		extensionIDFirefox = "icapkiservice@csob.cz";
		extensionInstallURLFirefox = "https://www.csob.cz/portal/documents/10710/17949716/postup-instalace-firefox-extension.pdf";
		break;
	case "CSOBSR":
		extensionIDChrome = "keajcofpfbehkilblmcicgkdffacogbd";
		break;
	case "SBCZ":
		extensionIDChrome = "fgelgbhmdpeipcngganbpabhhccalapg";
		extensionIDOpera  = "emppaeenjldlghecpgkkfpmiglmplimn";
		extensionIDEdge = "lkdgplffifmmefehabdkmfeckeafcebl";
		extensionIDFirefox = "icapkiservice@sberbank.cz";
		extensionInstallURLFirefox = "https://www.sberbank.cz/-/media/files/sberbankcz/ostatni-dokumenty/cz/e-sign/postup-instalace-firefox-extension.pdf";
		break;
	case "CNB":
		extensionIDChrome = "hdbmbdpobhimeabanajlbkinlhkoflpk";
		extensionIDOpera  = "iaafkmelnilmbaipeiplbgpkamooffnk";
		extensionIDEdge = "hlaepfiibpjcgbjhnebnlninminjfchd";
		extensionIDFirefox = "icapkiservice@cnb.cz";
		extensionInstallURLFirefox = "https://abok.cnb.cz/aboks/login/postup-instalace-firefox-extension.pdf";
		break;
	case "FNPM":
		extensionIDChrome = "nbjmjacokmgdabnafjgnkifabbjmefbf";
		extensionIDEdge = "nbjmjacokmgdabnafjgnkifabbjmefbf";
		break;
	default:
		break;
}
*/

//------------------LOGGING OBJECT------------------------
var loggingLevel = 4; //0 - turned off, 1 - error, 2 - warning, 3 - info, 4 - log/debug

function Log(level, message) {
    switch (level) {
        case "log":
            if(loggingLevel >= 4) console.log(message);
            break;
        case "info":
            if(loggingLevel >= 3) console.info(message);
            break;
        case "warn":
            if(loggingLevel >= 2) console.warn(message);
            break;
        case "error":
            if(loggingLevel >= 1) console.error(message);
            break;
        default :
            if(loggingLevel >= 4) console.log(message);
            break;
    }
}

function paramExists (param) {
    return param !== undefined;
}

//-------------ICAPKISERVICE OBJECT------------------------
var ICAPKIService = {
    libHost: "ICAPKIServiceHost",

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

    DIR_CONNECT_EXTENSION: "connectExtension",
    DIR_HOST_UPDATE_STARTED: "upgrading",
    DIR_HOST_AND_AX_UPDATE_STARTED: "upgradingAx",
    DIR_EXTENSION_VERSION: "extensionVersion",

    DIROBJ_CONNECT_EXTENSION: function() {
        return {"type": this.m_messageTypeEnum.DIRECTIVE,"content": this.DIR_CONNECT_EXTENSION};
    },

    DIROBJ_EXTENSION_VERSION: function() {
        return {"type": this.m_messageTypeEnum.DIRECTIVE,"content": this.DIR_EXTENSION_VERSION};
    },

    //extension only, return link to extension at browser store
    InstallExtension: function (onSuccess, onFailed) {
        if (ICAPKIService.m_curBrowser == ICAPKIService.m_browserEnum.CHROME) {
            var extensionChromeInstallURL = "https://chrome.google.com/webstore/detail/" + extensionIDChrome;
            window.open(extensionChromeInstallURL, '_blank');
        }
        else if (ICAPKIService.m_curBrowser == ICAPKIService.m_browserEnum.OPERA) {
			var extensionOperaInstallURL = "https://addons.opera.com/extensions/details/app_id/" + extensionIDOpera;
            window.open(extensionOperaInstallURL, '_blank');
        }
		else if (ICAPKIService.m_curBrowser == ICAPKIService.m_browserEnum.EDGEnew) {
			var extensionEdgeInstallURL = "https://microsoftedge.microsoft.com/addons/detail/" + extensionIDEdge;
            window.open(extensionEdgeInstallURL, '_blank');
        }
        else if (ICAPKIService.m_curBrowser == ICAPKIService.m_browserEnum.FIREFOX) {
			window.open(extensionInstallURLFirefox, '_blank');
        }
    },

    //extension only, return link to extension at browser store
    InstallExtensionFirefoxWebstoreURL: function () {
        return extensionInstallURLFirefox;
    },

    //extension only, connect browser extension to page
    ConnectExtension: function (cb) {
        var extensionId = extensionIDChrome;

        if(ICAPKIService.m_curBrowser == ICAPKIService.m_browserEnum.OPERA)
            extensionId = extensionIDOpera;
		
		if(ICAPKIService.m_curBrowser == ICAPKIService.m_browserEnum.EDGEnew)
            extensionId = extensionIDEdge;

        if(ICAPKIService.m_curBrowser == ICAPKIService.m_browserEnum.FIREFOX)
            extensionId = extensionIDFirefox;

        if(cb != undefined)
            ICAPKIService.m_extensionConnectedCallback = cb;

        if (ICAPKIService.m_port == null) {
            Log("log","ConnectExtension: Connection 'm_port == null', establishing connection to the background page of extension " + extensionId);

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
                    Log("error","ConnectExtension: Unable to establish connection");
                    return false;
                }
                ICAPKIService.m_port.onMessage.addListener(ICAPKIService.onMessage);
                ICAPKIService.m_port.onDisconnect.addListener(ICAPKIService.onExtensionDisconnect);
                ICAPKIService.m_port.postMessage(ICAPKIService.DIROBJ_CONNECT_EXTENSION());
            }
        }
        else {
            // Log("log","ConnectExtension: Connection 'm_port != null', connection to the background page of extension " + extensionId + " already established");
        }
        return true;
    },

    //ActiveX only, connect ActiveX to page
    ConnectActiveX: function(cb) {

        var acxObj = ICAPKIService.m_object;

        var onLoaded = function() {
            Log("log", "ConnectActiveX: connecting ActiveX component.");

            if(cb != undefined)
                ICAPKIService.m_extensionConnectedCallback = cb;

            var connectMsg = JSON.stringify(ICAPKIService.DIROBJ_CONNECT_EXTENSION());

            try {
                Log("log", "ConnectActiveX: sending message to connect ActiveX.");
                acxObj.SendMessage(connectMsg);
            }
            catch (ex) {
                Log("log", "ConnectActiveX: error on ActiveX.SendMessage(connectMsg). Exception message: " + ex.message);
            }
        };

        var state = acxObj.readyState;

        if(state != 4) {
            Log("error", "ConnectActiveX: object state: " + state);
        }
        else {
            Log("info", "ConnectActiveX: object state: " + state);
            onLoaded();
        }
    },

    //plugin only, connect plugin to page
    ConnectPlugin: function(cb) {

        Log("log", "ConnectPlugin: connecting NPAPI plugin component.");

        function addEvent(obj, name, func)
        {
            if (obj.attachEvent) {
                obj.attachEvent("on"+name, func);
            } else {
                obj.addEventListener(name, func, false);
            }
        }
        try {
            var plugObj = ICAPKIService.m_object;

            if(cb != undefined)
                ICAPKIService.m_extensionConnectedCallback = cb;

            addEvent(plugObj, "PluginDisconnected", ICAPKIService.onPluginDisconnect);
            addEvent(plugObj, "PluginMessageReady", ICAPKIService.LoadPluginPendingMessages);

            Log("log", "ConnectPlugin: sending message to connect plugin.");
            var connectMsg = JSON.stringify(ICAPKIService.DIROBJ_CONNECT_EXTENSION());
            plugObj.SendPluginMessage(connectMsg);
        }
        catch (ex) {
            Log("log", "ConnectPlugin: error on Plugin.SendPluginMessage(connectMsg). Exception message: " + ex.message);
        }
    },

    QueryExtensionVersion: function(cb) {
        Log("log","QueryExtensionVersion: sending message to get extension version.");
        ICAPKIService.m_callbackMap[ICAPKIService.DIR_EXTENSION_VERSION] = cb;
        ICAPKIService.sendMessage(ICAPKIService.DIROBJ_EXTENSION_VERSION());
    },

    //extension only, callback when host (messaging app) disconnects from page
    onExtensionDisconnect: function () {
        Log("info","onExtensionDisconnect: Messaging disconnected from extension. Setting 'm_port = null'");
        ICAPKIService.m_port = null;
		
		if (ICAPKIService.GetObjectState() == 5) {
			//ICAPKIService.SetObjectState(1);
		}
    },

    //plugin only, callback when host (messaging app) disconnects from page
    onPluginDisconnect: function () {
        Log("info","onPluginDisconnect: Messaging disconnected from plugin.");
        
		if (ICAPKIService.GetObjectState() == 5) {
			//ICAPKIService.SetObjectState(1);
		}
    },
	
	//Safari App Extension only
    m_commandObject: null,
    m_responseObject: null,
  
    //Safari App Extension only
    ConnectSafariAppExtension: function(cb) {
		Log("log", "ConnectSafariAppExtension: connecting Safari App Extension component.");
		
		try {               
			if(cb != undefined)
				ICAPKIService.m_extensionConnectedCallback = cb;
			
			ICAPKIService.m_responseObject.addEventListener("ICAPKIServiceResponse", function(event) {
				Log("log", "ConnectSafariAppExtension: ICAPKIServiceResponse handler." + JSON.stringify(event.detail));
				ICAPKIService.receiveSafariAppExtensionMessage(event.detail);
			});
        
			Log("log", "ConnectSafariAppExtension: sending message to connect plugin.");
			var connectMsg = JSON.stringify(ICAPKIService.DIROBJ_CONNECT_EXTENSION());
			ICAPKIService.sendSafariAppExtensionMessage(connectMsg);
		}
		catch (ex) {
			Log("error", "ConnectSafariAppExtension: error. Exception message: " + ex.message);
		}
	},

	//prepare message structure, encode message to base64 and call send function
    sendCallMessage: function (m, responseCallback) {
        if (m == undefined) {
            Log("error","sendCallMessage: Message to be send is empty!");
            return;
        }
        var msgB64 = encodeToBase64(JSON.stringify(m));
        var msgId = HashCode.value(msgB64);
        var wrappedMsg = {"type": ICAPKIService.m_messageTypeEnum.CALL, "id": msgId, "content": msgB64};

        Log("log","sendCallMessage: Registering msgId(hash)=" + msgId + " to the map of callbacks");
        Log("info","sendCallMessage: Decoded call content: " + JSON.stringify(m));
        ICAPKIService.m_callbackMap[msgId] = responseCallback;
        ICAPKIService.sendMessage(wrappedMsg);
    },

    sendConfirmMessage: function (id, length, number) {
        if (id == undefined || length == undefined || number == undefined) {
            Log("error","sendConfirmMessage: Do not know what to confirm!");
            return;
        }
        var wrappedMsg = {"type": ICAPKIService.m_messageTypeEnum.CONFIRM, "length": length, "number": number, "id": id};
        Log("log","sendConfirmMessage: Sending confirm of " + id + " with number: " + number + " out of: " + length);
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
            Log("error","sendExtensionMessage: No connection to the background page of extension");
            return;
        }

        if(ICAPKIService.m_curBrowser == ICAPKIService.m_browserEnum.FIREFOX) {
            Log("log","sendExtensionMessage: sending through content script");
            window.csPostMessage(m);
        }
        else {
            Log("info", "sendExtensionMessage: Sending wrapped message to the background page: " + JSON.stringify(m));
            ICAPKIService.m_port.postMessage(m);
        }
    },

    //ActiveX only
    sendActiveXMessage: function (m) {

        var strM = JSON.stringify(m);
        Log("info","sendMessage: Sending wrapped message to the ActiveX component: " + strM);
        this.m_object.SendMessage(strM);
    },

    //plugin only
    sendPluginMessage: function(m) {

        var strM = JSON.stringify(m);
        Log("info","sendMessage: Sending wrapped message to the NPAPI plugin: " + strM);
        this.m_object.SendPluginMessage(strM);
    },

    //ActiveX only
    LoadActiveXPendingMessages: function() {
        Log("log", "LoadActiveXPendingMessages: started.");
        while (this.m_object != null && this.m_object.MessageAvailable() > 0) {
            var axcMsg = this.m_object.GetMessage();
            Log("log", "loadActiveXPendingMessages: received message from ActiveX component: " + axcMsg);
            var axcMsgObj = JSON.parse(axcMsg);
            ICAPKIService.onMessage(axcMsgObj);
        }
    },

    //plugin only
    LoadPluginPendingMessages: function() {
        Log("log", "LoadPluginPendingMessages: started.");
        while (ICAPKIService.m_object != null && ICAPKIService.m_object.PluginMessageAvailable > 0) {
            var plugMsg = ICAPKIService.m_object.GetPluginMessage;
            Log("log", "LoadPluginPendingMessages: received message from NPAPI plugin: " + plugMsg);
            var plugMsgObj = JSON.parse(plugMsg);
            ICAPKIService.onMessage(plugMsgObj);
        }
    },
	
	//Safari App Extension only
    sendSafariAppExtensionMessage: function(m) {
		var strM = JSON.stringify(m);
		Log("info","sendSafariAppExtensionMessage: Sending wrapped message to S.A.E: " + strM);

		var ev = new CustomEvent("ICAPKIServiceCommand", { detail: strM } );
		this.m_commandObject.dispatchEvent(ev);
	},

    //Safari App Extension only
    receiveSafariAppExtensionMessage: function(strM) {
		Log("info","receiveSafariAppExtensionMessage: Received wrapped message from S.A.E: " + strM);
		var ev = new CustomEvent("ICAPKIServiceCommand", { detail: strM } );
		var msgObj = JSON.parse(strM);
		ICAPKIService.onMessage(msgObj);
    },

	//called when new message arrives from host (messaging app)
    onMessage: function (m, sender, sendResponse) {
        if(ICAPKIService.m_curBrowser == ICAPKIService.m_browserEnum.FIREFOX) {
            m = window.contentMessage;
        }

        Log("info", "onMessage: Received message content: " + JSON.stringify(m));
        var msgId = m.id;
        var msgType = m.type;
        var msgLen = m.length
        var msgNum = m.number;

        if (msgLen <= 0 || msgNum <= 0 || msgLen < msgNum) {
            Log("error", "onMessage: wrong message 'length' or 'number'!");
            return;
        }

        try {
            switch (msgType) {
                case ICAPKIService.m_messageTypeEnum.RESPONSE:
                    if (msgLen == 1) {
                        var callback = ICAPKIService.getFncCallBackFromMap(msgId);

                        try {
                            var decodedContentString = decodeFromBase64(m.content);
                            Log("info", "onMessage: Decoded response content: " + decodedContentString);
                            var content = JSON.parse(decodedContentString);
                            callback(content);
                        }
                        catch (ex) {
                            Log("error", "onMessage: Unable to decode the response message.");
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
                                var decodedContentString = decodeFromBase64(encodedContent);
                                Log("info", "onMessage: Decoded response content: " + decodedContentString);
                                var content = JSON.parse(decodedContentString);
                                callback(content);
                            }
                            catch (ex) {
                                Log("error", "onMessage: Unable to decode the response message.");
                                ICAPKIService.m_throwHostExceptionCallback(-1, "Unable to decode Host response message from base64");
                            }
                        }
                    }
                    ICAPKIService.sendConfirmMessage(msgId, msgLen, msgNum);
                    break;

                case ICAPKIService.m_messageTypeEnum.ERROR:
                    var errorStr = decodeFromBase64(m.content);
                    Log("info", "onMessage: Decoded error content: " + errorStr);
                    ICAPKIService.m_throwHostExceptionCallback(m.number, errorStr);
                    break;

                case ICAPKIService.m_messageTypeEnum.CONFIRM:
                    break;

                case ICAPKIService.m_messageTypeEnum.DIRECTIVE:
                    var dirContent = m.content;

                    //DIR_CONNECT_EXTENSION
                    if (dirContent == ICAPKIService.DIR_CONNECT_EXTENSION) {
                        ICAPKIService.m_extensionConnectedCallback();
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
                    else {
                        Log("error", "onMessage: unknown directive message!");
                    }

                    break;

                default:
                    Log("error", "onMessage: unknown message type!");
                    break;
            }
        }
        catch (ex) {
            ICAPKIService.m_processHostExceptionCallback("", ex);
        }
    },

    getFncCallBackFromMap: function (msgId) {
        if (msgId == undefined) {
            Log("error","onMessage: Received message without 'id'");
            return null;
        }
        var callback = ICAPKIService.m_callbackMap[msgId];
        if (callback == undefined || callback == null) {
            Log("error","onMessage: The given msgId is not registered in the callback map. Id: " + msgId);
            return null;
        }
        else {
            Log("info","onMessage: Getting callback with Id: " + msgId);
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
    },

	//check returnCode of response from host (messaging native app), it should be equal to 0, otherwise throw exception
    checkHostResponse: function (fnName, rsp) {
        var result;
        var returnCode;
        if (rsp == undefined) {
            Log("error","checkResponse: response == 'undefined' of the function: '" + fnName + "'");
        }
        else {
            returnCode = rsp.function.returnVal;
            Log("info","checkResponse: 'returnCode' of '" + fnName + "'  == " + returnCode);
        }
        ICAPKIService.m_lastErrorNumber = returnCode;

        result = returnCode == 0;

        if (!result)
            ICAPKIService.m_throwHostExceptionCallback(returnCode, null, fnName);

        return result;
    },

	//check returnCode of response from library (messaging native app), it should be equal to 0, otherwise throw exception
    checkResponse: function (fnName, rsp) {
        var result;
        var returnCode;
        if (rsp == undefined) {
            Log("error","checkResponse: response == 'undefined' of the function: '" + fnName + "'");
        }
        else {
            returnCode = rsp.function.returnVal;
            Log("info","checkResponse: 'returnCode' of '" + fnName + "'  == " + returnCode);
        }
		
		if (typeof (ICAClientSign) !== "undefined")
			ICAClientSign.m_lastErrorNumber = returnCode;

        result = returnCode == 0;

        if (!result)
            ICAPKIService.m_throwSignerExceptionCallback(returnCode, fnName);

        return result;
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
    }
};

//------------------------HASH FUNCTION--------------------

var HashCode = function () {

    var serialize = function (object) {
        // Private
        var type, serializedCode = "";

        type = typeof object;

        if (type === 'object') {
            var element;

            for (element in object) {
                serializedCode += "[" + type + ":" + element + serialize(object[element]) + "]";
            }

        } else if (type === 'function') {
            serializedCode += "[" + type + ":" + object.toString() + "]";
        } else {
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
}();

function decodeFromBase64(encoded) {
    try {
        return decodeURIComponent(escape(window.atob(encoded)));
    }
    catch (ex) {
        Log("error", "decodeFromBase64: Unable to decode message from base64.");
        ICAPKIService.m_throwHostExceptionCallback(-1, "Unable to decode message from base64");
    }
}

function encodeToBase64(decoded) {
    return window.btoa(unescape(encodeURIComponent(decoded)));
}

function decode_utf8(s) {
    return decodeURIComponent(escape(s));
}

//-----------------END OF--HASH FUNCTION-------------------
