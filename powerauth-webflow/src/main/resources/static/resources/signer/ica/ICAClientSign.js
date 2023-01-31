/// ICAClientSign.js
/// file version 3.0.0.0

/// Intended to use with native libraries:
/// ICAClientSignExt v5.1.0.0

//----------ICACLIENTSIGN OBJECT-------------------------------

var ICAClientSign = {
  libICAClientSign: "ICAClientSign",
  configURL: "ICAClientSignExt_v5100.xml.icf",
  
  Init: function (configURL) {
	  if (configURL)
		  ICAClientSign.configURL = configURL;
  },

  libICAClientSignURL: function () {
      return ICAClientSign.configURL;
  }
  /* Commented out broken code from ICA which does not work on localhost with different context than expected.
    function getEnvironmentFromURL() {
      let url = window.location.href;
      let domain;
      //find & remove protocol (http, ftp, etc.) and get domain
      if (url.indexOf("://") > -1) domain = url.split("/")[2];
      else domain = url.split("/")[0];

      //find & remove port number
      domain = domain.split(":")[0];

      let domainEndsWith = function (domain, endsWithStr) {
        return (
          domain.indexOf(endsWithStr, domain.length - endsWithStr.length) !== -1
        );
      };

      if (domainEndsWith(domain, "ica.cz")) {
		  if (domain === "ca.ica.cz")
			  return "ca";
		  else
			  return "ica";
	  }
      else if (domain.substring(0) === "localhost")
		  return "localhost";
      else
		  return "customer";
    }

    let environment = getEnvironmentFromURL();

    switch (environment) {
      case "localhost":
	    return (window.location.protocol + "//" + window.location.host + "/pkiservice/" + ICAClientSign.configURL);
        break;
      case "ica":
        return (window.location.protocol + "//" + window.location.hostname + "/ica_pkiservice/" + ICAClientSign.configURL);
        break;
      case "ca":
        return (window.location.protocol + "//" + window.location.hostname + "/pub/ICAPKIService/ICAClientSign/pkiservice/" + ICAClientSign.configURL);
        break;
      default:
        return ICAClientSign.configURL;
    }
  }*/,

  m_langEnum: {
    CZ: 0,
    SK: 1,
    EN: 2,
  },

  m_curLanguage: 0,
  m_lastErrorNumber: 0,

  callMsgTemp: function () {
    let temp = new ICAPKIService.callMsgTemp();
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
  CERTLOAD_SHA256_SUPPORTED_FLAG: 64,

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
  CERTVAL_IK_MPSV: 13,
  CERTVAL_CHECK_SHA256_SUPPORT: 14,
  
  ICAClientSignException: function (num, msg) {
	this.name = "Signer exception";
	this.number = num;
	this.description = msg;
  },
  
  SendProcessResponseOutput: function (resolve, reject, msg) {
	ICAPKIService.sendCallMessage(msg, function (rsp) {
		Promise.resolve(ICAPKIService.checkResponseAsync(msg.function.name + "()", rsp))
			.then(function(returnCode) {
				if (returnCode !== 0)
					throw returnCode;
			
				const outParamsCount = msg.function.outParams.length;
				if (outParamsCount > 1) {
					let arrOutput = [];
					for (let ii = 0; ii < outParamsCount; ii++) {
						arrOutput.push(rsp.function.outParamsVal[ii]);
					}
					resolve(arrOutput);
				}
				else if (outParamsCount === 1) {
					const output = rsp.function.outParamsVal[0];
					resolve(output);
				}
				else {
					resolve(returnCode);
				}
		}).catch(function(returnCode) {
			Promise.resolve(ICAClientSign.getErrorMessageAsync(returnCode))
				.then(function(errorMsg) {
					reject(new ICAClientSign.ICAClientSignException(returnCode, errorMsg));
				}).catch(function(ex) {
					reject(new ICAClientSign.ICAClientSignException(returnCode, ex.description));
				});
		});
	});
  },
  
  SendProcessHostResponseOutput: function (resolve, reject, msg) {
	ICAPKIService.sendCallMessage(msg, function (rsp) {
		Promise.resolve(ICAPKIService.checkHostResponseAsync(msg.function.name + "()", rsp))
			.then(function(returnCode) {
				if (returnCode === 0) {
					let output = rsp.function.outParamsVal[0];
					resolve(output);
				}
				else {
					reject(new ICAPKIService.ICAPKIHostException(returnCode, "Error occured during calling " + msg.function.name + "()"));
				}
			});
	});
  },

  //----------ICACLIENTSIGN LIBRARY FUNCTIONS----------------

  //initialize native messaging app
  InitializeHost: function (cb) {
    ICAPKIService.Log("log", "InitializeHost(): start");
    var msg = new ICAPKIService.callMsgTemp();
    msg.function.name = "InitHost";
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      if (ICAPKIService.checkHostResponse("InitializeHost()", rsp)) {
        ICAPKIService.SetObjectState(3); //host installed state
        cb();
      }
    });
  },
  
  InitializeHostAsync: function () {
	  return new Promise(function (resolve, reject) {
		ICAPKIService.Log("log", "InitializeHost(): start");
		let msg = new ICAPKIService.callMsgTemp();
		msg.function.name = "InitHost";
		
		ICAPKIService.sendCallMessage(msg, function (rsp) {
			if (typeof rsp === "undefined") {
				reject(new ICAPKIService.ICAPKIHostException(-1, "Error occured during calling InitializeHost()"));
			}
			else {
				Promise.resolve(ICAPKIService.checkHostResponseAsync("InitializeHost()", rsp))
					.then(function(returnCode) {
						if (returnCode === 0) {
							ICAPKIService.SetObjectState(3); //host installed state
							resolve(returnCode);
						}
						else {
							reject(new ICAPKIService.ICAPKIHostException(returnCode, "Error occured during calling InitializeHost()"));
						}
					});
			}
		});
	  });
  },

  //download ICF config file, download files of library (or check file present on disk), cbInit = callback code run after successfully loaded libraries
  LoadLibrary: function (cbInit) {
    ICAPKIService.Log("log", "LoadLibrary(): start");

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
  
  LoadLibraryAsync: function () {
	  return new Promise(function (resolve, reject) {
		ICAPKIService.Log("log", "LoadLibrary(): start");

		let uninitMsg = new ICAClientSign.callMsgTemp();
		uninitMsg.function.name = "IcsxFinalizeLibrary";

		let msg = new ICAPKIService.callMsgTemp();
		msg.function.name = "LoadLibrary";
		msg.function.inParams = ["const char[]", "const char[]", "char[#4]", "int*", "const char[]", "const char[]"];
		msg.function.inParamsVal = [ICAClientSign.libICAClientSign, ICAClientSign.libICAClientSignURL(), null, 260, JSON.stringify(uninitMsg), "IcsxFreeBuffer"];
		msg.function.outParams = ["#3 char[#4] string"];
		
		ICAPKIService.sendCallMessage(msg, function (rsp) {
			if (typeof rsp === "undefined") {
				reject(new ICAPKIService.ICAPKIHostException(-1, "Error occured during calling LoadLibrary()"));
			}
			else {
				Promise.resolve(ICAPKIService.checkHostResponseAsync("LoadLibrary()", rsp))
					.then(function(returnCode) {
						if (returnCode === 0) {
							let libFolderPath = rsp.function.outParamsVal[0];
							ICAPKIService.SetObjectState(4); //host installed state
							resolve(libFolderPath);
						}
						else {
							reject(new ICAPKIService.ICAPKIHostException(returnCode, "Error occured during calling LoadLibrary()"));
						}
					});
			}
		});
    });
  },

  //ICAClientSign - initialize library
  Initialize: function (cb) {
    var cbInit = function cbInit(libraryFolderPath) {
	  var logPath = libraryFolderPath.match(/.*(PKIService).*?(\\|\/)/)[0] + "logicaclientsign.txt";
      ICAPKIService.Log("log", "Initialize: start");
      var msg = new ICAClientSign.callMsgTemp();
      msg.function.name = "IcsxInitializeLibrary";
      msg.function.inParams = ["const char[]", "const char[]"];
      msg.function.inParamsVal = [libraryFolderPath, logPath];
      ICAPKIService.sendCallMessage(msg, function (rsp) {
        if (ICAPKIService.checkResponse("IcsxInitializeLibrary()", rsp)) {
          ICAPKIService.SetObjectState(5); //ready
          if (ICAPKIService.paramExists(cb)) cb();
        }
      });
    };

    ICAClientSign.LoadLibrary(cbInit);
  },
  
  InitializeAsync: function () {
	return new Promise(function (resolve, reject) {
		Promise.resolve(ICAClientSign.LoadLibraryAsync())
			.then(function(libraryFolderPath) {
				const logPath = libraryFolderPath.match(/.*(PKIService).*?(\\|\/)/)[0] + "logicaclientsign.txt";
				ICAPKIService.Log("log", "Initialize: start");
				let msg = new ICAClientSign.callMsgTemp();
				msg.function.name = "IcsxInitializeLibrary";
				msg.function.inParams = ["const char[]", "const char[]"];
				msg.function.inParamsVal = [libraryFolderPath, logPath];
		
				ICAPKIService.sendCallMessage(msg, function (rsp) {
					Promise.resolve(ICAPKIService.checkResponseAsync("IcsxInitializeLibrary()", rsp))
						.then(function(returnCode) {
							if (returnCode === 0) {
								ICAPKIService.SetObjectState(5); //ready
								resolve(returnCode);
							}
							else {
								reject(new ICAClientSign.ICAClientSignException(returnCode, "Error occured during calling IcsxInitializeLibrary()"));
							}
						});
				});
			})	
			.catch(function(ex) {
				reject(new ICAPKIService.ICAPKIHostException(ex.number, ex.description));
			});
	});
  },
  
  //get text message for error number, exception in native host app
  GetHostErrorString: function (num, cb, funcName) {
    var fnName = "GetHostErrorString";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  GetHostErrorStringAsync: function (num) {
	return new Promise(function (resolve, reject) {
		const fnName = "GetHostErrorString";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAPKIService.callMsgTemp();
		msg.function.name = "GetHostErrorString";
		msg.function.inParams = ["int", "char[#3]", "int*"];
		msg.function.inParamsVal = [num, null, 1000];
		msg.function.outParams = ["#2 char[#3] string"];
		
		ICAPKIService.sendCallMessage(msg, function (rsp) {
			if (typeof rsp === "undefined") {
				reject(new ICAPKIService.ICAPKIHostException(-1, "Error occured during calling GetHostErrorString()"));
			}
			else {
				Promise.resolve(ICAPKIService.checkHostResponseAsync("GetHostErrorString()", rsp))
					.then(function(returnCode) {
						if (returnCode === 0) {
							let str = rsp.function.outParamsVal[0];
							resolve(str);
						}
						else {
							reject(new ICAPKIService.ICAPKIHostException(returnCode, "Error occured during calling GetHostErrorString()"));
						}
					});
			}
		});
	})
  },
  
  //ICAClientSign - get last error number
  getLastErrorNumber: function () {
	return ICAClientSign.m_lastErrorNumber;  
  },

  //ICAClientSign - get text message for last error
  getErrorMessage: function (cb, num, funcName) {
    var fnName = "getErrorMessage";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  getErrorMessageAsync: function (num) {
	return new Promise(function (resolve, reject) {
		var fnName = "getErrorMessage";
		ICAPKIService.Log("log", fnName + ": start");
		var msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxGetErrorMessage";
		msg.function.inParams = ["long", "int", "char**"];
		msg.function.inParamsVal = [num, ICAClientSign.m_curLanguage];
		msg.function.outParams = ["#3 char[]* string"];
		
		ICAPKIService.sendCallMessage(msg, function (rsp) {
			if (typeof rsp === "undefined") {
				reject(new ICAClientSign.ICAClientSignException(-1, "Error occured during calling IcsxGetErrorMessage()"));
			}
			else {
				Promise.resolve(ICAPKIService.checkResponseAsync("IcsxGetErrorMessage()", rsp))
					.then(function(returnCode) {
						if (returnCode === 0) {
							let str = rsp.function.outParamsVal[0];
							resolve(str);
						}
						else {
							reject(new ICAClientSign.ICAClientSignException(returnCode, "Error occured during calling IcsxGetErrorMessage()"));
						}
					});
			}
		});
	})
  },
  
  //ICAClientSign - library version info
  getAbout: function (cb) {
    ICAPKIService.Log("log", "getAbout: start");
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
  
  getAboutAsync: function () {
	return new Promise(function (resolve, reject) {
		ICAPKIService.Log("log", "getAbout: start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxGetAbout";
		msg.function.inParams = ["char**"];
		msg.function.outParams = ["#1 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - clear log
  logClear: function (cb) {
    var fnName = "LogClear";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxLogClear";
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      var stat = ICAPKIService.checkResponse("IcsxLogClear()", rsp);
      cb(stat);
    });
  },
  
  logClearAsync: function () {
	return new Promise(function (resolve, reject) {
		const fnName = "LogClear";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxLogClear";
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - get log
  logGetContent: function (cb) {
    var fnName = "logGetContent";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  logGetContentAsync: function () {
	return new Promise(function (resolve, reject) {
		const fnName = "logGetContent";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxLogGetContent";
		msg.function.inParams = ["char**"];
		msg.function.outParams = ["#1 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - obtain a specific value from the certificate - eg. SN, SUBJECT
  certificateGetValue: function (cb, partText, certIndex, pem, partEnum) {
    var fnName = "certificateGetValue";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  certificateGetValueAsync: function (pem, partEnum) {
	return new Promise(function (resolve, reject) {
		const fnName = "certificateGetValue";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxCertificateGetValue";
		msg.function.inParams = ["char[]", "int", "char**"];
		msg.function.inParamsVal = [pem, partEnum];
		msg.function.outParams = ["#3 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - returns PEM of certificate for requested index
  //deprecated
  certificateEnumerateStore: function (cb, index) {
    var fnName = "certificateEnumerateStore";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  certificateEnumerateStoreAsync: function (index) {
	return new Promise(function (resolve, reject) {
		const fnName = "certificateEnumerateStore";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxCertificateEnumerateStore";
		msg.function.inParams = ["int", "char**"];
		msg.function.inParamsVal = [index];
		msg.function.outParams = ["#2 char[]* string"];
		
		ICAPKIService.sendCallMessage(msg, function (rsp) {
			Promise.resolve(ICAPKIService.checkResponseAsync("IcsxCertificateEnumerateStore()", rsp))
				.then(function(returnCode) {
					if (returnCode === 0) {
						let pem = rsp.function.outParamsVal[0];
						resolve([index, pem]);
					}
					else {
						Promise.resolve(ICAClientSign.getErrorMessageAsync(returnCode))
							.then(function(errorMsg) {
								reject(new ICAClientSign.ICAClientSignException(returnCode, errorMsg));
							})
							.catch(function(ex) {
								reject(new ICAClientSign.ICAClientSignException(returnCode, ex.description));
						});
					}
				});
		});
	})
  },

  //ICAClientSign - returns the number of certificates in storage (Windows or I.CA smart card)
  //deprecated
  certificateLoadUserKeyStore: function (cb, cardStore, flags) {
    var fnName = "certificateLoadUserKeyStore";
    ICAPKIService.Log("log", fnName + ": start");

    cardStore = cardStore ? 1 : 0;
    flags = ICAPKIService.paramExists(flags)
      ? flags
      : ICAClientSign.CERTLOAD_SIGNING_FLAG |
        ICAClientSign.CERTLOAD_QUALIFIED_FLAG;

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
  
  certificateLoadUserKeyStoreAsync: function (cardStore, flags) {
	return new Promise(function (resolve, reject) {
		const fnName = "certificateLoadUserKeyStore";
		ICAPKIService.Log("log", fnName + ": start");

		cardStore = cardStore ? 1 : 0;
		flags = ICAPKIService.paramExists(flags)
			? flags
			: ICAClientSign.CERTLOAD_SIGNING_FLAG |
			  ICAClientSign.CERTLOAD_QUALIFIED_FLAG;

		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxCertificateLoadUserKeyStore";
		msg.function.inParams = ["int", "unsigned long*", "unsigned long"];
		msg.function.inParamsVal = [cardStore, null, flags];
		msg.function.outParams = ["#2 unsigned long* int"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - returns the number of certificates in storage (Windows or I.CA smart card)
  //deprecated
  certificateLoadUserKeyStoreSN: function (cb, cardStore, serialNumbers, hexadecimal) {
    var fnName = "certificateLoadUserKeyStoreSN";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  certificateLoadUserKeyStoreSNAsync: function (cardStore, serialNumbers, hexadecimal) {
	return new Promise(function (resolve, reject) {
		const fnName = "certificateLoadUserKeyStoreSN";
		ICAPKIService.Log("log", fnName + ": start");
		cardStore = cardStore ? 1 : 0;
		hexadecimal = hexadecimal ? 1 : 0;
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxCertificateLoadUserKeyStoreSN";
		msg.function.inParams = ["int", "const char[]", "int", "unsigned long*"];
		msg.function.inParamsVal = [cardStore, serialNumbers, hexadecimal];
		msg.function.outParams = ["#4 unsigned long* int"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - returns the number of certificates in storage (Windows or I.CA smart card)
  //deprecated
  certificateLoadUserKeyStoreJSON: function (cb, cardStore, jsonFilter, flags) {
    var fnName = "certificateLoadUserKeyStoreJSON";
    ICAPKIService.Log("log", fnName + ": start");

    cardStore = cardStore ? 1 : 0;
    flags = ICAPKIService.paramExists(flags)
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
  
  certificateLoadUserKeyStoreJSONAsync: function (cardStore, jsonFilter, flags) {
	return new Promise(function (resolve, reject) {
		const fnName = "certificateLoadUserKeyStoreJSON";
		ICAPKIService.Log("log", fnName + ": start");

		cardStore = cardStore ? 1 : 0;
		flags = ICAPKIService.paramExists(flags)
			? flags
			: ICAClientSign.CERTLOAD_SIGNING_FLAG |
			  ICAClientSign.CERTLOAD_QSCD_QUALIFIED_FLAG;

		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxCertificateLoadUserKeyStoreJSON";
		msg.function.inParams = ["int", "const char[]", "unsigned long*", "unsigned long"];
		msg.function.inParamsVal = [cardStore, jsonFilter, null, flags];
		msg.function.outParams = ["#3 unsigned long* int"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },
  
  //ICAClientSign - load and return certificates from the Windows or smart card store
  certLoadUserKeyStoreJSON: function (cb, cardStore, jsonFilter, flags) {
    var fnName = "certLoadUserKeyStoreJSON";
    ICAPKIService.Log("log", fnName + ": start");

    cardStore = cardStore ? 1 : 0;
    flags = ICAPKIService.paramExists(flags)
      ? flags
      : ICAClientSign.CERTLOAD_SIGNING_FLAG |
        ICAClientSign.CERTLOAD_QUALIFIED_FLAG;

    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxCertificateLoadAndListUserKeyStoreJSON";
    msg.function.inParams = ["int", "const char[]", "char**", "unsigned long"];
    msg.function.inParamsVal = [cardStore, jsonFilter, null, flags];
    msg.function.outParams = ["#3 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxCertificateLoadAndListUserKeyStoreJSON()", rsp);
      var jsonCerts = rsp.function.outParamsVal[0];
      cb(jsonCerts);
    });
  },
  
  certLoadUserKeyStoreJSONAsync: function (cardStore, jsonFilter, flags) {
	return new Promise(function (resolve, reject) {
		const fnName = "certLoadUserKeyStoreJSON";
		ICAPKIService.Log("log", fnName + ": start");

		cardStore = cardStore ? 1 : 0;
		flags = ICAPKIService.paramExists(flags)
			? flags
			: ICAClientSign.CERTLOAD_SIGNING_FLAG |
			  ICAClientSign.CERTLOAD_QUALIFIED_FLAG;

		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxCertificateLoadAndListUserKeyStoreJSON";
		msg.function.inParams = ["int", "const char[]", "char**", "unsigned long"];
		msg.function.inParamsVal = [cardStore, jsonFilter, null, flags];
		msg.function.outParams = ["#3 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - get certificate used for signing, returns PEM of the certificate
  signerGetCertificate: function (cb) {
    var fnName = "signerGetCertificate";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  signerGetCertificateAsync: function () {
	return new Promise(function (resolve, reject) {
		const fnName = "signerGetCertificate";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxSignerGetCertificate";
		msg.function.inParams = ["char**"];
		msg.function.outParams = ["#1 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - set certificate used for signing, in: cb - callback to run after setting the certificate, pem - certificate in PEM format
  signerSetCertificate: function (cb, pem) {
    var fnName = "signerSetCertificate";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxSignerSetCertificate";
    msg.function.inParams = ["char[]"];
    msg.function.inParamsVal = [pem];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      var stat = ICAPKIService.checkResponse("IcsxSignerSetCertificate()", rsp);
      cb(stat);
    });
  },
  
  signerSetCertificateAsync: function (pem) {
	return new Promise(function (resolve, reject) {
		const fnName = "signerSetCertificate";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxSignerSetCertificate";
		msg.function.inParams = ["char[]"];
		msg.function.inParamsVal = [pem];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - set browser cookie
  setCookie: function (cb, cookieObjects) {
    //pole URL a pole k nim odpovídajících jmen proměnných
    var fnName = "setCookie";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxSetCookie";
    msg.function.inParams = ["const char[]"];
    msg.function.inParamsVal = [JSON.stringify(cookieObjects)];

    var responseCallback = function (rsp) {
      ICAPKIService.checkResponse("IcsxSetCookie()", rsp);
      cb();
    };

    var msgB64 = ICAPKIService.encodeToBase64(JSON.stringify(msg));
    var msgId = ICAPKIService.HashCode.value(msgB64);
    var wrappedMsg = {
      type: ICAPKIService.m_messageTypeEnum.DIRECTIVE,
      purpose: "cookie",
      id: msgId,
      content: msgB64,
    };

    ICAPKIService.Log(
      "log",
      "sendCallMessage: Registering msgId(hash)=" +
        msgId +
        " to the map of callbacks"
    );
    ICAPKIService.Log(
      "info",
      "sendCallMessage: Decoded call content: " + JSON.stringify(msg)
    );
    ICAPKIService.m_callbackMap[msgId] = responseCallback;
    ICAPKIService.sendMessage(wrappedMsg);
  },
  
  setCookieAsync: function (cookieObjects) {
	return new Promise(function (resolve, reject) {
		//pole URL a pole k nim odpovídajících jmen proměnných
		const fnName = "setCookie";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxSetCookie";
		msg.function.inParams = ["const char[]"];
		msg.function.inParamsVal = [JSON.stringify(cookieObjects)];

		let responseCallback = function (rsp) {
			Promise.resolve(ICAPKIService.checkResponseAsync("IcsxSetCookie()", rsp))
				.then(function(returnCode) {
					if (returnCode === 0) {
						resolve(returnCode);
					}
					else {
						Promise.resolve(ICAClientSign.getErrorMessageAsync(returnCode))
							.then(function(errorMsg) {
								reject(new ICAClientSign.ICAClientSignException(returnCode, errorMsg));
							})
							.catch(function(ex) {
								reject(new ICAClientSign.ICAClientSignException(returnCode, ex.description));
							});
					}
				});
		};

		let msgB64 = ICAPKIService.encodeToBase64(JSON.stringify(msg));
		let msgId = ICAPKIService.HashCode.value(msgB64);
		let wrappedMsg = {
			type: ICAPKIService.m_messageTypeEnum.DIRECTIVE,
			purpose: "cookie",
			id: msgId,
			content: msgB64,
		};

		ICAPKIService.Log("log", "sendCallMessage: Registering msgId(hash)=" + msgId +" to the map of callbacks");
		ICAPKIService.Log("info","sendCallMessage: Decoded call content: " + JSON.stringify(msg));
	
		ICAPKIService.m_callbackMap[msgId] = responseCallback;
		ICAPKIService.sendMessage(wrappedMsg);
	})
  },
  
  //ICAClientSign - load base64 data to ICAClientSign library and return contentId
  //larger message will be divided to chunks to avoid browser limitations
  loadBase64Data: function (base64Data, cb) {
	var fnName = "loadBase64Data";
    ICAPKIService.Log("log", fnName + ": start");
	
	var msgLength = base64Data.length;
	var maxMessageLength = 30 * 1024 * 1024;
	var arrOfMessageChunks = [];
	var numberOfChunks = Math.ceil(msgLength / maxMessageLength);
	
	var offset = 0;
	
	for (var i = 0; i < numberOfChunks; i++) {
		arrOfMessageChunks.push(base64Data.substr(offset, maxMessageLength));
		offset += maxMessageLength;
	}
	
	var contentLoad = function(msgDataPart, cb) {
		var msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxContentLoad";
		msg.function.inParams = ["char[]", "char**"];
		msg.function.inParamsVal = [msgDataPart];
		msg.function.outParams = ["#2 char[]* string"];
		
		ICAPKIService.sendCallMessage(msg, function (rsp) {
			var stat = ICAPKIService.checkResponse("IcsxContentLoad()", rsp);
			var contentId = rsp.function.outParamsVal[0];
			cb(stat, contentId);
		});
	};
	
	var contentLoadUpdate = function(contentId, msgDataPart, index, cb) {
		var msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxContentLoadUpdate";
		msg.function.inParams = ["char[]", "char[]"];
		msg.function.inParamsVal = [contentId, msgDataPart];
		
		ICAPKIService.sendCallMessage(msg, function (rsp) {
			var stat = ICAPKIService.checkResponse("IcsxContentLoadUpdate()", rsp);
			cb(stat, contentId, index);
		});
	};
	
	var cbAfterUpdate = function(stat, contentId, index) {
		if (stat === true) {
			if (index === arrOfMessageChunks.length - 1)
				cb(contentId);
			else {
				index = index + 1;
				contentLoadUpdate(contentId, arrOfMessageChunks[index], index, cbAfterUpdate);
			}
		}
	};
	
	var cbAfterLoad = function(stat, contentId) {
		if (stat === true) {
			var index = 1;
			
			if (arrOfMessageChunks.length === 1)
				cb(contentId);
			else
				contentLoadUpdate(contentId, arrOfMessageChunks[index], index, cbAfterUpdate);
		}
	};
	
	contentLoad(arrOfMessageChunks[0], cbAfterLoad);
  },
  
  loadBase64DataAsync: function (base64Data) {
	let contentLoad = function(msgDataPart) {
		return new Promise(function(resolve, reject) {
			let msg = new ICAClientSign.callMsgTemp();
			msg.function.name = "IcsxContentLoad";
			msg.function.inParams = ["char[]", "char**"];
			msg.function.inParamsVal = [msgDataPart];
			msg.function.outParams = ["#2 char[]* string"];
		
			ICAPKIService.sendCallMessage(msg, function (rsp) {
				Promise.resolve(ICAPKIService.checkResponseAsync("IcsxContentLoad()", rsp))
					.then(function(returnCode) {
						if (returnCode === 0)
							resolve(rsp.function.outParamsVal[0]);
						else
							reject(returnCode);
					});
			});
		});
	};
	
	let contentLoadUpdate = function(contentId, msgDataPart) {
		return new Promise(function(resolve, reject) {
			let msg = new ICAClientSign.callMsgTemp();
			msg.function.name = "IcsxContentLoadUpdate";
			msg.function.inParams = ["char[]", "char[]"];
			msg.function.inParamsVal = [contentId, msgDataPart];
			
			ICAPKIService.sendCallMessage(msg, function (rsp) {
				Promise.resolve(ICAPKIService.checkResponseAsync("IcsxContentLoadUpdate()", rsp))
					.then(function(returnCode) {
						if (returnCode === 0)
							resolve(contentId);
						else
							reject(returnCode);
					});
			});
		});
	};
	
	return new Promise(function (resolve, reject) {
		const fnName = "loadBase64Data";
		ICAPKIService.Log("log", fnName + ": start");
	
		const msgLength = base64Data.length;
		const maxMessageLength = 30 * 1024 * 1024;
		let arrOfMessageChunks = [];
		const numberOfChunks = Math.ceil(msgLength / maxMessageLength);
	
		let offset = 0;
	
		for (let i = 0; i < numberOfChunks; i++) {
			arrOfMessageChunks.push(base64Data.substr(offset, maxMessageLength));
			offset += maxMessageLength;
		}
		
		const arrOfMessageChunksLen = arrOfMessageChunks.length;
		let promise = Promise.resolve(contentLoad(arrOfMessageChunks[0]));
		
		for (let i = 1; i <= arrOfMessageChunksLen; i++) {
			promise = promise.then(function(contentId) {
				if (i === arrOfMessageChunksLen)
					resolve(contentId);
				else
					return Promise.resolve(contentLoadUpdate(contentId, arrOfMessageChunks[i]));
			}).catch(function(returnCode) {
				Promise.resolve(ICAClientSign.getErrorMessageAsync(returnCode))
					.then(function(errorMsg) {
						reject(new ICAClientSign.ICAClientSignException(returnCode, errorMsg));
					})
					.catch(function(ex) {
						reject(new ICAClientSign.ICAClientSignException(returnCode, ex.description));
					});
			});
		}
	});
  },

  //ICAClientSign - download document from server, in: url - address of document to download
  contentDownload: function (cb, url) {
    var fnName = "contentDownload";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  contentDownloadAsync: function (url) {
	return new Promise(function (resolve, reject) {
		var fnName = "contentDownload";
		ICAPKIService.Log("log", fnName + ": start");
		var msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxContentDownload";
		msg.function.inParams = ["const char[]", "char**", "long*", "char**"];
		msg.function.inParamsVal = [url];
		msg.function.outParams = ["#2 char[]* string", "#3 long* long", "#4 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - upload document to server, in: contentId - signed content to upload, url - address of server/script for uploading
  contentUpload: function (cb, contentId, url) {
    var fnName = "contentUpload";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  contentUploadAsync: function (contentId, url) {
	return new Promise(function (resolve, reject) {
		const fnName = "contentUpload";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxContentUpload";
		msg.function.inParams = ["const char[]", "const char[]", "long*", "char**"];
		msg.function.inParamsVal = [contentId, url];
		msg.function.outParams = ["#3 long* long", "#4 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },
  
  
  // ICAClientSign - get base64 data for contentId (e.g. ICSX_0)
  getBase64ForContentId: function (cb, contentId) {
    var fname = "getBase64ForContentId";
    ICAPKIService.Log("log", fname + ": start");
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
  
  getBase64ForContentIdAsync: function (contentId) {
	return new Promise(function (resolve, reject) {
		let fname = "getBase64ForContentId";
		ICAPKIService.Log("log", fname + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxGetContent";
		msg.function.inParams = ["const char[]", "char**"];
		msg.function.inParamsVal = [contentId];
		msg.function.outParams = ["#2 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - display a preview of the document in the user's default desktop application, in: content - data to show, file_ext: extension of file (ex. pdf)
  contentPreview: function (cb, content, file_ext) {
    var fnName = "contentPreview";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxContentPreview";
    msg.function.inParams = ["const char[]", "const char[]"];
    msg.function.inParamsVal = [content, file_ext];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      var stat = ICAPKIService.checkResponse("IcsxContentPreview()", rsp);
      cb(stat);
    });
  },
  
  contentPreviewAsync: function (content, file_ext) {
	return new Promise(function (resolve, reject) {
		const fnName = "contentPreview";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxContentPreview";
		msg.function.inParams = ["const char[]", "const char[]"];
		msg.function.inParamsVal = [content, file_ext];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - save a file to disk, in: content - data to save, full_fill_name - default file name including extension, dialogTitle - title of save dialog
  saveToDisk: function (cb, content, full_fill_name, dialogTitle) {
    var fnName = "saveToDisk";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxSaveToDisk";
    msg.function.inParams = ["const char[]", "const char[]", "const char[]"];
    msg.function.inParamsVal = [content, full_fill_name, dialogTitle];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      var stat = ICAPKIService.checkResponse("IcsxSaveToDisk()", rsp);
      cb(stat);
    });
  },
  
  saveToDiskAsync: function (content, full_fill_name, dialogTitle) {
	return new Promise(function (resolve, reject) {
		const fnName = "saveToDisk";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxSaveToDisk";
		msg.function.inParams = ["const char[]", "const char[]", "const char[]"];
		msg.function.inParamsVal = [content, full_fill_name, dialogTitle];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  getSignature: function (cb, signedId) {
    var fnName = "getSignature";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  getSignatureAsync: function (signedId) {
	return new Promise(function (resolve, reject) {
		const fnName = "getSignature";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxGetSignature";
		msg.function.inParams = ["const char[]", "char**"];
		msg.function.inParamsVal = [signedId];
		msg.function.outParams = ["#2 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - calculate hashes of the file, in: hashAlgs - hashes to return (SHA-1;SHA-256;SHA-512), full_fill_name - default file name including extension, dialogTitle - title of save dialog
  getFileHash: function (cb, hashAlgs, full_fill_name, dialogTitle) {
    var fnName = "getFileHash";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  getFileHashAsync: function (hashAlgs, full_fill_name, dialogTitle) {
	return new Promise(function (resolve, reject) {
		const fnName = "getFileHash";
		ICAPKIService.Log("log", fnName + ": start");
		var msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxGetFileHash";
		msg.function.inParams = ["const char[]", "const char[]", "const char[]", "char**"];
		msg.function.inParamsVal = [hashAlgs, full_fill_name, dialogTitle];
		msg.function.outParams = ["#4 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - get serial number of timestamp from file, in: timestampValType - integer of type of SN to return, full_fill_name - default file name including extension, dialogTitle - title of save dialog
  getTimeStampSNFromSignedFile: function (cb, timestampValType, full_fill_name, dialogTitle) {
    var fnName = "getTimeStampSNFromSignedFile";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  getTimeStampSNFromSignedFileAsync: function (timestampValType, full_fill_name, dialogTitle) {
	return new Promise(function (resolve, reject) {
		const fnName = "getTimeStampSNFromSignedFile";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxTimeStampGetValuesFromSignedFile";
		msg.function.inParams = ["int", "const char[]", "const char[]", "char**"];
		msg.function.inParamsVal = [timestampValType, full_fill_name, dialogTitle];
		msg.function.outParams = ["#4 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - CMS internal signature
  signCms: function (cb, content) {
    var fnName = "signCms";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  signCmsAsync: function (content) {
	return new Promise(function (resolve, reject) {
		const fnName = "signCms";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxCmsSign";
		msg.function.inParams = ["const char[]", "char**"];
		msg.function.inParamsVal = [content];
		msg.function.outParams = ["#2 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - CMS external signature
  signCmsDetached: function (cb, content) {
    var fnName = "signCmsDetached";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  signCmsDetachedAsync: function (content) {
	return new Promise(function (resolve, reject) {
		const fnName = "signCmsDetached";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxCmsSignDetached";
		msg.function.inParams = ["const char[]", "char**"];
		msg.function.inParamsVal = [content];
		msg.function.outParams = ["#2 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - add signature to existing CMS internal signature
  coSignCms: function (cb, content) {
    var fnName = "coSignCms";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  coSignCmsAsync: function (content) {
	return new Promise(function (resolve, reject) {
		const fnName = "coSignCms";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxCmsCoSign";
		msg.function.inParams = ["const char[]", "char**"];
		msg.function.inParamsVal = [content];
		msg.function.outParams = ["#2 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - add signature to existing CMS external signature
  coSignCmsDetached: function (cb, content, originalContent) {
    var fnName = "coSignCmsDetached";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  coSignCmsDetachedAsync: function (content, originalContent) {
	return new Promise(function (resolve, reject) {
		const fnName = "coSignCmsDetached";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxCmsCoSignDetached";
		msg.function.inParams = ["const char[]", "const char[]", "char**"];
		msg.function.inParamsVal = [content, originalContent];
		msg.function.outParams = ["#3 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - CAdES internal signature
  signCades: function (cb, content, profile) {
    var fnName = "signCades";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  signCadesAsync: function (content, profile) {
	return new Promise(function (resolve, reject) {
		const fnName = "signCades";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxCadesSign";
		msg.function.inParams = ["const char[]", "int", "char**"];
		msg.function.inParamsVal = [content, profile];
		msg.function.outParams = ["#3 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - CAdES external signature
  signCadesDetached: function (cb, content, profile) {
    var fnName = "signCadesDetached";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  signCadesDetachedAsync: function (content, profile) {
	return new Promise(function (resolve, reject) {
		const fnName = "signCadesDetached";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxCadesSignDetached";
		msg.function.inParams = ["const char[]", "int", "char**"];
		msg.function.inParamsVal = [content, profile];
		msg.function.outParams = ["#3 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - add signature to existing CAdES internal signature
  coSignCades: function (cb, content, profile) {
    var fnName = "coSignCades";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  coSignCadesAsync: function (content, profile) {
	return new Promise(function (resolve, reject) {
		const fnName = "coSignCades";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxCadesCoSign";
		msg.function.inParams = ["const char[]", "int", "char**"];
		msg.function.inParamsVal = [content, profile];
		msg.function.outParams = ["#3 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - add signature to existing CAdES external signature
  coSignCadesDetached: function (cb, content, originalContent, profile) {
    var fnName = "coSignCadesDetached";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  coSignCadesDetachedAsync: function (content, originalContent, profile) {
	return new Promise(function (resolve, reject) {
		const fnName = "coSignCadesDetached";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxCadesCoSignDetached";
		msg.function.inParams = ["const char[]", "const char[]", "int", "char**"];
		msg.function.inParamsVal = [content, originalContent, profile];
		msg.function.outParams = ["#4 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - PAdES (PDF) signature
  signPades: function (cb, content, profile) {
    var fnName = "signPades";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  signPadesAsync: function (content, profile) {
	return new Promise(function (resolve, reject) {
		const fnName = "signPades";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPadesSign";
		msg.function.inParams = ["const char[]", "int", "char**"];
		msg.function.inParamsVal = [content, profile];
		msg.function.outParams = ["#3 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - get description used for PDF signature
  pdfOptionsGetDescription: function (cb) {
    var fnName = "pdfOptionsGetDescription";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  pdfOptionsGetDescriptionAsync: function () {
	return new Promise(function (resolve, reject) {
		var fnName = "pdfOptionsGetDescription";
		ICAPKIService.Log("log", fnName + ": start");
		var msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsGetDescription";
		msg.function.inParams = ["char**"];
		msg.function.outParams = ["#1 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - set description used for PDF signature
  pdfOptionsSetDescription: function (cb, desc) {
    var fnName = "pdfOptionsSetDescription";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsSetDescription";
    msg.function.inParams = ["const char[]"];
    msg.function.inParamsVal = [desc];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsSetDescription()", rsp);
      cb(fnName);
    });
  },
  
  pdfOptionsSetDescriptionAsync: function (desc) {
	return new Promise(function (resolve, reject) {
		const fnName = "pdfOptionsSetDescription";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsSetDescription";
		msg.function.inParams = ["const char[]"];
		msg.function.inParamsVal = [desc];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - get reason text used for PDF signature
  pdfOptionsGetPdfSignReason: function (cb) {
    var fnName = "pdfOptionsGetPdfSignReason";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  pdfOptionsGetPdfSignReasonAsync: function () {
	return new Promise(function (resolve, reject) {
		const fnName = "pdfOptionsGetPdfSignReason";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsGetPdfSignReason";
		msg.function.inParams = ["char**"];
		msg.function.outParams = ["#1 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - set reason text used for PDF signature
  pdfOptionsSetPdfSignReason: function (cb, reason) {
    var fnName = "pdfOptionsSetPdfSignReason";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsSetPdfSignReason";
    msg.function.inParams = ["const char[]"];
    msg.function.inParamsVal = [reason];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsSetPdfSignReason()", rsp);
      cb(fnName);
    });
  },
  
  pdfOptionsSetPdfSignReasonAsync: function (reason) {
	return new Promise(function (resolve, reject) {
		const fnName = "pdfOptionsSetPdfSignReason";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsSetPdfSignReason";
		msg.function.inParams = ["const char[]"];
		msg.function.inParamsVal = [reason];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - get number of page where to place visible signature
  pdfOptionsGetSignaturePage: function (cb) {
    var fnName = "pdfOptionsGetSignaturePage";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  pdfOptionsGetSignaturePageAsync: function () {
	return new Promise(function (resolve, reject) {
		const fnName = "pdfOptionsGetSignaturePage";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsGetSignaturePage";
		msg.function.inParams = ["int*"];
		msg.function.outParams = ["#1 int* int"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - set number of page where to place visible signature
  pdfOptionsSetSignaturePage: function (cb, page) {
    var fnName = "pdfOptionsSetSignaturePage";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsSetSignaturePage";
    msg.function.inParams = ["int"];
    msg.function.inParamsVal = [page];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsSetSignaturePage()", rsp);
      cb(fnName);
    });
  },
  
  pdfOptionsSetSignaturePageAsync: function (page) {
	return new Promise(function (resolve, reject) {
		const fnName = "pdfOptionsSetSignaturePage";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsSetSignaturePage";
		msg.function.inParams = ["int"];
		msg.function.inParamsVal = [page];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - get type (visible, invisible, ...) of PDF signature
  pdfOptionsGetSignatureType: function (cb) {
    var fnName = "pdfOptionsGetSignatureType";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  pdfOptionsGetSignatureTypeAsync: function () {
	return new Promise(function (resolve, reject) {
		const fnName = "pdfOptionsGetSignatureType";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsGetSignatureType";
		msg.function.inParams = ["int*"];
		msg.function.outParams = ["#1 int* int"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - set type (visible, invisible, ...) of PDF signature
  pdfOptionsSetSignatureType: function (cb, type) {
    var fnName = "pdfOptionsSetSignatureType";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsSetSignatureType";
    msg.function.inParams = ["int"];
    msg.function.inParamsVal = [type];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsSetSignatureType()", rsp);
      cb(fnName);
    });
  },
  
  pdfOptionsSetSignatureTypeAsync: function (type) {
	return new Promise(function (resolve, reject) {
		const fnName = "pdfOptionsSetSignatureType";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsSetSignatureType";
		msg.function.inParams = ["int"];
		msg.function.inParamsVal = [type];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - get name of signer (signing person) used for signing
  pdfOptionsGetSignerName: function (cb) {
    var fnName = "pdfOptionsGetSignerName";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  pdfOptionsGetSignerNameAsync: function () {
	return new Promise(function (resolve, reject) {
		const fnName = "pdfOptionsGetSignerName";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsGetSignerName";
		msg.function.inParams = ["char**"];
		msg.function.outParams = ["#1 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - set name of signer (signing person) used for signing
  pdfOptionsSetSignerName: function (cb, name) {
    var fnName = "pdfOptionsSetSignerName";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsSetSignerName";
    msg.function.inParams = ["const char[]"];
    msg.function.inParamsVal = [name];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsSetSignerName()", rsp);
      cb(fnName);
    });
  },
  
  pdfOptionsSetSignerNameAsync: function (name) {
	return new Promise(function (resolve, reject) {
		const fnName = "pdfOptionsSetSignerName";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsSetSignerName";
		msg.function.inParams = ["const char[]"];
		msg.function.inParamsVal = [name];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - get location text (not position) used for signing
  pdfOptionsGetSignLocation: function (cb) {
    var fnName = "pdfOptionsGetSignLocation";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  pdfOptionsGetSignLocationAsync: function () {
	return new Promise(function (resolve, reject) {
		const fnName = "pdfOptionsGetSignLocation";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsGetSignLocation";
		msg.function.inParams = ["char**"];
		msg.function.outParams = ["#1 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - set location text (not position) used for signing
  pdfOptionsSetSignLocation: function (cb, loc) {
    var fnName = "pdfOptionsSetSignLocation";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsSetSignLocation";
    msg.function.inParams = ["const char[]"];
    msg.function.inParamsVal = [loc];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsSetSignLocation()", rsp);
      cb(fnName);
    });
  },
  
  pdfOptionsSetSignLocationAsync: function (loc) {
	return new Promise(function (resolve, reject) {
		const fnName = "pdfOptionsSetSignLocation";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsSetSignLocation";
		msg.function.inParams = ["const char[]"];
		msg.function.inParamsVal = [loc];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - get horizontal position of signature (PDF file)
  pdfOptionsGetPosX: function (cb) {
    var fnName = "pdfOptionsGetPosX";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  pdfOptionsGetPosXAsync: function () {
	return new Promise(function (resolve, reject) {
		const fnName = "pdfOptionsGetPosX";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsGetPosX";
		msg.function.inParams = ["float*"];
		msg.function.outParams = ["#1 float* float"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - set horizontal position of signature (PDF file)
  pdfOptionsSetPosX: function (cb, pos) {
    var fnName = "pdfOptionsSetPosX";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsSetPosX";
    msg.function.inParams = ["float"];
    msg.function.inParamsVal = [pos];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsSetPosX()", rsp);
      cb(fnName);
    });
  },
  
  pdfOptionsSetPosXAsync: function (pos) {
	return new Promise(function (resolve, reject) {
		const fnName = "pdfOptionsSetPosX";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsSetPosX";
		msg.function.inParams = ["float"];
		msg.function.inParamsVal = [pos];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - get vertical position of visible signature (PDF file)
  pdfOptionsGetPosY: function (cb) {
    var fnName = "pdfOptionsGetPosY";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  pdfOptionsGetPosYAsync: function () {
	return new Promise(function (resolve, reject) {
		const fnName = "pdfOptionsGetPosY";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsGetPosY";
		msg.function.inParams = ["float*"];
		msg.function.outParams = ["#1 float* float"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - set vertical position of visible signature (PDF file)
  pdfOptionsSetPosY: function (cb, pos) {
    var fnName = "pdfOptionsSetPosY";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsSetPosY";
    msg.function.inParams = ["float"];
    msg.function.inParamsVal = [pos];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsSetPosY()", rsp);
      cb(fnName);
    });
  },
  
  pdfOptionsSetPosYAsync: function (pos) {
	return new Promise(function (resolve, reject) {
		const fnName = "pdfOptionsSetPosY";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsSetPosY";
		msg.function.inParams = ["float"];
		msg.function.inParamsVal = [pos];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - get width of visible signature (PDF file)
  pdfOptionsGetWidth: function (cb) {
    var fnName = "pdfOptionsGetWidth";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  pdfOptionsGetWidthAsync: function () {
	return new Promise(function (resolve, reject) {
		const fnName = "pdfOptionsGetWidth";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsGetWidth";
		msg.function.inParams = ["float*"];
		msg.function.outParams = ["#1 float* float"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - set width of visible signature (PDF file)
  pdfOptionsSetWidth: function (cb, pos) {
    var fnName = "pdfOptionsSetWidth";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsSetWidth";
    msg.function.inParams = ["float"];
    msg.function.inParamsVal = [pos];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsSetWidth()", rsp);
      cb(fnName);
    });
  },
  
  pdfOptionsSetWidthAsync: function (pos) {
	return new Promise(function (resolve, reject) {
		const fnName = "pdfOptionsSetWidth";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsSetWidth";
		msg.function.inParams = ["float"];
		msg.function.inParamsVal = [pos];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - get height of visible signature (PDF file)
  pdfOptionsGetHeight: function (cb) {
    var fnName = "pdfOptionsGetHeight";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  pdfOptionsGetHeightAsync: function () {
	return new Promise(function (resolve, reject) {
		const fnName = "pdfOptionsGetHeight";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsGetHeight";
		msg.function.inParams = ["float*"];
		msg.function.outParams = ["#1 float* float"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - set height of visible signature (PDF file)
  pdfOptionsSetHeight: function (cb, pos) {
    var fnName = "pdfOptionsSetHeight";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsSetHeight";
    msg.function.inParams = ["float"];
    msg.function.inParamsVal = [pos];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsSetHeight()", rsp);
      cb(fnName);
    });
  },
  
  pdfOptionsSetHeightAsync: function (pos) {
	return new Promise(function (resolve, reject) {
		const fnName = "pdfOptionsSetHeight";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsSetHeight";
		msg.function.inParams = ["float"];
		msg.function.inParamsVal = [pos];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - set background image for visible signature (PDF file), in: imgB64 - image encoded to base64
  pdfOptionsLoadBackgroundImage: function (cb, imgB64) {
    var fnName = "pdfOptionsLoadBackgroundImage";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsLoadBackgroundImage";
    msg.function.inParams = ["const char[]"];
    msg.function.inParamsVal = [imgB64];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsLoadBackgroundImage()", rsp);
      cb(fnName);
    });
  },
  
  pdfOptionsLoadBackgroundImageAsync: function (imgB64) {
	return new Promise(function (resolve, reject) {
		const fnName = "pdfOptionsLoadBackgroundImage";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsLoadBackgroundImage";
		msg.function.inParams = ["const char[]"];
		msg.function.inParamsVal = [imgB64];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - set signer image for visible signature (PDF file), in: imgB64 - image encoded to base64
  pdfOptionsLoadSignatureImage: function (cb, imgB64) {
    var fnName = "pdfOptionsLoadSignatureImage";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsLoadSignatureImage";
    msg.function.inParams = ["const char[]"];
    msg.function.inParamsVal = [imgB64];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsLoadSignatureImage()", rsp);
      cb(fnName);
    });
  },
  
  pdfOptionsLoadSignatureImageAsync: function (imgB64) {
	return new Promise(function (resolve, reject) {
		const fnName = "pdfOptionsLoadSignatureImage";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsLoadSignatureImage";
		msg.function.inParams = ["const char[]"];
		msg.function.inParamsVal = [imgB64];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - get name of user property that includes information about anchor (PDF file)
  pdfOptionsGetAnchorPropertyName: function (cb) {
    var fnName = "pdfOptionsGetAnchorPropertyName";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  pdfOptionsGetAnchorPropertyNameAsync: function () {
	return new Promise(function (resolve, reject) {
		const fnName = "pdfOptionsGetAnchorPropertyName";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsGetAnchorPropertyName";
		msg.function.inParams = ["char**"];
		msg.function.outParams = ["#1 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - get placeholder text for anchor (PDF file)
  pdfOptionsGetAnchorPlaceholderText: function (cb) {
    var fnName = "pdfOptionsGetAnchorPlaceholderText";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  pdfOptionsGetAnchorPlaceholderTextAsync: function () {
	return new Promise(function (resolve, reject) {
		const fnName = "pdfOptionsGetAnchorPlaceholderText";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsGetAnchorPlaceholderText";
		msg.function.inParams = ["char**"];
		msg.function.outParams = ["#1 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - set name of user property that includes information about anchor and placeholder text (PDF file)
  pdfOptionsSetAnchor: function (cb, propertyName, placeholderText) {
    var fnName = "pdfOptionsSetAnchor";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsSetAnchor";
    msg.function.inParams = ["const char[]", "const char[]"];
    msg.function.inParamsVal = [propertyName, placeholderText];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsSetAnchor()", rsp);
      cb(fnName);
    });
  },
  
  pdfOptionsSetAnchorAsync: function (propertyName, placeholderText) {
	return new Promise(function (resolve, reject) {
		const fnName = "pdfOptionsSetAnchor";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsSetAnchor";
		msg.function.inParams = ["const char[]", "const char[]"];
		msg.function.inParamsVal = [propertyName, placeholderText];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },
  
  //ICAClientSign - set default anchor position and size if not specified in PDF properties
  //default value "-10 -4 42 8"
  setDefaultAnchorBasedPosition: function (cb, position) {
    var fnName = "setDefaultAnchorBasedPosition";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxSetDefaultAnchorBasedPosition";
    msg.function.inParams = ["const char[]"];
    msg.function.inParamsVal = [position];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxSetDefaultAnchorBasedPosition()", rsp);
      cb(fnName);
    });
  },
  
  setDefaultAnchorBasedPositionAsync: function (position) {
	return new Promise(function (resolve, reject) {
		const fnName = "setDefaultAnchorBasedPosition";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxSetDefaultAnchorBasedPosition";
		msg.function.inParams = ["const char[]"];
		msg.function.inParamsVal = [position];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - get flag if add timestamp with signature (PDF file)
  pdfOptionsGetAddTimeStamp: function (cb) {
    var fnName = "pdfOptionsGetAddTimeStamp";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  pdfOptionsGetAddTimeStampAsync: function () {
	return new Promise(function (resolve, reject) {
		const fnName = "pdfOptionsGetAddTimeStamp";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsGetAddTimeStamp";
		msg.function.inParams = ["int*"];
		msg.function.outParams = ["#1 int* int"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - set flag if add timestamp with signature (PDF file)
  pdfOptionsSetAddTimeStamp: function (cb, addTimeStamp) {
    var fnName = "pdfOptionsSetAddTimeStamp";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  pdfOptionsSetAddTimeStampAsync: function (addTimeStamp) {
	return new Promise(function (resolve, reject) {
		const fnName = "pdfOptionsSetAddTimeStamp";
		ICAPKIService.Log("log", fnName + ": start");
		addTimeStamp = addTimeStamp ? 1 : 0;
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsSetAddTimeStamp";
		msg.function.inParams = ["int"];
		msg.function.inParamsVal = [addTimeStamp];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - reset PDF parameters
  pdfOptionsReset: function (cb) {
    var fnName = "pdfOptionsReset";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxPdfOptionsReset";
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxPdfOptionsReset()", rsp);
      if (cb) cb();
    });
  },
  
  pdfOptionsResetAsync: function () {
	return new Promise(function (resolve, reject) {
		const fnName = "pdfOptionsReset";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxPdfOptionsReset";
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - get timestamp server URL
  timeStampOptionsGetUrl: function (cb) {
    var fnName = "timeStampOptionsGetUrl";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  timeStampOptionsGetUrlAsync: function () {
	return new Promise(function (resolve, reject) {
		const fnName = "timeStampOptionsGetUrl";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxTimeStampOptionsGetUrl";
		msg.function.inParams = ["char**"];
		msg.function.outParams = ["#1 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - set timestamp server URL
  timeStampOptionsSetUrl: function (cb, urlTSA) {
    var fnName = "timeStampOptionsSetUrl";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxTimeStampOptionsSetUrl";
    msg.function.inParams = ["const char[]"];
    msg.function.inParamsVal = [urlTSA];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxTimeStampOptionsSetUrl()", rsp);
      cb(fnName);
    });
  },
  
  timeStampOptionsSetUrlAsync: function (urlTSA) {
	return new Promise(function (resolve, reject) {
		const fnName = "timeStampOptionsSetUrl";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxTimeStampOptionsSetUrl";
		msg.function.inParams = ["const char[]"];
		msg.function.inParamsVal = [urlTSA];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - get timestamp authentication username
  timeStampOptionsGetAuthUser: function (cb) {
    var fnName = "timeStampOptionsGetAuthUser";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  timeStampOptionsGetAuthUserAsync: function () {
	return new Promise(function (resolve, reject) {
		const fnName = "timeStampOptionsGetAuthUser";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxTimeStampOptionsGetAuthUser";
		msg.function.inParams = ["char**"];
		msg.function.outParams = ["#1 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - set timestamp authentication username
  timeStampOptionsSetAuthUser: function (cb, authUser) {
    var fnName = "timeStampOptionsSetAuthUser";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxTimeStampOptionsSetAuthUser";
    msg.function.inParams = ["const char[]"];
    msg.function.inParamsVal = [authUser];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxTimeStampOptionsSetAuthUser()", rsp);
      cb(fnName);
    });
  },
  
  timeStampOptionsSetAuthUserAsync: function (authUser) {
	return new Promise(function (resolve, reject) {
		const fnName = "timeStampOptionsSetAuthUser";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxTimeStampOptionsSetAuthUser";
		msg.function.inParams = ["const char[]"];
		msg.function.inParamsVal = [authUser];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - get timestamp authentication password
  timeStampOptionsGetAuthPassword: function (cb) {
    var fnName = "timeStampOptionsGetAuthPassword";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  timeStampOptionsGetAuthPasswordAsync: function () {
	return new Promise(function (resolve, reject) {
		const fnName = "timeStampOptionsGetAuthPassword";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxTimeStampOptionsGetAuthPassword";
		msg.function.inParams = ["char**"];
		msg.function.outParams = ["#1 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - set timestamp authentication password
  timeStampOptionsSetAuthPassword: function (cb, authPassword) {
    var fnName = "timeStampOptionsSetAuthPassword";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxTimeStampOptionsSetAuthPassword";
    msg.function.inParams = ["const char[]"];
    msg.function.inParamsVal = [authPassword];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxTimeStampOptionsSetAuthPassword()", rsp);
      cb(fnName);
    });
  },
  
  timeStampOptionsSetAuthPasswordAsync: function (authPassword) {
	return new Promise(function (resolve, reject) {
		const fnName = "timeStampOptionsSetAuthPassword";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxTimeStampOptionsSetAuthPassword";
		msg.function.inParams = ["const char[]"];
		msg.function.inParamsVal = [authPassword];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - get timestamp authentication certificate
  timeStampOptionsGetAuthCertificate: function (cb) {
    var fnName = "timeStampOptionsGetAuthCertificate";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  timeStampOptionsGetAuthCertificateAsync: function () {
	return new Promise(function (resolve, reject) {
		const fnName = "timeStampOptionsGetAuthCertificate";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxTimeStampOptionsGetAuthCertificate";
		msg.function.inParams = ["char**"];
		msg.function.outParams = ["#1 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - set timestamp authentication certificate
  timeStampOptionsSetAuthCertificate: function (cb, authCertificate) {
    var fnName = "timeStampOptionsSetAuthCertificate";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  timeStampOptionsSetAuthCertificateAsync: function (authCertificate) {
	return new Promise(function (resolve, reject) {
		const fnName = "timeStampOptionsSetAuthCertificate";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxTimeStampOptionsSetAuthCertificate";
		msg.function.inParams = ["const char[]"];
		msg.function.inParamsVal = [authCertificate];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },
  
  //ICAClientSign - get timestamp hash algorithm
  timeStampOptionsGetHashAlgorithm: function (cb) {
    var fnName = "timeStampOptionsGetHashAlgorithm";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxTimeStampOptionsGetHashAlgorithm";
    msg.function.inParams = ["char**"];
    msg.function.outParams = ["#1 char[]* string"];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse(
        "IcsxTimeStampOptionsGetHashAlgorithm()",
        rsp
      );
      var hashAlgorithm = rsp.function.outParamsVal[0];
      cb(hashAlgorithm);
    });
  },
  
  timeStampOptionsGetHashAlgorithmAsync: function () {
	return new Promise(function (resolve, reject) {
		const fnName = "timeStampOptionsGetHashAlgorithm";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxTimeStampOptionsGetHashAlgorithm";
		msg.function.inParams = ["char**"];
		msg.function.outParams = ["#1 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - set timestamp hash algorithm
  timeStampOptionsSetHashAlgorithm: function (cb, hashAlgorithm) {
    var fnName = "timeStampOptionsSetHashAlgorithm";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxTimeStampOptionsSetHashAlgorithm";
    msg.function.inParams = ["const char[]"];
    msg.function.inParamsVal = [hashAlgorithm];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse(
        "IcsxTimeStampOptionsSetHashAlgorithm()",
        rsp
      );
      cb(fnName);
    });
  },
  
  timeStampOptionsSetHashAlgorithmAsync: function (hashAlgorithm) {
	return new Promise(function (resolve, reject) {
		const fnName = "timeStampOptionsSetHashAlgorithm";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxTimeStampOptionsSetHashAlgorithm";
		msg.function.inParams = ["const char[]"];
		msg.function.inParamsVal = [hashAlgorithm];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - reset TimeStamp parameters
  timeStampOptionsReset: function (cb) {
    var fnName = "timeStampOptionsReset";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxTimeStampOptionsReset";
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      ICAPKIService.checkResponse("IcsxTimeStampOptionsReset()", rsp);
      if (cb) cb();
    });
  },
  
  timeStampOptionsResetAsync: function () {
	return new Promise(function (resolve, reject) {
		const fnName = "timeStampOptionsReset";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxTimeStampOptionsReset";
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - initialization, create session for encryption
  encryptCreateSession: function (cb, algorithm) {
    var fnName = "encryptCreateSession";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  encryptCreateSessionAsync: function (algorithm) {
	return new Promise(function (resolve, reject) {
		const fnName = "encryptCreateSession";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxEncryptCreateSession";
		msg.function.inParams = ["const char[]", "char**"];
		msg.function.inParamsVal = [algorithm];
		msg.function.outParams = ["#2 char[]* string"];
	
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - setting the initialization vector
  encryptSetIV: function (cb, sessionId, initVector) {
    var fnName = "encryptSetIV";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxEncryptSetIV";
    msg.function.inParams = ["const char[]", "const char[]"];
    msg.function.inParamsVal = [sessionId, initVector];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      var stat = ICAPKIService.checkResponse("IcsxEncryptSetIV()", rsp);
      cb(stat);
    });
  },
  
  encryptSetIVAsync: function (sessionId, initVector) {
	return new Promise(function (resolve, reject) {
		const fnName = "encryptSetIV";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxEncryptSetIV";
		msg.function.inParams = ["const char[]", "const char[]"];
		msg.function.inParamsVal = [sessionId, initVector];
	
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - generate a symmetric key in the session
  encryptGenerateSecretKey: function (cb, sessionId, keyLength) {
    var fnName = "encryptGenerateSecretKey";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxEncryptGenerateSecretKey";
    msg.function.inParams = ["const char[]", "int"];
    msg.function.inParamsVal = [sessionId, keyLength];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      var stat = ICAPKIService.checkResponse("IcsxEncryptGenerateSecretKey()", rsp);
      cb(stat);
    });
  },
  
  encryptGenerateSecretKeyAsync: function (sessionId, keyLength) {
	return new Promise(function (resolve, reject) {
		const fnName = "encryptGenerateSecretKey";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxEncryptGenerateSecretKey";
		msg.function.inParams = ["const char[]", "int"];
		msg.function.inParamsVal = [sessionId, keyLength];
	
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - export symmetric key
  encryptExportSecretKey: function (cb, sessionId, exportCertPem) {
    var fnName = "encryptExportSecretKey";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  encryptExportSecretKeyAsync: function (sessionId, exportCertPem) {
	return new Promise(function (resolve, reject) {
		const fnName = "encryptExportSecretKey";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxEncryptExportSecretKey";
		msg.function.inParams = ["const char[]", "const char[]", "char**"];
		msg.function.inParamsVal = [sessionId, exportCertPem];
		msg.function.outParams = ["#3 char[]* string"];
	
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - data block encryption
  encryptUpdate: function (cb, sessionId, plainData) {
    var fnName = "encryptUpdate";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  encryptUpdateAsync: function (sessionId, plainData) {
	return new Promise(function (resolve, reject) {
		const fnName = "encryptUpdate";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxEncryptUpdate";
		msg.function.inParams = ["const char[]", "const char[]", "char**"];
		msg.function.inParamsVal = [sessionId, plainData];
		msg.function.outParams = ["#3 char[]* string"];
	
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - finalize encryption (encrypt last block of data, add padding, end session, delete encrypt key)
  encryptFinal: function (cb, sessionId) {
    var fnName = "encryptFinal";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  encryptFinalAsync: function (sessionId) {
	return new Promise(function (resolve, reject) {
		const fnName = "encryptFinal";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxEncryptFinal";
		msg.function.inParams = ["const char[]", "char**"];
		msg.function.inParamsVal = [sessionId];
		msg.function.outParams = ["#2 char[]* string"];
	
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - end session for encryption
  encryptCloseSession: function (cb, sessionId) {
    var fnName = "encryptCloseSession";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxEncryptCloseSession";
    msg.function.inParams = ["const char[]"];
    msg.function.inParamsVal = [sessionId];
    ICAPKIService.sendCallMessage(msg, function (rsp) {
      var stat = ICAPKIService.checkResponse("IcsxEncryptCloseSession()", rsp);
      cb(stat);
    });
  },
  
  encryptCloseSessionAsync: function (sessionId) {
	return new Promise(function (resolve, reject) {
		const fnName = "encryptCloseSession";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxEncryptCloseSession";
		msg.function.inParams = ["const char[]"];
		msg.function.inParamsVal = [sessionId];
	
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //ICAClientSign - decrypt symmetric key
  decryptSecretKey: function (cb, encryptedKey, decryptionCertPem, exportCertPem) {
    var fnName = "decryptSecretKey";
    ICAPKIService.Log("log", fnName + ": start");
    var msg = new ICAClientSign.callMsgTemp();
    msg.function.name = "IcsxDecryptSecretKey";
    msg.function.inParams = [
      "const char[]",
      "const char[]",
      "const char[]",
      "char**",
    ];
    if (ICAPKIService.paramExists(exportCertPem))
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
  
  decryptSecretKeyAsync: function (encryptedKey, decryptionCertPem, exportCertPem) {
	return new Promise(function (resolve, reject) {
		const fnName = "decryptSecretKey";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxDecryptSecretKey";
		msg.function.inParams = ["const char[]", "const char[]", "const char[]", "char**"];
		
		if (ICAPKIService.paramExists(exportCertPem))
			msg.function.inParamsVal = [encryptedKey, decryptionCertPem, exportCertPem];
		else
			msg.function.inParamsVal = [encryptedKey, decryptionCertPem, null];
		
		msg.function.outParams = ["#4 char[]* string"];
	
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //----------XAdES (XML) related FUNCTIONS----------------------

  compressWithGzip: function (cb, toCompress) {
    var fname = "compressWithGzip";
    ICAPKIService.Log("log", fname + ": start");
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
  
  compressWithGzipAsync: function (toCompress) {
	return new Promise(function (resolve, reject) {
		let fname = "compressWithGzip";
		ICAPKIService.Log("log", fname + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxContentGzip";
		msg.function.inParams = ["const char[]", "char**"];
		msg.function.inParamsVal = [toCompress];
		msg.function.outParams = ["#2 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  getBase64OfGzip: function (cb, contentId) {
    var fname = "getBase64OfGzip";
    ICAPKIService.Log("log", fname + ": start");
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
  
  getBase64OfGzipAsync: function (contentId) {
	return new Promise(function (resolve, reject) {
		let fname = "getBase64OfGzip";
		ICAPKIService.Log("log", fname + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxGetContent";
		msg.function.inParams = ["const char[]", "char**"];
		msg.function.inParamsVal = [contentId];
		msg.function.outParams = ["#2 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  signXml: function (cb, contentIdOrBase64Xml, contentURI, profile) {
    var fname = "signXml";
    ICAPKIService.Log("log", fname + ": start");
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
  
  signXmlAsync: function (contentIdOrBase64Xml, contentURI, profile) {
	return new Promise(function (resolve, reject) {
		let fname = "signXml";
		ICAPKIService.Log("log", fname + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxXadesSignDetached";
		msg.function.inParams = ["const char[]", "const char[]", "int", "char**"];
		msg.function.inParamsVal = [contentIdOrBase64Xml, contentURI, profile];
		msg.function.outParams = ["#4 char[]* string"];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  xadesVerifyHashDetached: function (cb, unsignedBase64OrContentId, base64OrContentIdOfSignature) {
    var fname = "xadesVerifyHashDetached";
    ICAPKIService.Log("log", fname + ": start");
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
  
  xadesVerifyHashDetachedAsync: function (unsignedBase64OrContentId, base64OrContentIdOfSignature) {
	return new Promise(function (resolve, reject) {
		let fname = "xadesVerifyHashDetached";
		ICAPKIService.Log("log", fname + ": start");
		let msg = new ICAClientSign.callMsgTemp();
		msg.function.name = "IcsxXadesVerifyHashDetached";
		msg.function.inParams = ["const char[]", "const char[]"];
		msg.function.inParamsVal = [unsignedBase64OrContentId, base64OrContentIdOfSignature];
		
		ICAClientSign.SendProcessResponseOutput(resolve, reject, msg);
	})
  },

  //----------NATIVE HOST FUNCTIONS----------------------

  //Get operating system version
  GetOsVersion: function (cb) {
    var fnName = "GetOsVersion";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  GetOsVersionAsync: function () {
	return new Promise(function (resolve, reject) {
		const fnName = "GetOsVersion";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAPKIService.callMsgTemp();
		msg.function.name = "GetOsVersion";
		msg.function.inParams = ["char[#2]", "int*"];
		msg.function.inParamsVal = [null, 100];
		msg.function.outParams = ["#1 char[#2] string"];
		
		ICAClientSign.SendProcessHostResponseOutput(resolve, reject, msg);
    })
  },

  //Get installed service pack of Windows
  GetSpVersion: function (cb) {
    var fnName = "GetSpVersion";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  GetSpVersionAsync: function () {
	return new Promise(function (resolve, reject) {
		const fnName = "GetSpVersion";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAPKIService.callMsgTemp();
		msg.function.name = "GetSpVersion";
		msg.function.inParams = ["char[#2]", "int*"];
		msg.function.inParamsVal = [null, 100];
		msg.function.outParams = ["#1 char[#2] string"];
		
		ICAClientSign.SendProcessHostResponseOutput(resolve, reject, msg);
    })
  },

  //Get native host app version
  GetNativeHostVersion: function (cb) {
    var fnName = "GetNativeHostVersion";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  GetNativeHostVersionAsync: function () {
	return new Promise(function (resolve, reject) {
		const fnName = "GetNativeHostVersion";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAPKIService.callMsgTemp();
		msg.function.name = "GetNativeHostVersion";
		msg.function.inParams = ["char[#2]", "int*"];
		msg.function.inParamsVal = [null, 10];
		msg.function.outParams = ["#1 char[#2] string"];
		
		ICAClientSign.SendProcessHostResponseOutput(resolve, reject, msg);
    })
  },

  //Get whether USB devices are supported
  GetUsbSupport: function (cb) {
    var fnName = "GetUsbSupport";
    ICAPKIService.Log("log", fnName + ": start");
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
  
  GetUsbSupportAsync: function () {
	return new Promise(function (resolve, reject) {
		const fnName = "GetUsbSupport";
		ICAPKIService.Log("log", fnName + ": start");
		let msg = new ICAPKIService.callMsgTemp();
		msg.function.name = "GetUsbSupport";
		msg.function.inParams = ["int*"];
		msg.function.inParamsVal = [null];
		msg.function.outParams = ["#1 int* int"];
		
		ICAPKIService.sendCallMessage(msg, function (rsp) {
			Promise.resolve(ICAPKIService.checkHostResponseAsync(msg.function.name + "()", rsp))
				.then(function(returnCode) {
					if (returnCode === 0) {
						let usb = rsp.function.outParamsVal[0];
						resolve(parseInt(usb));
					}
					else {
						reject(new ICAPKIService.ICAPKIHostException(returnCode, "Error occured during calling " + msg.function.name + "()"));
					}
				});
		});
    })
  },
};
//-----------END OF ICACLIENTSIGN OBJECT FUNCTIONS-------------
