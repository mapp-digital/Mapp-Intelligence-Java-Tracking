<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<div class="row">
    <div class="span9">
        <c:if test="${product != null}">
            <div class="row">
                <div class="span4">
                    <a href="${product.url}" class="thumbnail" data-fancybox-group="group1" title="Description 1"><img alt="" src="${product.url}"></a>
                    <ul class="thumbnails small">
                        <li class="span1">
                            <a href="${product.url}" class="thumbnail" data-fancybox-group="group1" title="Description 2"><img src="${product.url}" alt=""></a>
                        </li>
                    </ul>
                </div>
                <div class="span5">
                    <h4><strong>${product.name}€</strong></h4>
                    <address>
                        <strong>Category:</strong> <a href="category/${fn:toLowerCase(product.category)}" class="category"><span>${product.category}</span></a><br>
                        <strong>Product ID:</strong> <span>Product ${product.id}</span><br>
                        <strong>Product SKU:</strong> <span>${product.sku}</span><br>
                    </address>
                    <h4><strong>Price: ${product.price}€</strong></h4>
                </div>
                <div class="span5">
                    <form class="form-inline" action="basket/cart" method="GET">
                        <input type="hidden" name="id" value="${product.id}">
                        <input type="hidden" name="sku" value="${product.sku}">
                        <input type="hidden" name="action" value="add">

                        <label>Qty:</label>
                        <input type="text" class="span1" name="qty" placeholder="1" value="1" required />
                        <button class="btn btn-inverse" type="submit">Add to cart</button>
                    </form>
                </div>
            </div>
            <div class="row">
                <div class="span9">
                    <ul class="nav nav-tabs" id="myTab">
                        <li class="active"><a href="#home">Description</a></li>
                        <li class=""><a href="#profile">Additional Information</a></li>
                    </ul>
                    <div class="tab-content">
                        <div class="tab-pane active" id="home">${product.description}<br /><br /></div>
                        <div class="tab-pane" id="profile">
                            <table class="table table-striped shop_attributes">
                                <tbody>
                                    <tr class="">
                                        <th>Size</th>
                                        <td>Large, Medium, Small, X-Large</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
                <div class="span9">
                    <jsp:include page="../product/related.jsp" />
                </div>
            </div>
        </c:if>
        <c:if test="${product == null}">
            <div class="row">
                <div class="span9">
                    <jsp:include page="../sections/404.jsp" />
                </div>
            </div>
        </c:if>
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
