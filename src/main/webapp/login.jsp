<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="includes/header.jsp">
    <jsp:param name="pageTitle" value="Login - Expense Tracker"/>
</jsp:include>

<main class="d-flex align-items-center justify-content-center py-5">
    <div class="container">
        <div class="auth-card">
            <div class="auth-header">
                <i class="bi bi-wallet2 fs-1 text-warning mb-2 d-block"></i>
                <h3 class="fw-bold mb-1">ExpenseTracker</h3>
                <p class="text-white-50 mb-0 small">Sign in to manage your income &amp; expenses</p>
            </div>

            <div class="p-4 p-sm-5">
                <!-- Status & Error Alerts -->
                <c:if test="${not empty successMessage}">
                    <div class="alert alert-success alert-dismissible fade show d-flex align-items-center" role="alert">
                        <i class="bi bi-check-circle-fill me-2 fs-5"></i>
                        <div>${successMessage}</div>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>

                <c:if test="${not empty errorMessage}">
                    <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center" role="alert">
                        <i class="bi bi-exclamation-triangle-fill me-2 fs-5"></i>
                        <div>${errorMessage}</div>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>

                <c:if test="${not empty infoMessage}">
                    <div class="alert alert-info alert-dismissible fade show d-flex align-items-center" role="alert">
                        <i class="bi bi-info-circle-fill me-2 fs-5"></i>
                        <div>${infoMessage}</div>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>

                <form action="${pageContext.request.contextPath}/login" method="post" autocomplete="off">
                    <div class="mb-3">
                        <label for="email" class="form-label fw-semibold">Email Address</label>
                        <div class="input-group">
                            <span class="input-group-text bg-light text-muted"><i class="bi bi-envelope"></i></span>
                            <input type="email" class="form-control" id="email" name="email"
                                   placeholder="name@example.com" value="${email}" required autofocus>
                        </div>
                    </div>

                    <div class="mb-4">
                        <label for="password" class="form-label fw-semibold">Password</label>
                        <div class="input-group">
                            <span class="input-group-text bg-light text-muted"><i class="bi bi-lock"></i></span>
                            <input type="password" class="form-control" id="password" name="password"
                                   placeholder="Enter your password" required>
                        </div>
                    </div>

                    <button type="submit" class="btn btn-primary w-100 py-2 fw-semibold shadow-sm mb-3">
                        <i class="bi bi-box-arrow-in-right me-1"></i> Sign In
                    </button>
                </form>

                <div class="text-center pt-3 border-top">
                    <p class="text-muted small mb-0">
                        Don't have an account? 
                        <a href="${pageContext.request.contextPath}/register" class="fw-semibold text-decoration-none">
                            Create Account
                        </a>
                    </p>
                </div>
            </div>
        </div>
    </div>
</main>

<jsp:include page="includes/footer.jsp"/>
