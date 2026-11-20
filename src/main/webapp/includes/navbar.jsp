<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_IN"/>

<nav class="navbar navbar-expand-lg navbar-dark navbar-custom sticky-top">
    <div class="container-fluid px-lg-4">
        <a class="navbar-brand navbar-brand-custom" href="${pageContext.request.contextPath}/dashboard">
            <i class="bi bi-wallet2 text-warning fs-4"></i>
            <span>ExpenseTracker</span>
        </a>

        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarContent"
                aria-controls="navbarContent" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="navbarContent">
            <!-- Navigation Links -->
            <ul class="navbar-nav me-auto mb-2 mb-lg-0 ms-lg-3">
                <li class="nav-item">
                    <a class="nav-link ${pageContext.request.requestURI.endsWith('dashboard.jsp') || pageContext.request.requestURI.endsWith('/dashboard') ? 'active' : ''}" 
                       href="${pageContext.request.contextPath}/dashboard">
                        <i class="bi bi-speedometer2 me-1"></i> Dashboard
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${pageContext.request.requestURI.endsWith('add-transaction.jsp') || pageContext.request.requestURI.endsWith('/add-transaction') ? 'active' : ''}" 
                       href="${pageContext.request.contextPath}/add-transaction">
                        <i class="bi bi-plus-circle me-1"></i> Add Transaction
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${pageContext.request.requestURI.endsWith('transactions.jsp') || pageContext.request.requestURI.endsWith('/transactions') ? 'active' : ''}" 
                       href="${pageContext.request.contextPath}/transactions">
                        <i class="bi bi-clock-history me-1"></i> Transactions
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link ${pageContext.request.requestURI.endsWith('manage-categories.jsp') || pageContext.request.requestURI.endsWith('/manage-categories') ? 'active' : ''}" 
                       href="${pageContext.request.contextPath}/manage-categories">
                        <i class="bi bi-tags me-1"></i> Categories &amp; Modes
                    </a>
                </li>
                <c:if test="${sessionScope.currentUser.role == 'ADMIN'}">
                    <li class="nav-item">
                        <a class="nav-link ${pageContext.request.requestURI.endsWith('admin.jsp') || pageContext.request.requestURI.endsWith('/admin') ? 'active' : ''}" 
                           href="${pageContext.request.contextPath}/admin">
                            <i class="bi bi-shield-lock me-1"></i> Admin Panel
                        </a>
                    </li>
                </c:if>
            </ul>

            <!-- User Session Details & Logout -->
            <c:if test="${not empty sessionScope.currentUser}">
                <div class="d-flex align-items-center gap-3">
                    <div class="d-none d-xl-flex align-items-center text-light px-3 py-1 rounded bg-white bg-opacity-10">
                        <small class="me-2 text-white-50">Total Balance:</small>
                        <span class="fw-bold text-warning">
                            ₹<fmt:formatNumber value="${sessionScope.currentUser.totalBalance}" type="number" minFractionDigits="2" maxFractionDigits="2"/>
                        </span>
                    </div>

                    <div class="d-flex align-items-center text-white">
                        <i class="bi bi-person-circle fs-4 me-2 text-info"></i>
                        <span class="fw-semibold me-3">${sessionScope.currentUser.fullName}</span>
                    </div>

                    <a href="${pageContext.request.contextPath}/logout" class="btn btn-outline-light btn-sm px-3 rounded-pill">
                        <i class="bi bi-box-arrow-right me-1"></i> Logout
                    </a>
                </div>
            </c:if>
        </div>
    </div>
</nav>
