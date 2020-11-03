<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<form id="personal" method="POST" action="">
    <div class="span6">
        <input type="hidden" name="action" value="personal" />

        <h4 class="title"><span class="text"><strong>Your</strong> Personal Details</span></h4>
        <div class="control-group">
            <label class="control-label"><span class="required">*</span> First Name</label>
            <div class="controls">
                <input name="firstname" type="text" placeholder="" class="input-xlarge" value="${user.firstName}" required>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label"><span class="required">*</span> Last Name</label>
            <div class="controls">
                <input name="lastname" type="text" placeholder="" class="input-xlarge" value="${user.lastName}" required>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label"><span class="required">*</span> Email Address</label>
            <div class="controls">
                <input name="email"  type="email" placeholder="" class="input-xlarge" value="${user.email}" required>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">Telephone</label>
            <div class="controls">
                <input name="tel"  type="tel" placeholder="" class="input-xlarge" value="${user.telephone}">
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">Fax</label>
            <div class="controls">
                <input type="tel" placeholder="" class="input-xlarge">
            </div>
        </div>
    </div>
    <div class="span6">
        <h4 class="title"><span class="text"><strong>Your</strong> Address</span></h4>
        <div class="control-group">
            <label class="control-label"><span class="required">*</span> Address 1:</label>
            <div class="controls">
                <input name="address1" type="text" placeholder="" class="input-xlarge" value="${personal.address1}" required>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label">Address 2:</label>
            <div class="controls">
                <input name="address2" type="text" placeholder="" class="input-xlarge" value="${personal.address2}">
            </div>
        </div>
        <div class="control-group">
            <label class="control-label"><span class="required">*</span> Country:</label>
            <div class="controls">
                <input name="country" type="text" placeholder="" class="input-xlarge" value="${personal.country}" required>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label"><span class="required">*</span> City:</label>
            <div class="controls">
                <input name="city" type="text" placeholder="" class="input-xlarge" value="${personal.city}" required>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label"><span class="required">*</span> Post Code:</label>
            <div class="controls">
                <input name="postcode" type="text" placeholder="" class="input-xlarge" value="${personal.postCode}" required>
            </div>
        </div>

        <button type="submit" class="btn btn-inverse">Continue</button>
    </div>
</form>