<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<section id="footer-bar">
    <div class="row">
        <div class="span3">
            <h4>Navigation</h4>
            <ul class="nav">
                <li><a href="">Homepage</a></li>
                <li><a href="#">About Us</a></li>
                <li><a href="contact">Contac Us</a></li>
                <li><a href="basket/cart">Your Cart</a></li>

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
        <div class="span4">
            <h4>My Account</h4>
            <ul class="nav">
                <li><a href="#">My Account</a></li>
                <li><a href="#">Order History</a></li>
                <li><a href="#">Wish List</a></li>
                <li><a href="#">Newsletter</a></li>
            </ul>
        </div>
    </div>
</section>
