/*! NZ Session */
var SESSION = (function _SESSION() {
	var KEY_PREFIX = 'nz.' + FWK.user + '.',
		SESSION_KEYS = {
			user: KEY_PREFIX + 'user',
			accessiblebps: KEY_PREFIX + 'accessiblebps',
			popupBlockerDetection: KEY_PREFIX + 'popupBlockerDetection'
		};

	return {

		removeAllValues: function _removeValues() {
			var sk;

			if (window.sessionStorage && sessionStorage.removeItem) {
				for (sk in SESSION_KEYS) {
					sessionStorage.removeItem(SESSION_KEYS[sk]);
				}
			}
		},

		getValue: function _getValue(key) {
			key = SESSION_KEYS[key];
			if (window.sessionStorage && typeof sessionStorage[key] !== 'undefined' && sessionStorage.getItem && window.JSON && JSON.parse) {
				return JSON.parse(sessionStorage.getItem(key));
			}
			return null;
		},

		putValue: function _putValue(key, data) {
			key = SESSION_KEYS[key];
			if (window.sessionStorage && sessionStorage.setItem) {
				sessionStorage.setItem(key, JSON.stringify(data));
			}
		},

		removeValue: function _removeValue(key) {
			key = SESSION_KEYS[key];
			if (window.sessionStorage && sessionStorage.removeItem) {
				sessionStorage.removeItem(key);
			}
		}

	};
})();
