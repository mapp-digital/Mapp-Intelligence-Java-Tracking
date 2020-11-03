<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<h4 class="title"><span class="text"><strong>Register</strong> Form</span></h4>
<form class="form-stacked" method="POST" action="">
    <fieldset>
        <input type="hidden" name="action" value="register" />

        <div class="control-group">
            <label class="control-label">First Name</label>
            <div class="controls">
                <input name="firstname" type="text" placeholder="Enter your firstname" class="input-xlarge" required>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">Last Name</label>
            <div class="controls">
                <input name="lastname" type="text" placeholder="Enter your lastname" class="input-xlarge" required>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">Telephone</label>
            <div class="controls">
                <input name="tel" type="tel" placeholder="Enter your telephone" class="input-xlarge" required>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">Email address</label>
            <div class="controls">
                <input name="email" type="email" placeholder="Enter your email" class="input-xlarge" required>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">Password</label>
            <div class="controls">
                <input name="password" type="password" placeholder="Enter your password" class="input-xlarge" required>
            </div>
        </div>
        <hr>
        <div class="actions"><input tabindex="9" class="btn btn-inverse large" type="submit" value="Create your account"></div>
    </fieldset>
</form>
