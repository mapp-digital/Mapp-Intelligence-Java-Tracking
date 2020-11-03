<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="row">
    <div class="span9">
        <c:if test="${categoryList != null}">
            <jsp:include page="../product/category-list.jsp" />
        </c:if>
        <c:if test="${categoryList == null}">
            <jsp:include page="../sections/404.jsp" />
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
