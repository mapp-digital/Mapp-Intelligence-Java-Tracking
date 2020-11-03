<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="row">
    <div class="span12">
        <div class="accordion" id="accordion2">
            <jsp:include page="../checkout/step1.jsp" />
            <jsp:include page="../checkout/step2.jsp" />
            <jsp:include page="../checkout/step3.jsp" />
        </div>
    </div>
</div>
