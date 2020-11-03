<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="accordion-group">
    <div class="accordion-heading">
        <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#collapseTwo">Account &amp; Billing Details</a>
    </div>

    <c:if test="${user == null || (user != null && personal != null)}">
        <div id="collapseTwo" class="accordion-body collapse">
    </c:if>
    <c:if test="${user != null && personal == null}">
        <div id="collapseTwo" class="accordion-body in collapse">
    </c:if>

        <div class="accordion-inner">
            <div class="row-fluid">
                <jsp:include page="../forms/personal.jsp" />
            </div>
        </div>
    </div>
</div>