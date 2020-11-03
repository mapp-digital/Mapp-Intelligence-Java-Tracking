<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="accordion-group">
    <div class="accordion-heading">
        <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#collapseOne">Checkout Options</a>
    </div>

    <c:if test="${user == null}">
        <div id="collapseOne" class="accordion-body in collapse">
    </c:if>
    <c:if test="${user != null}">
        <div id="collapseOne" class="accordion-body collapse">
    </c:if>

        <div class="accordion-inner">
            <div class="row-fluid">
                <div class="span6">
                    <jsp:include page="../forms/new-customer.jsp" />
                </div>
                <div class="span6">
                    <jsp:include page="../forms/returning-customer.jsp" />
                </div>
            </div>
        </div>
    </div>
 </div>