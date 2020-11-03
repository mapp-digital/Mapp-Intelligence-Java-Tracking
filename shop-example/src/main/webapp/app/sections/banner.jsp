<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<section class="header_text sub">
    <img class="pageBanner" src="assets/themes/images/pageBanner.png" alt="New products" >

    <c:if test="${info != null}">
    <div class="row">
        <div class="span12">
            <h4 class="title info"><span class="text" style="padding-left: 10px">${info}</span></h4>
        </div>
    </div>
    </c:if>
    <c:if test="${error != null}">
    <div class="row">
        <div class="span12">
            <h4 class="title error"><span class="text" style="padding-left: 10px;">${error}</span></h4>
        </div>
    </div>
    </c:if>
</section>