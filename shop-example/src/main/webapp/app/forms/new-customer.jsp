<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<h4 class="title"><span class="text"><strong>New</strong> Customer</span></h4>
<p>By creating an account you will be able to shop faster, be up to date on an order's status, and keep track of the orders you have previously made.</p>
<form id="new-customer" action="#" onsubmit="return false;">
    <fieldset>
        <label class="radio" for="register">
            <input type="radio" name="account" value="register" id="register" checked="checked">Register Account
        </label>
        <label class="radio" for="guest">
            <input type="radio" name="account" value="guest" id="guest">Guest Checkout
        </label>
        <br>
        <button type="submit" class="btn btn-inverse">Continue</button>
    </fieldset>
</form>