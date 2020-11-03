<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<!DOCTYPE html>
<html lang="en">
	<head>
        <jsp:include page="app/sections/header.jsp" />
	</head>
    <body>
        <jsp:include page="app/sections/top-bar.jsp" />
		<div id="wrapper" class="container">
            <jsp:include page="app/sections/menu.jsp" />

            <c:choose>
                <c:when test="${fn:endsWith(requestUrl, ':8081/')}">
                    <jsp:include page="app/sections/slider.jsp" />
                </c:when>
                <c:otherwise>
                    <jsp:include page="app/sections/banner.jsp" />
                </c:otherwise>
            </c:choose>

			<section class="main-content">
				<div class="row">
					<div class="span12">

					    <c:choose>
                            <c:when test="${fn:contains(requestUrl, '/products')}">
                                <jsp:include page="app/pages/products.jsp" />
                            </c:when>
                            <c:when test="${fn:contains(requestUrl, '/product_detail/')}">
                                <jsp:include page="app/pages/product-detail.jsp" />
                            </c:when>
                            <c:when test="${fn:contains(requestUrl, '/cart')}">
                                <jsp:include page="app/pages/cart.jsp" />
                            </c:when>
                            <c:when test="${fn:contains(requestUrl, '/checkout')}">
                                <jsp:include page="app/pages/checkout.jsp" />
                            </c:when>
                            <c:when test="${fn:contains(requestUrl, '/order/')}">
                                <jsp:include page="app/pages/confirmation.jsp" />
                            </c:when>
                            <c:when test="${fn:contains(requestUrl, '/category/')}">
                                <jsp:include page="app/pages/category.jsp" />
                            </c:when>
                            <c:when test="${fn:contains(requestUrl, '/search/')}">
                                <jsp:include page="app/pages/search.jsp" />
                            </c:when>
                            <c:when test="${fn:contains(requestUrl, '/register')}">
                                <jsp:include page="app/pages/register.jsp" />
                            </c:when>
                            <c:when test="${fn:contains(requestUrl, '/contact')}">
                                <jsp:include page="app/pages/contact.jsp" />
                            </c:when>
                            <c:otherwise>
                                <jsp:include page="app/pages/home.jsp" />
                            </c:otherwise>
                        </c:choose>

                        <c:if test="${fn:endsWith(requestUrl, ':8081/')}">
						    <jsp:include page="app/sections/feature.jsp" />
						</c:if>

					</div>
				</div>
			</section>

			<c:if test="${fn:endsWith(requestUrl, ':8081/')}">
			    <jsp:include page="app/sections/manufactures.jsp" />
			</c:if>

			<jsp:include page="app/sections/footer.jsp" />
		</div>
        <jsp:include page="app/sections/script.jsp" />
    </body>
</html>