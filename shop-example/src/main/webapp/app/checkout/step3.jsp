<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<div class="accordion-group">
    <div class="accordion-heading">
        <a class="accordion-toggle" data-toggle="collapse" data-parent="#accordion2" href="#collapseThree">Confirm Order</a>
    </div>

    <c:if test="${personal == null}">
        <div id="collapseThree" class="accordion-body collapse">
    </c:if>
    <c:if test="${personal != null}">
        <div id="collapseThree" class="accordion-body in collapse">
    </c:if>

        <div class="accordion-inner">
            <div class="row-fluid">
                <div class="control-group">
                    <h4 class="title"><span class="text"><strong>Order</strong> Review</span></h4>
                    <div class="row" style="margin-left: 0;">
                        <div class="span9">
                            <h4 class="title"><span class="text"><strong>Basket</strong> Review</span></h4>
                            <jsp:include page="basket.jsp" />
                        </div>
                        <div class="span3">
                            <h4 class="title"><span class="text"><strong>Personal</strong> Review</span></h4>
                            <ul class="nav">
                                <li>First Name: ${user.firstName}</li>
                                <li>Last Name: ${user.lastName}</li>
                                <li>Email Address: ${user.email}</li>
                                <li>Telephone: ${user.telephone}</li>
                                <li>Address 1: ${personal.address1}</li>
                                <li>Address 2: ${personal.address2}</li>
                                <li>Country: ${personal.country}</li>
                                <li>City: ${personal.city}</li>
                                <li>Post Code: ${personal.postCode}</li>
                            </ul>
                        </div>
                    </div>
                </div>
                <hr/>
                <button id="confirmation" class="btn btn-inverse pull-right">Place Order</button>
            </div>
        </div>
    </div>
</div>
