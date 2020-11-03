<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<h1 class="center"><strong>Search results for '${search}' (${searchListResult} results)</strong></h1>
<ul class="thumbnails listing-products">
    <c:forEach var="product" items="${searchList}">
        <li class="span3">
            <div class="product-box">
                <span class="sale_tag"></span>
                <a href="product_detail/${product.sku}"><img alt="" src="${product.url}"></a><br/>
                <a href="product_detail/${product.sku}" class="title">${product.name}</a><br/>
                <a href="category/${fn:toLowerCase(product.category)}" class="category">${product.category}</a>
                <p class="price">${product.price}€</p>
            </div>
        </li>
    </c:forEach>
</ul>
