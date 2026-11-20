<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<jsp:include page="includes/header.jsp">
    <jsp:param name="pageTitle" value="Register - Expense Tracker"/>
</jsp:include>

<main class="d-flex align-items-center justify-content-center py-5">
    <div class="container">
        <div class="auth-card" style="max-width: 540px;">
            <div class="auth-header">
                <i class="bi bi-person-plus-fill fs-1 text-warning mb-2 d-block"></i>
                <h3 class="fw-bold mb-1">Create Account</h3>
                <p class="text-white-50 mb-0 small">Set up your profile and starting balances</p>
            </div>

            <div class="p-4 p-sm-5">
                <!-- Error Alert -->
                <c:if test="${not empty errorMessage}">
                    <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center" role="alert">
                        <i class="bi bi-exclamation-triangle-fill me-2 fs-5"></i>
                        <div>${errorMessage}</div>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
                </c:if>

                <form action="${pageContext.request.contextPath}/register" method="post" autocomplete="off">
                    <div class="mb-3">
                        <label for="fullName" class="form-label fw-semibold">Full Name</label>
                        <div class="input-group">
                            <span class="input-group-text bg-light text-muted"><i class="bi bi-person"></i></span>
                            <input type="text" class="form-control" id="fullName" name="fullName"
                                   placeholder="John Doe" value="${fullName}" required autofocus>
                        </div>
                    </div>

                    <div class="mb-3">
                        <label for="email" class="form-label fw-semibold">Email Address</label>
                        <div class="input-group">
                            <span class="input-group-text bg-light text-muted"><i class="bi bi-envelope"></i></span>
                            <input type="email" class="form-control" id="email" name="email"
                                   placeholder="name@example.com" value="${email}" required>
                        </div>
                    </div>

                    <div class="row g-2 mb-3">
                        <div class="col-md-6">
                            <label for="password" class="form-label fw-semibold">Password</label>
                            <div class="input-group">
                                <span class="input-group-text bg-light text-muted"><i class="bi bi-lock"></i></span>
                                <input type="password" class="form-control" id="password" name="password"
                                       placeholder="At least 6 chars" minlength="6" required>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <label for="confirmPassword" class="form-label fw-semibold">Confirm Password</label>
                            <div class="input-group">
                                <span class="input-group-text bg-light text-muted"><i class="bi bi-shield-check"></i></span>
                                <input type="password" class="form-control" id="confirmPassword" name="confirmPassword"
                                       placeholder="Repeat password" minlength="6" required>
                            </div>
                        </div>
                    </div>

                    <div class="card bg-light border-0 p-3 mb-4 rounded-3">
                        <h6 class="fw-bold mb-2 text-primary">
                            <i class="bi bi-cash-stack me-1"></i> Starting Account Balances
                        </h6>
                        <p class="text-muted small mb-3">
                            Set your current funds. Negative balances are strictly disallowed.
                        </p>

                        <div class="row g-3">
                            <div class="col-sm-6">
                                <label for="initialBankBalance" class="form-label fw-semibold small">Initial Bank Balance</label>
                                <div class="input-group">
                                    <span class="input-group-text">₹</span>
                                    <input type="number" step="0.01" min="0" class="form-control"
                                           id="initialBankBalance" name="initialBankBalance"
                                           value="${empty initialBankBalance ? '0.00' : initialBankBalance}" required>
                                </div>
                            </div>
                            <div class="col-sm-6">
                                <label for="initialCashBalance" class="form-label fw-semibold small">Initial Cash Balance</label>
                                <div class="input-group">
                                    <span class="input-group-text">₹</span>
                                    <input type="number" step="0.01" min="0" class="form-control"
                                           id="initialCashBalance" name="initialCashBalance"
                                           value="${empty initialCashBalance ? '0.00' : initialCashBalance}" required>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="alert alert-secondary small d-flex align-items-center py-2 mb-4" role="alert">
                        <i class="bi bi-magic me-2 fs-5 text-primary"></i>
                        <div>
                            Default categories (Food, Rent, Salary, etc.) and payment modes (Cash, UPI, Cards) will be automatically created for you.
                        </div>
                    </div>

                    <button type="submit" class="btn btn-primary w-100 py-2 fw-semibold shadow-sm mb-3">
                        <i class="bi bi-person-check-fill me-1"></i> Complete Registration
                    </button>
                </form>

                <div class="text-center pt-3 border-top">
                    <p class="text-muted small mb-0">
                        Already have an account? 
                        <a href="${pageContext.request.contextPath}/login" class="fw-semibold text-decoration-none">
                            Sign In
                        </a>
                    </p>
                </div>
            </div>
        </div>
    </div>
</main>

<jsp:include page="includes/footer.jsp"/>
