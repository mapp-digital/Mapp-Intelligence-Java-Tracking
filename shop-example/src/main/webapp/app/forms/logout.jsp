<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<form id="logout" method="POST" action="" style="margin: 0;">
    <input type="hidden" name="action" value="logout" />
    <a href="javascript:void(0);" onclick="document.getElementById('logout').submit(); return false;" style="display: block;">Logout</a>
</form>
