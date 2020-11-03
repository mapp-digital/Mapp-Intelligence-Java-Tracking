<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<h4 class="title"><strong>Best</strong> Seller</h4>
<ul class="small-product">
    <c:forEach var="product" items="${bestseller}">
        <li>
            <a href="product_detail/${product.sku}" title="${product.name}">
                <img src="${product.url}" alt="${product.name}">
            </a>
            <a href="product_detail/${product.sku}">${product.name}</a>
        </li>
    </c:forEach>
</ul>