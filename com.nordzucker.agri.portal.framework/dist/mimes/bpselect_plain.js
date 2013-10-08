/*! Business Partner Selection */
if (!window.sap) sap = {};
sap.bpselect = {};

(function (window, document, undefined) {
	var _bpselect = {};

	_bpselect.availableBusinessPartners = null;
	_bpselect.user = null;
	_bpselect.serviceroot = '/nordzucker.com~common~connector~service/api/';

	_bpselect.getUnique = function() {
		return '?_=' + (+new Date());
	};


	/**
	 * Retrieves some information about the currently logged in user.
	 * 
	 * @params  callback The callback function which is called when a return value is ready
	 *          Callback param could be null => error, or the user object as JSON
	 */
	_bpselect.getUser = function(callback) {
		var url, user;

		if (_bpselect.user) {
			if (callback) callback(_bpselect.user);
			return _bpselect.user;
		}


		user = SESSION.getValue('user');
		if (user) {
			_bpselect.user = user;
			if (callback) callback(user);
		} else {
			url = _bpselect.serviceroot + 'user' + _bpselect.getUnique();
			$.get(url)
				.success(function(user, status, jqXHR) {
					if (!user || !user.userid && !!callback) callback(null);
					_bpselect.user = user;
					SESSION.putValue('user', user);

					_bpselect.setBusinessPartner(user.userid);

					if (callback) callback(user);
				})
				.error(function(jqXHR, status, error) {
					var user;

					//IE Problem, sometimes status is good but it runs in here...
					if (jqXHR.responseText) {
						try {
							user = JSON.parse(jqXHR.responseText);
							if (!user || !user.userid) callback(null);
							_bpselect.user = user;
							SESSION.putValue('user', user);
							if (callback) callback(_bpselect.user);
						}
						catch (ex) {
							if (callback) callback(null);
						}
					}
					else {
						if (callback) callback(null);
					}
				});
		}
	};


	/**
	 * Checks the access of a given business partner id, i.e. if the current user is allowed
	 * to switch to that business partner.
	 * 
	 * @params id        The business partner id which should be checked
	 * @params callback  The callback function which is called when a return value is ready
	 *                   Callback param could be null => error, true or false
	 */
	_bpselect.checkBusinessPartnerID = function(id, callback) {
		var url;

		if (!$.trim(id)) {
			throw 'Business Partner ID must be given.';
		}

		url = _bpselect.serviceroot + 'canaccessbp/' + id + _bpselect.getUnique();
		$.get(url)
			.success(function(data, status, jqXHR) {
				if (data && data.boolean && data.boolean === 'X') {
					if (callback) callback(true);
				}
				else {
					if (callback) callback(false);
				}
			})
			.error(function(jqXHR, status, error) {
				if (callback) callback(null);
			});
	};

	/**
	 * Retrieves a list of business partners, if not already existent.
	 * 
	 * @params	callback  The callback function which is called when a return value is ready
	 *                    Callback param could be null => error, or an array of business partner ids
	 */
	_bpselect.getAvailableBusinessPartners = function(callback) {
		var url, accessiblebps;

		function process(accessiblebps, callback) {
			var i, l, le;

			_bpselect.availableBusinessPartners = [];

			if (accessiblebps && accessiblebps.list) {
				l = accessiblebps.list.length;
				if (l && l > 0) {
					for (i = l-1; i >= 0; i--) {
						le = accessiblebps.list[i];
						if (le && le.option === 'EQ' && le.low) {
							_bpselect.availableBusinessPartners.push(+le.low); // store as integer to get rid of leading zeros
						}
					}

					_bpselect.availableBusinessPartners.sort();

					if (callback) callback(_bpselect.availableBusinessPartners);
				}
				else {
					if (callback) callback(_bpselect.availableBusinessPartners);
				}
			}
			else {
				if (callback) callback(null);
			}
		}

		_bpselect.availableBusinessPartners = null;
		if (_bpselect.availableBusinessPartners && callback) {
			callback(_bpselect.availableBusinessPartners);
			return _bpselect.availableBusinessPartners;
		}

		accessiblebps = SESSION.getValue('accessiblebps');
		if (accessiblebps) {
			process(accessiblebps, callback);
		} else {
			url = _bpselect.serviceroot + 'getaccessiblebps' + _bpselect.getUnique();
			$.get(url)
				.success(function(accessiblebps, status, jqXHR) {
					process(accessiblebps, callback);

					if (accessiblebps) {
						SESSION.putValue('accessiblebps', accessiblebps);
					}
				})
				.error(function(jqXHR, status, error) {
					if (callback) callback(null);
				});
		}
	};


	/**
	 * Resets the business partner to the (still) logged in user.
	 */
	_bpselect.resetBusinessPartner = function() {
		_bpselect.setBusinessPartner(_bpselect.user.userid);
	};


	/**
	 * Tries to write back the given business partner id to the server.
	 * The value should be first validated with validateInput(input, callback)!
	 * 
	 * @params bpid      The business partner id to write back
	 * @params callback  The callback function which is called when a return value is ready
	 */
	_bpselect.setBusinessPartner = function(bpid, callback) {
		var url;

		if (!bpid) {
			throw 'Business Partner ID must be given.';
		}

		if (!_bpselect.user) {
			throw 'User must first be fetched.';
		}

		if (bpid === _bpselect.user.selectedBP) {
			return;
		}

		url = _bpselect.serviceroot + 'selected' + _bpselect.getUnique();
		$.post(url, {
				'id': $.trim(bpid).replace(/^0+/, ''),
				'token': _bpselect.user.token
			}, function(data, status, jqXHR) {
				if (callback) callback(true);
			})
			.error(function(jqXHR, status, error) {
				if (callback) callback(null);
			});

		// trigger the reload/update of the framework, handled in nz.js 
		$(document).trigger('updateFrameworkForAOB', [bpid]);
	};


	/**
	 * Retrieves the current UI Component type which should be used, and
	 * - fetches a user if not already existent
	 * - fetches all accessible business partners if not already existent
	 * 
	 * @params callback	The callback function which is called when a return value is ready
	 * 
	 * Callback params:	1st     2nd
	 * Errors occured:  null
	 * Label:           1,      the Label Content
	 * TextInput:       2,      the currently selected BP (if available)
	 * DropDown Box:    3,      an array of businessPartner ids
	 */
	_bpselect.getUIType = function(callback) {
		_bpselect.getUser(function(user) {
			if (!user) {
				if (callback) callback(null);
				return null;
			}

			//User is an employee, show Textinput
			if (user.isEmployee) {
				if (callback) callback(2, user.selectedBP);
				return 2;
			}

			_bpselect.getAvailableBusinessPartners(function(businessPartners) {
				if (!businessPartners) {
					if (callback) callback(null);
					return null;
				}

				if (businessPartners.length === 0) {
					if (callback) callback(2, user.selectedBP ? user.selectedBP : user.userid);
					return 2;
				}
				else if (businessPartners.length === 1) {
					if (callback) callback(1, user.userid);
					return 1;
				}
				else if (businessPartners.length > 10) {
					if (callback) callback(2, user.selectedBP);
					return 2;
				}
				else {
					if (callback) callback(3, businessPartners);
					return 3;
				}
			});
		});
	};


	/**
	 * Validates the given input string
	 * 
	 * @parmas	id          The business partner id which should be validated
	 * @params	callback    The callback function which is called when a return value is ready
	 *                      First callback parameter is true or false, if query fails also null
	 *                      Second callback parameter is a resource string which could be used for error messages
	 */
	_bpselect.validateInput = function(bpid, callback) {
		var input = $.trim(bpid + '').replace(/^0+/, ''),
			isValid;

		if (input.length === 0) {
			if (callback) callback(false, 'sap.bpselect.empty');
			return false;
		}

		if (input.length > 10) {
			if (callback) callback(false, 'sap.bpselect.toolong');
			return false;
		}

		isValid = /^[0-9]+$/.test(input);
		if (!isValid) {
			if (callback) callback(false, 'sap.bpselect.notanumber');
			return false;
		}

		_bpselect.checkBusinessPartnerID(input, function(valid) {
			if (valid === null) {
				if (callback) callback(null, 'sap.bpselect.error');
				return null;
			}
			if (valid === true) {
				if (callback) callback(true, 'sap.bpselect.allowed');
				return true;
			}
			else {
				if (callback) callback(false, 'sap.bpselect.notallowed');
				return false;
			}
		});
	};

	_bpselect.error = function(msg) {
		if (sap.bpselect.isDebug) alert('ERROR: ' + msg);
	};

	_bpselect.change = function(bpid) {
		_bpselect.validateInput(bpid, function(check, msg) {
			if (check) {
				_bpselect.setBusinessPartner(bpid, function(check) {
					if (!check) _bpselect.error('_bpselect.setBusinessPartner');

					// // reset the session values
					// SESSION.removeAllValues();

					// set the 'selectedBP' on the user object in the session storage as well
					var user = SESSION.getValue('user');
					user.selectedBP = bpid;
					SESSION.putValue('user', user);
				});
			}
			else {
				_bpselect.error('_bpselect.change > ' + msg);
			}
		});
	};

	_bpselect.createUI = function($container) {
		if (!$container || !$container.length) return;

		_bpselect.getUIType(function(type, value) {
			var $label = $('#meta .bpselect_label'),
				$printableBP = $('#printableBPLabel, #printableBP');

			$label.hide();
			$printableBP.hide();
			switch (type) {
				default:
				case null:
					_bpselect.error('_bpselect.getUIType > case null');
					break;

				case 1:
					// $container.text(value);
					break;

				case 2:
					$label.show();
					$printableBP.show().filter('span').text(+_bpselect.user.selectedBP);
					var input = $('<input type="text" id="bpselect_text" autocomplete="on" maxlength="10" min="0" pattern="^[0-9]+$">');
					input.val(+_bpselect.user.selectedBP).on('change', function() {
						_bpselect.change(
							$('#bpselect_text').val()
						);
					}).on('keypress', function(ev) {
						if (ev.which === 13) {
							_bpselect.change(
								$('#bpselect_text').val()
							);
						}
					});
					input.appendTo($container.empty());
					break;

				case 3:
					$label.show();
					$printableBP.show().filter('span').text(+_bpselect.user.selectedBP);
					var ddl = $('<select id="bpselect_ddl"></select>');
					$(value).each(function(index, item) {
						var option = $('<option>' + item + '</option>');
						if (item === +_bpselect.user.selectedBP) { // convert selectedBP to integer
							option.attr('selected', 'true');
						}
						option.appendTo(ddl);
					});
					ddl.appendTo($container.empty());
					if ($.fn.dropkick) {
						$('#bpselect_ddl').dropkick({
							change : function() {
								_bpselect.change(
									$('#bpselect_ddl').val()
								);
							},
							width:'72px'
						});
					}
					break;
			}
		});
	};

	sap.bpselect.getUser = _bpselect.getUser;
	sap.bpselect.validateInput = _bpselect.validateInput;
	sap.bpselect.getUIType = _bpselect.getUIType;
	sap.bpselect.setBusinessPartner = _bpselect.setBusinessPartner;
	sap.bpselect.resetBusinessPartner = _bpselect.resetBusinessPartner;
	sap.bpselect.createUI = _bpselect.createUI;
})(window, document);
