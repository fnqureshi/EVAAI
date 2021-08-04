(function($) {
	
	// featured text
	$("#rotator .1strotate").textrotator({
		animation: "dissolve",
		speed: 4000
	});
	$("#rotator .2ndrotate").textrotator({
		animation: "dissolve",
		speed: 4000
	});
		
		
	//parallax
	if ($('#parallax1').length || $('#parallax2').length) {

		$(window).stellar({
			responsive: true,
			scrollProperty: 'scroll',
			parallaxElements: false,
			horizontalScrolling: false,
			horizontalOffset: 0,
			verticalOffset: 0
		});

	}

	// Carousel
	$('.service .carousel').carousel({
		interval: 4000
	})

	//works
	$(function() {
		Grid.init();
	});

	//animation
	new WOW().init();

})(jQuery);
