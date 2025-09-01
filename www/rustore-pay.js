let RustorePay = (function () {
    let initialized = false;

    return {
        events: {
            // Define your plugin events here
            // example: {
            //     loaded: 'exampleDidLoad',
            //     failed: 'exampleDidFail',
            // }
        },

        /**
         * Returns the state of initialization
         */
        isInitialized: function isInitialized()
        {
            return initialized;
        },

        /**
         * Initializes the plugin
         * @param {Object} params - initialization parameters
         * @param {Function} params.onSuccess - optional on success callback
         * @param {Function} params.onFailure - optional on failure callback
         */
        init: function init(params)
        {
            return new Promise((resolve, reject) => {
                params = defaults(params, {});

                // Add your validation logic here
                // if (params.hasOwnProperty('requiredParam') === false)
                // {
                //     throw new Error('RustorePay::init - requiredParam is required');
                // }

                callPlugin(
                    'init',
                    [
                        // Add your initialization parameters here
                        params.options || {},
                    ],
                    function () {
                        initialized = true;
                        resolve();
                    },
                    reject
                );
            });
        },

        /**
         * Example method - replace with your plugin methods
         * @param {Object} params - method parameters
         * @param {Function} params.onSuccess - optional on success callback
         * @param {Function} params.onFailure - optional on failure callback
         */
        exampleMethod: function exampleMethod(params)
        {
            return new Promise((resolve, reject) => {
                params = defaults(params, {});

                callPlugin('exampleMethod', [params.value], resolve, reject);
            });
        },

        // Add your plugin methods here

        /**
         * getUserAuthorizationStatus
         * @param {Object} params - method parameters
         * @param {Function} params.onSuccess - optional on success callback
         * @param {Function} params.onFailure - optional on failure callback
         */
        getUserAuthorizationStatus: function getUserAuthorizationStatus(params)
        {
            return new Promise((resolve, reject) => {
                params = defaults(params, {});

                callPlugin('getUserAuthorizationStatus', [params], resolve, reject);
            });
        },

        /**
         * purchase
         * @param {Object} params - method parameters
         * @param {Function} params.onSuccess - optional on success callback
         * @param {Function} params.onFailure - optional on failure callback
         */
        purchase: function purchase(params)
        {
            return new Promise((resolve, reject) => {
                params = defaults(params, {});

                callPlugin('purchase', [params], resolve, reject);
            });
        },

        /**
         * getPurchase
         * @param {Object} params - method parameters
         * @param {Function} params.onSuccess - optional on success callback
         * @param {Function} params.onFailure - optional on failure callback
         */
        getPurchase: function getPurchase(params)
        {
            return new Promise((resolve, reject) => {
                params = defaults(params, {});

                callPlugin('getPurchase', [params], resolve, reject);
            });
        },

        /**
         * getPurchases
         * @param {Object} params - method parameters
         * @param {Function} params.onSuccess - optional on success callback
         * @param {Function} params.onFailure - optional on failure callback
         */
        getPurchases: function getPurchases(params)
        {
            return new Promise((resolve, reject) => {
                params = defaults(params, {});

                callPlugin('getPurchases', [params], resolve, reject);
            });
        },

        /**
         * getPurchaseAvailability
         * @param {Object} params - method parameters
         * @param {Function} params.onSuccess - optional on success callback
         * @param {Function} params.onFailure - optional on failure callback
         */
        getPurchaseAvailability: function getPurchaseAvailability(params)
        {
            return new Promise((resolve, reject) => {
                params = defaults(params, {});

                callPlugin('getPurchaseAvailability', [params], resolve, reject);
            });
        },


        /**
         * getProducts
         * @param {Object} params - method parameters
         * @param {Array} params.productIds - array of product IDs to retrieve
         * @param {Function} params.onSuccess - optional on success callback
         * @param {Function} params.onFailure - optional on failure callback
         */
        getProducts: function getProducts(params)
        {
            return new Promise((resolve, reject) => {
                params = defaults(params, {});

                if (!params.productIds || !Array.isArray(params.productIds)) {
                    reject(new Error('getProducts: productIds parameter is required and must be an array'));
                    return;
                }

                callPlugin('getProducts', [params.productIds], resolve, reject);
            });
        },
    }
})();



/**
 * Helper function to call cordova plugin
 * @param {String} name - function name to call
 * @param {Array} params - optional params
 * @param {Function} onSuccess - optional on success function
 * @param {Function} onFailure - optional on failure function
 */
function callPlugin(name, params, onSuccess, onFailure)
{
    cordova.exec(function callPluginSuccess(result)
    {
        if (isFunction(onSuccess))
        {
            onSuccess(result);
        }
    }, function callPluginFailure(error)
    {
        if (isFunction(onFailure))
        {
            onFailure(error)
        }
    }, 'RustorePayPlugin', name, params);
}

/**
 * Helper function to check if a function is a function
 * @param {Object} functionToCheck - function to check if is function
 */
function isFunction(functionToCheck)
{
    var getType = {};
    var isFunction = functionToCheck && getType.toString.call(functionToCheck) === '[object Function]';
    return isFunction === true;
}

/**
 * Helper function to do a shallow defaults (merge). Does not create a new object, simply extends it
 * @param {Object} o - object to extend
 * @param {Object} defaultObject - defaults to extend o with
 */
function defaults(o, defaultObject)
{
    if (typeof o === 'undefined')
    {
        return defaults({}, defaultObject);
    }

    for (var j in defaultObject)
    {
        if (defaultObject.hasOwnProperty(j) && o.hasOwnProperty(j) === false)
        {
            o[j] = defaultObject[j];
        }
    }

    return o;
}


if (typeof module !== undefined && module.exports)
{
    module.exports = RustorePay;
}