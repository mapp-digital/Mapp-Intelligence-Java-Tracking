<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<h4 class="title"><span class="text"><strong>Returning</strong> Customer</span></h4>
<p>I am a returning customer</p>
<form id="returning-customer" method="POST" action="">
    <fieldset>
        <input type="hidden" name="action" value="login" />
        <div class="control-group">
            <label class="control-label">Email address</label>
            <div class="controls">
                <input type="email" placeholder="Enter your email" name="email" class="input-xlarge" required />
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">Password</label>
            <div class="controls">
                <input type="password" placeholder="Enter your password" name="password" class="input-xlarge" required />
            </div>
        </div>
        <button type="submit" class="btn btn-inverse">Continue</button>
    </fieldset>
</form>