import Foundation

extension RustorePayPlugin {
    
    // MARK: - Data Structures
    
    struct ErrorData: Encodable {
        var code: String?
        var message: String
        var methodName: String?
    }
    
    struct SuccessData: Encodable {
        var success: Bool = true
        var message: String
        var data: [String: Any]?
        var timestamp: TimeInterval = Date().timeIntervalSince1970
        
        enum CodingKeys: String, CodingKey {
            case success, message, data, timestamp
        }
        
        func encode(to encoder: Encoder) throws {
            var container = encoder.container(keyedBy: CodingKeys.self)
            try container.encode(success, forKey: .success)
            try container.encode(message, forKey: .message)
            try container.encode(timestamp, forKey: .timestamp)
            
            if let data = data {
                // Convert Any values to encodable types
                let encodableData = data.compactMapValues { value -> Any? in
                    if value is String || value is Int || value is Double || value is Bool {
                        return value
                    }
                    return String(describing: value)
                }
                try container.encode(encodableData as? [String: String], forKey: .data)
            }
        }
    }
    
    // MARK: - Success Response Methods
    
    /**
     * Sends a simple success result
     * @param command The CDV command
     */
    public func sendSuccess(command: CDVInvokedUrlCommand) {
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
        self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
    }
    
    /**
     * Sends success result with message
     * @param command The CDV command
     * @param message Success message
     */
    public func sendSuccess(command: CDVInvokedUrlCommand, message: String) {
        let result = [
            "success": true,
            "message": message,
            "timestamp": Date().timeIntervalSince1970
        ] as [String : Any]
        
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: result)
        self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
    }
    
    /**
     * Sends success result with message and data
     * @param command The CDV command
     * @param message Success message
     * @param data Additional data to include
     */
    public func sendSuccess(command: CDVInvokedUrlCommand, message: String, data: [String: Any]?) {
        var result: [String: Any] = [
            "success": true,
            "message": message,
            "timestamp": Date().timeIntervalSince1970
        ]
        
        if let data = data {
            result["data"] = data
        }
        
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: result)
        self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
    }
    
    // MARK: - Error Response Methods
    
    /**
     * Sends error result with code and message
     * @param command The CDV command
     * @param code Error code
     * @param message Error message
     */
    public func sendError(command: CDVInvokedUrlCommand, code: String, message: String) {
        let errorObj = [
            "success": false,
            "code": code,
            "message": message,
            "methodName": command.methodName ?? "unknown",
            "timestamp": Date().timeIntervalSince1970
        ] as [String : Any]

        let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: errorObj)
        self.commandDelegate!.send(pluginResult, callbackId: command.callbackId)
    }
    
    /**
     * Sends error result with message only
     * @param command The CDV command
     * @param message Error message
     */
    public func sendError(command: CDVInvokedUrlCommand, message: String) {
        sendError(command: command, code: "PLUGIN_ERROR", message: message)
    }
    
    /**
     * Sends initialization error
     * @param command The CDV command
     */
    public func sendNotInitializedError(command: CDVInvokedUrlCommand) {
        sendError(command: command, code: "NOT_INITIALIZED", message: "Plugin not initialized")
    }
    
    // MARK: - Event Methods
    
    /**
     * Emits window event without data
     * @param event Event name
     */
    public func emitWindowEvent(event: String) {
        let js = "cordova.fireWindowEvent('\\(event)')"
        
        DispatchQueue.main.async {
            self.commandDelegate.evalJs(js)
        }
    }
    
    /**
     * Emits window event with data
     * @param event Event name
     * @param data Event data
     */
    public func emitWindowEvent(event: String, data: [String: Any]) {
        do {
            let jsonData = try JSONSerialization.data(withJSONObject: data, options: [])
            let jsonString = String(data: jsonData, encoding: .utf8) ?? "{}"
            let js = "cordova.fireWindowEvent('\\(event)', \\(jsonString))"
            
            DispatchQueue.main.async {
                self.commandDelegate.evalJs(js)
            }
        } catch {
            NSLog("Error serializing event data: \\(error)")
            emitWindowEvent(event: event)
        }
    }
    
    /**
     * Emits window event with encodable data
     * @param event Event name
     * @param data Encodable data
     */
    public func emitWindowEvent<T: Encodable>(event: String, data: T) {
        do {
            let jsonData = try JSONEncoder().encode(data)
            let jsonString = String(data: jsonData, encoding: .utf8) ?? "{}"
            let js = "cordova.fireWindowEvent('\\(event)', \\(jsonString))"
            
            DispatchQueue.main.async {
                self.commandDelegate.evalJs(js)
            }
        } catch {
            NSLog("Error encoding event data: \\(error)")
            emitWindowEvent(event: event)
        }
    }
    
    // MARK: - Logging Methods
    
    /**
     * Logs a message with plugin tag
     * @param message Message to log
     * @param level Log level (default: info)
     */
    public func log(_ message: String, level: String = "info") {
        NSLog("[RustorePayPlugin] [\\(level.uppercased())] \\(message)")
    }
    
    /**
     * Logs an error message
     * @param message Error message
     */
    public func logError(_ message: String) {
        log(message, level: "error")
    }
    
    /**
     * Logs a debug message
     * @param message Debug message
     */
    public func logDebug(_ message: String) {
        log(message, level: "debug")
    }
    
    // MARK: - Validation Methods
    
    /**
     * Checks if plugin is initialized and sends error if not
     * @param isInitialized Current initialization state
     * @param command The CDV command for error callback
     * @return true if initialized, false otherwise
     */
    public func checkInitialized(_ isInitialized: Bool, command: CDVInvokedUrlCommand) -> Bool {
        if !isInitialized {
            sendNotInitializedError(command: command)
            return false
        }
        return true
    }
    
    /**
     * Safely gets string parameter from command arguments
     * @param command The CDV command
     * @param index Parameter index
     * @param defaultValue Default value if parameter is missing
     * @return Parameter value or default
     */
    public func getStringParameter(from command: CDVInvokedUrlCommand, at index: Int, default defaultValue: String = "") -> String {
        guard command.arguments.count > index,
              let value = command.arguments[index] as? String else {
            return defaultValue
        }
        return value
    }
    
    /**
     * Safely gets dictionary parameter from command arguments
     * @param command The CDV command
     * @param index Parameter index
     * @return Parameter dictionary or empty dictionary
     */
    public func getDictionaryParameter(from command: CDVInvokedUrlCommand, at index: Int) -> [String: Any] {
        guard command.arguments.count > index,
              let value = command.arguments[index] as? [String: Any] else {
            return [:]
        }
        return value
    }
}