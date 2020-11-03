<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<script src="assets/themes/js/common.js"></script>
<script src="assets/themes/js/jquery.flexslider-min.js"></script>
<script type="text/javascript">
    $(function() {
        $('#myTab a:first').tab('show');
        $('#myTab a').click(function (e) {
            e.preventDefault();
            $(this).tab('show');
        });

        $(document).ready(function() {
            $('.flexslider').flexslider({
                animation: "fade",
                slideshowSpeed: 4000,
                animationSpeed: 600,
                controlNav: false,
                directionNav: true,
                controlsContainer: ".flex-container" // the container that holds the flexslider
            });

            try {
                $('.thumbnail').fancybox({
                    openEffect: 'none',
                    closeEffect: 'none'
                });
            } catch (e) {}

            $('#myCarousel-2').carousel({
                interval: 2500
            });

            $('#checkout').click(function (e) {
                document.location.href = "checkout";
            });

            $('#confirmation').click(function (e) {
                document.location.href = "order/confirmation";
            });

            $('#home').click(function (e) {
                document.location.href = "";
            });

            $('#search').submit(function (e) {
                document.location.href = "search/" + encodeURIComponent(this['search-term'].value);
            });

            $('#remove').click(function (e) {
                document.location.href = "basket/checkout?action=remove&id=" + this.value;
            });

            $('#new-customer').submit(function (e) {
                $(document.querySelector('.accordion-toggle[href="#collapseTwo"]')).click();
            });

            $('#returning-customer').submit(function (e) {
                $(document.querySelector('.accordion-toggle[href="#collapseTwo"]')).click();
            });

            $('#personal').submit(function (e) {
                $(document.querySelector('.accordion-toggle[href="#collapseThree"]')).click();
            });
        });
    });
</script>