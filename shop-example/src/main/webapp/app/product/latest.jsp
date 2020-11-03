<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<h4 class="title">
    <span class="pull-left"><span class="text"><span class="line">Latest <strong>Products</strong></span></span></span>
    <span class="pull-right">
        <a class="left button" href="#myCarousel-2" data-slide="prev"></a><a class="right button" href="#myCarousel-2" data-slide="next"></a>
    </span>
</h4>
<div id="myCarousel-2" class="myCarousel carousel slide">
    <div class="carousel-inner">
        <div class="active item">
            <ul class="thumbnails">
                <c:forEach var="product" items="${latest}" varStatus="counter">
                    <c:if test="${counter.index < 4}">
                        <li class="span3">
                            <div class="product-box">
                                <span class="sale_tag"></span>
                                <p><a href="product_detail/${product.sku}"><img src="${product.url}" alt="" /></a></p>
                                <a href="product_detail/${product.sku}" class="title">${product.name}</a><br/>
                                <a href="category/${fn:toLowerCase(product.category)}" class="category">${product.category}</a>
                                <p class="price">${product.price}€</p>
                            </div>
                        </li>
                    </c:if>
                </c:forEach>
            </ul>
        </div>
        <div class="item">
            <ul class="thumbnails">
                <c:forEach var="product" items="${feature}" varStatus="counter">
                    <c:if test="${counter.index >= 4 && counter.index < 8}">
                        <li class="span3">
                            <div class="product-box">
                                <span class="sale_tag"></span>
                                <p><a href="product_detail/${product.sku}"><img src="${product.url}" alt="" /></a></p>
                                <a href="product_detail/${product.sku}" class="title">${product.name}</a><br/>
                                <a href="category/${fn:toLowerCase(product.category)}" class="category">${product.category}</a>
                                <p class="price">${product.price}€</p>
                            </div>
                        </li>
                    </c:if>
                </c:forEach>
            </ul>
        </div>
    </div>
</div>
