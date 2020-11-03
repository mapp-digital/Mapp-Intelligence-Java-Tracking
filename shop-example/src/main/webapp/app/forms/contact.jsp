<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<form id="contact" method="POST" action="">
    <fieldset>
        <input type="hidden" name="action" value="contact" />

        <div class="clearfix">
            <label for="name"><span>Name:</span></label>
            <div class="input">
                <input tabindex="1" size="18" id="name" name="name" type="text" class="input-xlarge" value="${user.fullName}" placeholder="Name" required>
            </div>
        </div>

        <div class="clearfix">
            <label for="email"><span>Email:</span></label>
            <div class="input">
                <input tabindex="2" size="25" id="email" name="email" type="text" class="input-xlarge" value="${user.email}" placeholder="Email Address" required>
            </div>
        </div>

        <div class="clearfix">
            <label for="message"><span>Message:</span></label>
            <div class="input">
                <textarea tabindex="3" class="input-xlarge" id="message" name="body" rows="7" placeholder="Message" required></textarea>
            </div>
        </div>

        <div class="actions">
            <button tabindex="3" type="submit" class="btn btn-inverse">Send message</button>
        </div>
    </fieldset>
</form>
