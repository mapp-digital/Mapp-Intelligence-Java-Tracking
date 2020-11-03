<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<table class="table table-striped">
    <thead>
        <tr>
            <th>Remove</th>
            <th>Image</th>
            <th>Product Name</th>
            <th>Quantity</th>
            <th>Unit Price</th>
            <th>Total</th>
        </tr>
    </thead>
    <tbody>
        <c:set var="total" value="0"></c:set>
        <c:forEach var="item" items="${sessionScope.cart}">
            <c:set var="total" value="${total + item.product.price * item.quantity}"></c:set>
            <tr>
                <td><input type="checkbox" value="${item.product.id}" id="remove"></td>
                <td><a href="product_detail/${item.product.sku}"><img alt="" src="${item.product.url}"></a></td>
                <td>${item.product.name}</td>
                <td><input type="text" placeholder="${item.quantity}" value="${item.quantity}" class="input-mini"></td>
                <td>${item.product.price}€</td>
                <td>${item.product.price * item.quantity}€</td>
            </tr>
        </c:forEach>
        <tr>
            <td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td>
            <td><strong>${total}€</strong></td>
        </tr>
    </tbody>
</table>
