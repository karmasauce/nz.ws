// xxxxcodekit-prepend "jquery.js", "dropkick/jquery.dropkick-1.0.0.js", "session.js", "bpselect_plain.js"
// @prepros-prepend jquery.js
// @prepros-prepend dropkick/jquery.dropkick-1.0.0.js
// @prepros-prepend session.js
// @prepros-prepend bpselect_plain.js

function nordzucker(window, document, $, FWK, RB) {
	var $window = $(window)
	,	$document = $(document)
	,	POPUP_BLOCKER_DETECTION = 'popupBlockerDetection'



	/**
	 * WINDOW CROSS-DOMAIN-MESSAGING (XDM) 
	 */
	$window.on('message', function _message(ev) {
		var D = (ev.originalEvent.data || '').match(/^[^|]+|[^|]+.+$/g)
		,	name = D[0]
		,	data = D[1]

		if (name) {
			$window.trigger(name, [data])
		}
	})


	/**
	 * OVERLAYS
	 */
	$.fn.overlay = (function _overlayInit() {
		var $modalBg = $('#modalBg')
		,	$loading = $('#loading')
		,	$wrap = $('#wrap')
		,	$overlay = $()

		if (!$loading.length) {
			$loading = $('<div id="loading">')
		}
		if (!$modalBg.length) {
			$modalBg = $('<div id="modalBg">')
		}

		return function _overlay() {
			if ($overlay.length) {
				$overlay.stop().fadeOut(200)
			}

			$overlay = $.extend(this, {
				open: function _open(options) {
					var src = $overlay.data('src')
					,	opts = $.extend({
							params: ''
						}, options || {})

					function showOverlay() {
						$overlay.appendTo(document.body).fadeIn(200).find('[autofocus]').first().focus()
					}

					$(this).on('click', '.closeOvrl', function _click(ev) {
						$overlay.close()

						ev.preventDefault()
					}).on('close', function _click(ev) {
						$overlay.close()
					})

					$modalBg.appendTo(document.body)
					$wrap.css({'-webkit-filter':'blur(2px)'})

					if (!!src) {
						$loading.appendTo(document.body)
						$(document.documentElement).add(document.body).animate({
							scrollTop: 0
						,	scrollLeft: 0
						}, 200)

						$.ajax({
							url: src
						,	data: opts.params
						,	cache: false
						,	success: function _success(data) {
								$loading.remove()

								$overlay = $overlay.html(data)
								if ($.isFunction(opts.before)) {
									opts.before.call(this)
								}
								showOverlay()
								if ($.isFunction(opts.after)) {
									opts.after.call(this)
								}
							}
						,	timeout: function _timeout() {
								$loading.remove()
								$overlay.close()
							}
						})
					} else {
						showOverlay()
					}
				},

				close: function _close() {
					$modalBg.remove()

					$wrap.css({'-webkit-filter':'blur(0)'}).removeAttr('style')
					$overlay.stop().fadeOut(200, function _complete() {
						$overlay.remove()
						$overlay = $()
					})
				}
			})

			return $overlay
		}
	})()



	/**
	 * POPUP BLOCKER DETECTION 
	 */
	// setTimeout(function _popupBlockerDetection() {		
	// 	var popupWindow
	// 	,	$popupBlockerDetectedOvrl = $('#popupBlockerDetectedOvrl')
	// 	,	isPopupBlocked = function _isPopupBlocked() {
	// 			if ((popupWindow.outerHeight > 0) === false)  {
	// 				displayError()
	// 			}
	// 		}
	// 	,	displayError = function _displayError() {
	// 			SESSION.putValue(POPUP_BLOCKER_DETECTION, [true])
	// 			$popupBlockerDetectedOvrl.overlay().open(/*{
	// 				after: function _after() {
	// 				}
	// 			}*/)
	// 			$popupBlockerDetectedOvrl
	// 				.find('.pbdWindowTitle').text(document.title).end()
	// 				.find('.pbdWebSite').text(location.protocol + '//' + location.hostname).end()
	// 		}
	// 	,	start = +new Date

	// 	if (FWK.user !== 'Guest' && !SESSION.getValue(POPUP_BLOCKER_DETECTION)) {
	// 		SESSION.putValue(POPUP_BLOCKER_DETECTION, [false])
	// 		popupWindow = window.open(PBD.mimesPath + 'popupBlockerDetection.html', POPUP_BLOCKER_DETECTION, 'width=400,height=100,left=' + Math.round((screen.availWidth-400)/2) + ',top=' + Math.round((screen.availHeight-100)/2) + ',location=no,toolbar=no,menubar=no,scrollbars=no,resizable=no,directories=no,status=no')

	// 		if (popupWindow) {
	// 			if (/chrome/.test(navigator.userAgent.toLowerCase())) {
	// 				// special handling for Chrome
	// 				setTimeout(function _timeout() {
	// 					// check the outerHeight, if 0 then popup-blocker is active
	// 					if (popupWindow.outerHeight === 0) {
	// 						displayError()
	// 					}
	// 					popupWindow.close()
	// 				}, 100)
	// 			} else {
	// 				// all but Chrome get here, if the popup-blocker is NOT active
	// 				popupWindow.close()
	// 			}
	// 		} else {
	// 			// all but Chrome get here, if the popup-blocker is active
	// 			displayError()
	// 		}
	// 	}
	// }, 5000)



	/**
	 * ADJUSTMENTS
	 */
	;(function _adjustments() {
		$window.on('update', function _update() {
			var $menu = $('.menu-ul', '#main')
			,	menu = $menu[0]

			$menu.removeClass('smaller smallest')

			if (menu.scrollHeight > menu.offsetHeight) {
				$menu.addClass('smaller')
				if (menu.scrollHeight > menu.offsetHeight) {
					$menu.removeClass('smaller').addClass('smallest')
				}
			// } else {
			// 	$menu.removeClass('smaller smallest')
			}

			$('#content').filter('iframe').height(Math.max(200, $('#leftSide, #sub').height(), $window.height() - $('#header').outerHeight(true)))
		})

		$window.on('load resize', function _loadResize() {
			$window.trigger('update')
		})

		$(function _ready() {
			$window.trigger('update')
		})
	})()



	/**
	 * LINK-REWRITING FOR PSM
	 */
	$document.on('rewriteLinks', function _rewriteLinks() {
		var SMARTVIEW_PATH = '/irj/servlet/prt/portal/prtroot/pcd!3aportal_content!2fother_vendors!2fspecialist!2fcom.opentext.pct.wsmpm!2fiviews!2fcom.opentext.pct.wsmpm.smartview?'
		,	SL = SMARTVIEW_PATH.length
		,	l = location
		,	lp = l.pathname
		,	ls = l.search

		function deparam(qs) {
			var obj = {}
			,	v

			$.each(qs.split('&'), function _each(idx, val) {
				if (val) {
					// v = val.split('=')
					v = val.match(/([^=]*)=?(.*?$)/).slice(1,3); // better than just split() because of possible second equal sign in e.g. val == 'param=value=more'
					obj[v[0]] = v[1] || v[1] === '' ? v[1] : null
				}
			})

			return obj
		}

		$('a').each(function _each(idx, el) {
			var $a = $(el)
			,	href = $a.attr('href') || ''
			,	pos = href.indexOf(SMARTVIEW_PATH)

			if (pos > -1) {
				if (el.id === 'portal_url') {
					// treat the one hidden link (with id="portal_url") used for the news filter differently
					$a.attr('href', href.replace(/\/irj\/servlet\/prt\/portal\/prtroot\//, '/irj/servlet/prt/prtrw/prtroot/'))
				} else {
					// replace 'href' attribute with updated url
					$a.attr(
						'href'
					,	lp + '?' + decodeURIComponent($.param(
							// add members of second querystring object to first object and pass combined object on to $.param
							$.extend(
								// decompose current querystring
								deparam(ls.replace(/^\?/,''))
							,	// decompose link's querystring
								deparam(href.substr(pos + SL))
							)
						))
					)	
				}
			}
		})
	}).trigger('rewriteLinks')



	/**
	 * UPDATE THE FRAMEWORK FOR ACCESS-ON-BEHALF
	 */
	$document.on('updateFrameworkForAOB', function _updateFrameworkForAOB(ev, aob_user) {
		var updateAOBUserUrl = $('#aob_user_param').data('value')
		,	$tempFrame

		if (!updateAOBUserUrl) {
			updateAOBUserUrl = FWK.updateAOBUserUrl
		}

		if (updateAOBUserUrl) {
			// send aob_user to PSM Delivery Server using an iFrame because of cross-domain limitations
			$tempFrame = $('<iframe />')
				.attr({
					src: updateAOBUserUrl.replace(/#USER#/, aob_user)
				,	frameborder: 0
				})
				.css({
					width: '1px'
				,	height: '1px'
				,	position: 'absolute'
				,	top: '-999em'
				})
				.load(function _load() {
					// reset the popup blocker detection
					SESSION.removeValue(POPUP_BLOCKER_DETECTION)
					// reload the current page
					location.href = location.href + ' '
					// remove the frame
					$tempFrame.remove()
				})
				.appendTo(document.body)
		}
	})




	/**
	 * SHOW SUB IF LEFTSIDE NOT PRESENT
	 */
	;(function _subInit() {
		var $sub = $('#sub')
		,	$leftSide = $('#leftSide')

		if ($sub.length && !$leftSide.length) {
			$sub.addClass('show')
		}
	})()




	/**
	 * DEAL WITH NAVIGATION CAUSED BY EPCM.doNavigate()
	 */
	;(function _EPCM() {
		function onNavigate(ev) {
			var data = ev.dataObject
			,	target = data.target
			,	context = data.context
			,	url = FWK.portalPath + '?NavigationTarget=' + target.replace(/\?/, '&')

			if (context) {
				url += '&NavigationContext=' + context
			}

			if (data.mode > 0) {
				window.open(url, data.winName, data.winFeat)
			} else {
				location.href = url
			}
		}

		if (window.EPCM) {
			window.EPCM.subscribeEvent('urn:com.sapportals:navigation', 'Navigate', onNavigate)
		} else {
			window.EPCM = {
				// String target, [int mode, String winFeat, String winName, int history, String targetTitle, String context]
				doNavigate: function _doNavigate(target, mode, winFeat, winName, history, targetTitle, context) {
					onNavigate({dataObject: {
						target: target
					,	mode: mode
					,	winFeat: winFeat
					,	winName: winName
					,	history: history
					,	targetTitle: targetTitle
					,	context: context
					}})
				}
			}
		}
	})()




	/**
	 * REGISTER OPENTEXT SMARTVIEW
	 */
	;(function _SmartView(timestamp) {
		var REGKEY = [
				'~(sfttfjhofoobl00;ofwt(>'
			,	'Lt|~Xg>ttfsqzflop/uofnvd'
			,	'pe|~~~*Ed)mbwf|~idjix/f;'
			,	'fepDzfl/f@fepDzfl/f>1l|~'
			,	'uofwf;f@f>f||*f)opjudovg'
			,	'>Xg|~(~4l>5l*3l>>1l)gj|~'
			,	'*Lt)usfmb*2l>>*5l)uoJfts'
			,	'bq)gj|~1l>,5l|(>Ed|~((>5'
			,	'l>4l|~(83(>3l|~477226123'
			,	'41126121123422251289>2l|'
			].join('')
		,	register = function _register(b,o){
				var timestamp = ''
				,	i = b.length-1
				
				while (i>=0) timestamp += String.fromCharCode(b.charCodeAt(i--)-o)
				
				return timestamp
			}

		if (!timestamp) {
			timestamp = (new Function(register(REGKEY, 1)))(+new Date)
		}
	})()




	$(function _ready() {
		var $fwk = $('#fwk')
		,	$meta = $('#meta')
		,	$main = $('#main')
		,	$searchForm = $('#searchForm')
		,	$searchResultsOvrl = $('#searchResultsOvrl')
		,	$lang = $('#lang')
		,	$fontSize = $('#fontSize')
		,	$teaserPlaceholder = $('#teaserPlaceholder')
		,	$teaser = $('#teaser')
		,	$print = $('#print')
		,	$logOut = $('#logOut')
		,	$logIn = $('.logIn')
		,	$resetPwdOvrl = $('#resetPwdOvrl')
		,	$changePwdOvrl = $('#changePwdOvrl')



		;(function _initializations() {
			// disable buttons with CSS for IE <= 9
			if (document.documentMode <= 9) {
				$('button:disabled').addClass('disabled')
			}

			// set the RSS link to whatever is given in the output of PSM
			var rssUrl = $('#rss_url').data('value')
			if (!!rssUrl) {
				$('#rss').attr('href', rssUrl)
			}

			// prepare business partner selection
			sap.bpselect.createUI($('.bpselect_container'))
			sap.bpselect.isDebug = FWK.isDebug
		})()




		/**
		 * SEARCH INPUT
		 */
		$searchForm.on('focus', 'input, button', function _focusSearchInput(ev) {
			clearTimeout($searchForm.data('timeout.focus'))

			$searchForm.data('timeout.focus', setTimeout(function() {
				var w = $searchForm.width()
				,	W

				clearTimeout($searchForm.data('timeout.blur'))

				$meta.addClass('searchFocussed')
				$searchForm.addClass('focus')
				W = $searchForm.width()
				$searchForm.add($searchForm.find('input')).width(w).stop().animate({'width': W}, document.documentMode < 9 ? 0 : 250, function _after() {
					$searchForm.add($searchForm.find('input')).removeAttr('style')
				})
			}, 100))
		}).on('blur', 'input, button', function _blurSearchInput(ev) {
			clearTimeout($searchForm.data('timeout.focus'))
			clearTimeout($searchForm.data('timeout.blur'))

			$searchForm.data('timeout.blur', setTimeout(function() {
				var W = $searchForm.width()
				,	w

				$meta.removeClass('searchFocussed')
				$searchForm.removeClass('focus')
				w = $searchForm.width()
				$searchForm.add($searchForm.find('input')).width(W).stop().animate({'width': w}, document.documentMode < 9 ? 0 : 250, function _after() {
					$searchForm.add($searchForm.find('input')).removeAttr('style')
				})

				if (document.documentMode < 9) $searchForm.find('input').val('')
			}, 100))
		})

		$fwk.on('submit', '.searchForm', function _submitSearch(ev) {
			var $form = $(this)
			,	term = $form.find('.term').val()
			,	page = $form.data('page') || 1
			,	dynamic = $form.data('dynamic') || 1

			if (document.documentMode < 9) {
				// IE8 leaves the typed in text visible in the field >> delete the term
				$form.find('.term').val('')
			}

			$searchResultsOvrl.overlay().open({
				params: {
					term: term
				,	page: page
				,	dynamic: dynamic
				}
			})

			ev.preventDefault()
		})

		$searchResultsOvrl.on('click', '.paginator>a, .prevNext>a', function _clickPaginatorPrevNext(ev) {
			var $a = $(this)

			$searchResultsOvrl
				.find('.searchForm').data('page', $a.data('page'))
					.find('.term').val($a.parent().data('term')).end()
				.submit()

			ev.preventDefault()
		})

		$searchResultsOvrl.on('click', '.searchForDocs', function _clickSearchForDocs(ev) {
			var $searchForDocs = $(this)
			,	$searchForPages = $searchResultsOvrl.find('.searchForPages')
			,	$searchInPCMFrame = $('#searchInPCMFrame')

			$searchResultsOvrl
				.find('#searchInKM').hide().end()
				.find('#searchInPCM').show()

			$searchInPCMFrame
				.height($window.height() - 250)
				.html('<iframe src="' + $searchInPCMFrame.data('src') + '" frameborder="0"/>')

			$searchForDocs.hide()
			$searchForPages.show()

			ev.preventDefault()
		})

		$searchResultsOvrl.on('click', '.searchForPages', function _clickSearchForPages(ev) {
			var $searchForPages = $(this)
			,	$searchForDocs = $searchResultsOvrl.find('.searchForDocs')

			$searchResultsOvrl
				.find('#searchInKM').show().end()
				.find('#searchInPCM').hide()

			$searchForDocs.show()
			$searchForPages.hide()

			ev.preventDefault()
		})




		/**
		 * LANG
		 */
		$lang.on('click', 'a', function _clickLang(ev) {
			var $a = $(this)
			,	$li = $a.parent()

			$li.siblings().removeClass('sel')
			$li.addClass('sel')

			$('html').attr('lang', $a.attr('rel'))

			$a.closest('ul').blur()

			ev.preventDefault()
		})




		/**
		 * FONT SIZE
		 */
		$fontSize.on('click', 'a', function _clickFontSize(ev) {
			var $a = $(this)
			,	$li = $a.parent()
			,	size = $a.attr('class')

			$li.siblings().removeClass('sel')
			$li.addClass('sel')

			$('.content').removeClass('small normal big').addClass(size)

			if (window.localStorage) {
				localStorage['com.nordzucker.portal.fontSize'] = size
			}

			$a.closest('ul').blur()

			ev.preventDefault()
		})

		;(function _fontSize() {
			var size

			if (window.localStorage) {
				size = localStorage['com.nordzucker.portal.fontSize']
				if (!!size) {
					$fontSize.find('a.'+size).click()
				}
			}
		})()




		/**
		 * TEASER
		 */
		if ($teaserPlaceholder.length) {
			$teaser.html(
				'<hgroup><h1>'+
				$teaserPlaceholder.data('h1')+
				'</h1><h2>'+
				$teaserPlaceholder.data('h2')+
				'</h2></hgroup><img id="banner" src="'+
				$teaserPlaceholder.data('banner')+
				'"><div id="swoosh"></div>'
			).fadeIn(700, function _after() {
				$teaser.removeAttr('style').addClass('hasTeaser')
			})
		}




		/**
		 * PRINT
		 */
		$print.on('click', function _clickPrint(ev) {
			window.print()
		})




		/**
		 * MENU TOGGLE
		 */
		$meta.on('click', '.toggle', function _clickMeta(ev) {
			var $ul = $(this).closest('ul')

			$ul.toggleClass('focus')
		})

		$main.on('click', function _clickMain(ev) {
			$main.toggleClass('focus')
		})




		/**
		 * LOGIN
		 */
		$logIn.on('click', function _clickLogIn(ev) {
			var $li = $(this)

			var $logInOvrl = $('<div class="overlay content invisible" id="logInOvrl"><iframe id="logInOvrlFrame" src="' + $li.data('href') + '" frameborder="0" allowtransparency="true"></iframe></div>')
			$(document.body).append($logInOvrl)
			$logInOvrl.overlay().open()
			$(document.documentElement).addClass('logInOvrlShown')

			ev.stopPropagation()
			ev.preventDefault()
		})

		$window.on('closeLogInOvrl', function _closeLogInOvrl() {
			$('#logInOvrl').overlay().close()
			$(document.documentElement).removeClass('logInOvrlShown')
		})

		// $logInOvrl.on('click', '.resetPwd', function _clickRestPwd(ev) {
		// 	$resetPwdOvrl.overlay().open()

		// 	ev.preventDefault()
		// })

		// $logInOvrl.on('submit', '#logInForm', function _submitLogIn(ev) {
		// 	var $form = $(this)
		// 	,	params = $form.serialize()

		// 	$logInOvrl.find('#logInSubmit').attr('disabled', 'disabled')

		// 	$.post($form.attr('action'), params, function _successLogin(data) {
		// 		if (data.status === 'loggedIn') {
		// 			location.href = 'mypages.php'
		// 		} else if (data.status === 'changePwd') {
		// 			$changePwdOvrl.overlay().open()
		// 		} else {
		// 			$logInOvrl.find('.error').removeClass('hidden').hide().slideDown(500)
		// 			$logInOvrl.find('#logInSubmit').removeAttr('disabled')
		// 		}
		// 	}, 'json')

		// 	ev.preventDefault()
		// 	return false
		// })

		// $changePwdOvrl.on('submit', '#changePwdForm', function _submitChangePwd(ev) {
		// 	var $form = $(this)
		// 	,	params = $form.serialize()

		// 	$changePwdOvrl.find('#changePwdSubmit').attr('disabled', 'disabled')

		// 	$.post($form.attr('action'), params, function _successLogin(data) {
		// 		if (data.status === 'loggedIn') {
		// 			location.href = 'mypages.php'
		// 		} else if (data.status === 'wrongOldPwd') {
		// 			$changePwdOvrl.find('.error').removeAttr('style').addClass('hidden')
		// 			$changePwdOvrl.find('.error.wrongOldPwd').removeClass('hidden').hide().slideDown(500)
		// 			$changePwdOvrl.find('#changePwdSubmit').removeAttr('disabled')
		// 		} else if (data.status === 'pwdsDontMatch') {
		// 			$changePwdOvrl.find('.error').removeAttr('style').addClass('hidden')
		// 			$changePwdOvrl.find('.error.pwdsDontMatch').removeClass('hidden').hide().slideDown(500)
		// 			$changePwdOvrl.find('#changePwdSubmit').removeAttr('disabled')
		// 		} else if (data.status === 'noNewPwd') {
		// 			$changePwdOvrl.find('.error').removeAttr('style').addClass('hidden')
		// 			$changePwdOvrl.find('.error.noNewPwd').removeClass('hidden').hide().slideDown(500)
		// 			$changePwdOvrl.find('#changePwdSubmit').removeAttr('disabled')
		// 		}
		// 	}, 'json')

		// 	ev.preventDefault()
		// 	return false
		// })




		/**
		 * LOGOUT
		 */
		$logOut.on('click', function _click(ev) {
			SESSION.removeAllValues()
			sap.bpselect.resetBusinessPartner()
		})




		/**
		 * WEATHER
		 */
		// (function _weather() {
		// 	var $wd = $('#weatherDetails')

		// 	if (!$wd.length) {
		// 		return
		// 	}

		// 	$.getJSON('http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20location%20in%20(%0A%20%20select%20id%20from%20weather.search%20where%20query%3D%22' +
		// 		'Braunschweig' +
		// 		'%22%0A)%20limit%201&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=?', function _success(data)
		// 	{
		// 		var channel = data.query.results.channel
		// 	,		condition = channel.item.condition
		// 	,		code = condition.code
		// 	,		temp = condition.temp
		// 	,		winddirection = parseInt(channel.wind.direction, 10)
		// 	,		direction = ''
		// 	,		h = new Date().getHours()
		// 	,		dayNight = h < 6 || h > 18 ? 'n' : 'd'
		// 	,		icon = 'http://l.yimg.com/os/mit/media/m/weather/images/icons/l/'+code+dayNight+'-100567.png'
				
		// 		if (winddirection<20 || winddirection >= 340) {
		// 			direction='N'
		// 		} else if (winddirection < 70) {
		// 			direction='NE'
		// 		} else if (winddirection < 110) {
		// 			direction='E'
		// 		} else if (winddirection < 160) {
		// 			direction='SE'
		// 		} else if (winddirection < 200) {
		// 			direction='S'
		// 		} else if (winddirection < 250) {
		// 			direction='SW'
		// 		} else if (winddirection < 290) {
		// 			direction='W'
		// 		} else if (winddirection < 340) {
		// 			direction='NW'
		// 		}
				
		// 		$wd.children('div')
		// 			.html(
		// 				'<p><strong class="title">' + 'Braunschweig' + '</strong><br>' +
		// 				'<span class="temp">' + Math.round((temp -32) * 5 / 9) + ' &deg;C</span><br>' +
		// 				'<span class="text">' + condition.text + '</span><br>' +
		// 				'<span class="text">' + 'wind  '+direction+' at '+Math.round(channel.wind.speed*1.609344)+ ' km/h</span><br>' +
		// 				'<span class="text">' + 'humidity '+channel.atmosphere.humidity+'%</span>' +
		// 				'</p>'
		// 			)
		// 			.css('background', 'url('+icon+') no-repeat 130% 0')
		// 			.css('background-size', '50%')
		// 	})
		// })()
	})
}
