#import <Cordova/CDVPlugin.h>

@interface RustorePayPlugin : CDVPlugin

// Add your plugin method declarations here
- (void)init:(CDVInvokedUrlCommand*)command;
- (void)exampleMethod:(CDVInvokedUrlCommand*)command;

- (void)getPurchaseAvailability:(CDVInvokedUrlCommand*)command;
- (void)getPurchases:(CDVInvokedUrlCommand*)command;
- (void)getPurchase:(CDVInvokedUrlCommand*)command;
- (void)purchase:(CDVInvokedUrlCommand*)command;
- (void)getUserAuthorizationStatus:(CDVInvokedUrlCommand*)command;
@end