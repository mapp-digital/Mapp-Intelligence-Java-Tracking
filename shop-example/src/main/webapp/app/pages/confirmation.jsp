<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="row">
    <div class="span12">
        <h1 class="center"><strong>Your order has been received.</strong></h1>
        <h3 class="center">Thank you for your purchase!</h3>
        <p class="center">
            Your order # is: ${order_id}.<br/>
            You will receive an order confirmation email with details of your order and a link to track its progress.
        </p>
        <hr/>
        <p class="buttons center">
            <button class="btn btn-inverse" type="submit" id="home">Continue Shopping</button>
        </p>
    </div>
</div>
<div class="row">
    <div class="span12">
        <jsp:include page="../product/feature.jsp" />
    </div>
</div>