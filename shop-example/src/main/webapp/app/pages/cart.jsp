<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="row">
    <div class="span9">
        <h4 class="title"><span class="text"><strong>Your</strong> Cart</span></h4>
        <jsp:include page="../checkout/basket.jsp" />
        <hr/>
        <p class="buttons center">
            <button class="btn btn-inverse" type="submit" id="checkout">Proceed to Checkout</button>
        </p>
    </div>
    <div class="span3 col">
        <div class="block">
            <jsp:include page="../product/randomize.jsp" />
        </div>
        <div class="block">
            <jsp:include page="../product/best-seller.jsp" />
        </div>
    </div>
</div>