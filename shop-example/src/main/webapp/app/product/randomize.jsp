<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<h4 class="title">
    <span class="pull-left"><span class="text">Randomize</span></span>
    <span class="pull-right">
        <a class="left button" href="#myCarousel" data-slide="prev"></a><a class="right button" href="#myCarousel" data-slide="next"></a>
    </span>
</h4>
<div id="myCarousel" class="carousel slide">
    <div class="carousel-inner">
        <c:forEach var="product" items="${randomize}" varStatus="counter">
            <c:choose>
                <c:when test="${counter.index == 0}">
                    <div class="active item">
                </c:when>
                <c:otherwise>
                    <div class="item">
                </c:otherwise>
            </c:choose>
                <ul class="thumbnails listing-products">
                    <li class="span3">
                        <div class="product-box">
                            <a href="product_detail/${product.sku}"><img alt="" src="${product.url}"></a><br/>
                            <a href="product_detail/${product.sku}" class="title">${product.name}</a><br/>
                            <a href="category/${fn:toLowerCase(product.category)}" class="category">${product.category}</a>
                            <p class="price">${product.price}€</p>
                        </div>
                    </li>
                </ul>
            </div>
        </c:forEach>
    </div>
</div>
