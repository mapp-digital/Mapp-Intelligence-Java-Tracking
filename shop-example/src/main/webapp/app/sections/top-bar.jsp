<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div id="top-bar" class="container">
    <div class="row">
        <div class="span4">
            <jsp:include page="../forms/search.jsp" />
        </div>
        <div class="span8">
            <div class="account pull-right">
                <ul class="user-menu">
                    <c:if test="${user == null}">
                        <li><a href="#">My Account</a></li>
                    </c:if>
                    <c:if test="${user != null}">
                        <li><a href="#">${user.firstName} ${user.lastName}!</a></li>
                    </c:if>

                    <li><a href="basket/cart">Your Cart</a></li>
                    <li><a href="checkout">Checkout</a></li>

                    <c:if test="${user == null}">
                        <li><a href="register">Login</a></li>
                    </c:if>
                    <c:if test="${user != null}">
                        <li>
                            <jsp:include page="../forms/logout.jsp" />
                        </li>
                    </c:if>
                </ul>
            </div>
        </div>
    </div>
</div>