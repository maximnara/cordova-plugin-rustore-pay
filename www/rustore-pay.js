let RustorePay = (function () {
    return {
        events: {
            // Define your plugin events here
            // example: {
            //     loaded: 'exampleDidLoad',
            //     failed: 'exampleDidFail',
            // }
        },



        /**
         * Example method - replace with your plugin methods
         * @param {Object} params - method parameters
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
         * @param {string} params.productId - product ID to purchase (required)
         * @param {number} params.quantity - quantity to purchase (optional, default: 1)
         */
        purchase: function purchase(params)
        {
            return new Promise((resolve, reject) => {
                params = defaults(params, {});

                if (!params.productId || typeof params.productId !== 'string' || params.productId.trim() === '') {
                    reject(new Error('purchase: productId parameter is required and must be a non-empty string'));
                    return;
                }

                if (params.quantity !== undefined && (!Number.isInteger(params.quantity) || params.quantity < 1)) {
                    reject(new Error('purchase: quantity parameter must be a positive integer'));
                    return;
                }

                // Устанавливаем значение по умолчанию для quantity если не указано
                if (params.quantity === undefined) {
                    params.quantity = 1;
                }

                callPlugin('purchase', [params], resolve, reject);
            });
        },


        /**
         * getPurchases
         * @param {Object} params - method parameters
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

        /**
         * openRuStoreDownloadInstruction
         * @param {Object} params - method parameters
         */
        openRuStoreDownloadInstruction: function openRuStoreDownloadInstruction(params)
        {
            return new Promise((resolve, reject) => {
                params = defaults(params, {});

                callPlugin('openRuStoreDownloadInstruction', [params], resolve, reject);
            });
        },

        /**
         * openRuStore
         * @param {Object} params - method parameters
         */
        openRuStore: function openRuStore(params)
        {
            return new Promise((resolve, reject) => {
                params = defaults(params, {});

                callPlugin('openRuStore', [params], resolve, reject);
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