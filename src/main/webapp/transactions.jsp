<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_IN"/>

<jsp:include page="includes/header.jsp">
    <jsp:param name="pageTitle" value="Transaction History &amp; Filter - Expense Tracker"/>
</jsp:include>
<jsp:include page="includes/navbar.jsp"/>

<main class="py-4">
    <div class="container-fluid px-lg-4">

        <!-- Page Header & Action -->
        <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
            <div>
                <a href="${pageContext.request.contextPath}/dashboard" class="text-decoration-none text-muted small">
                    <i class="bi bi-arrow-left me-1"></i> Back to Dashboard
                </a>
                <h3 class="fw-bold mb-0 mt-1">Transaction History &amp; Filter</h3>
                <p class="text-muted small mb-0">Search, filter, and analyze your financial transactions by date, category, and type.</p>
            </div>
            <div class="d-flex gap-2">
                <a href="${pageContext.request.contextPath}/add-transaction" class="btn btn-primary px-3 shadow-sm">
                    <i class="bi bi-plus-circle me-1"></i> Add Transaction
                </a>
            </div>
        </div>

        <!-- Feedback Alerts -->
        <c:if test="${not empty successMessage}">
            <div class="alert alert-success alert-dismissible fade show d-flex align-items-center shadow-sm" role="alert">
                <i class="bi bi-check-circle-fill me-2 fs-5"></i>
                <div>${successMessage}</div>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center shadow-sm" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2 fs-5"></i>
                <div>${errorMessage}</div>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <!-- ======================================================== -->
        <!-- 1. FILTER BAR CARD                                      -->
        <!-- ======================================================== -->
        <div class="card content-card mb-4 shadow-sm border-0">
            <div class="card-header bg-white py-3 d-flex align-items-center gap-2">
                <i class="bi bi-funnel-fill text-primary"></i>
                <span class="fw-bold">Filter Transactions</span>
            </div>
            <div class="card-body p-4">
                <form action="${pageContext.request.contextPath}/transactions" method="get">
                    <div class="row g-3 align-items-end">
                        <!-- From Date -->
                        <div class="col-12 col-sm-6 col-lg-3">
                            <label for="startDate" class="form-label fw-semibold small text-muted">From Date</label>
                            <input type="date" class="form-control form-control-sm" id="startDate" name="startDate"
                                   value="${selectedStartDate}">
                        </div>

                        <!-- To Date -->
                        <div class="col-12 col-sm-6 col-lg-3">
                            <label for="endDate" class="form-label fw-semibold small text-muted">To Date</label>
                            <input type="date" class="form-control form-control-sm" id="endDate" name="endDate"
                                   value="${selectedEndDate}">
                        </div>

                        <!-- Category -->
                        <div class="col-12 col-sm-6 col-lg-3">
                            <label for="categoryId" class="form-label fw-semibold small text-muted">Category</label>
                            <select class="form-select form-select-sm" id="categoryId" name="categoryId">
                                <option value="all" ${selectedCategoryId == 'all' ? 'selected' : ''}>All Categories</option>
                                <c:forEach var="cat" items="${categories}">
                                    <option value="${cat.id}" ${selectedCategoryId == cat.id.toString() ? 'selected' : ''}>
                                        ${cat.name} (${cat.type})
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <!-- Type -->
                        <div class="col-12 col-sm-6 col-lg-3">
                            <label for="type" class="form-label fw-semibold small text-muted">Transaction Type</label>
                            <select class="form-select form-select-sm" id="type" name="type">
                                <option value="ALL" ${selectedType == 'ALL' ? 'selected' : ''}>All Types</option>
                                <option value="EXPENSE" ${selectedType == 'EXPENSE' ? 'selected' : ''}>Expense Only</option>
                                <option value="INCOME" ${selectedType == 'INCOME' ? 'selected' : ''}>Income Only</option>
                            </select>
                        </div>

                        <!-- Action Buttons -->
                        <div class="col-12 d-flex gap-2 justify-content-end mt-3 pt-2 border-top">
                            <a href="${pageContext.request.contextPath}/transactions" class="btn btn-outline-secondary btn-sm px-3">
                                <i class="bi bi-arrow-counterclockwise me-1"></i> Reset Filters
                            </a>
                            <button type="submit" class="btn btn-primary btn-sm px-4 fw-semibold">
                                <i class="bi bi-funnel me-1"></i> Apply Filter
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>

        <!-- ======================================================== -->
        <!-- 2. FILTERED SUMMARY STRIP                               -->
        <!-- ======================================================== -->
        <div class="row g-3 mb-4">
            <!-- Filtered Income -->
            <div class="col-12 col-md-4">
                <div class="card p-3 rounded-3 border bg-white shadow-sm h-100">
                    <div class="d-flex align-items-center justify-content-between mb-1">
                        <span class="text-success small fw-bold text-uppercase">
                            <i class="bi bi-arrow-down-left-circle me-1"></i> Filtered Income
                        </span>
                        <span class="badge bg-success-subtle text-success border border-success-subtle">INCOME</span>
                    </div>
                    <h3 class="fw-bold text-success mb-0">
                        +₹<fmt:formatNumber value="${filteredTotalIncome}" type="number" minFractionDigits="2" maxFractionDigits="2"/>
                    </h3>
                    <small class="text-muted mt-1">Total income in this selection</small>
                </div>
            </div>

            <!-- Filtered Expense -->
            <div class="col-12 col-md-4">
                <div class="card p-3 rounded-3 border bg-white shadow-sm h-100">
                    <div class="d-flex align-items-center justify-content-between mb-1">
                        <span class="text-danger small fw-bold text-uppercase">
                            <i class="bi bi-arrow-up-right-circle me-1"></i> Filtered Expense
                        </span>
                        <span class="badge bg-danger-subtle text-danger border border-danger-subtle">EXPENSE</span>
                    </div>
                    <h3 class="fw-bold text-danger mb-0">
                        -₹<fmt:formatNumber value="${filteredTotalExpense}" type="number" minFractionDigits="2" maxFractionDigits="2"/>
                    </h3>
                    <small class="text-muted mt-1">Total expenses in this selection</small>
                </div>
            </div>

            <!-- Net Cashflow -->
            <div class="col-12 col-md-4">
                <div class="card p-3 rounded-3 border bg-white shadow-sm h-100">
                    <div class="d-flex align-items-center justify-content-between mb-1">
                        <span class="text-primary small fw-bold text-uppercase">
                            <i class="bi bi-cash-stack me-1"></i> Net Cashflow
                        </span>
                        <span class="badge bg-light text-dark border">${matchCount} records</span>
                    </div>
                    <h3 class="fw-bold mb-0 ${filteredNetAmount >= 0 ? 'text-primary' : 'text-danger'}">
                        <c:choose>
                            <c:when test="${filteredNetAmount >= 0}">
                                +₹<fmt:formatNumber value="${filteredNetAmount}" type="number" minFractionDigits="2" maxFractionDigits="2"/>
                            </c:when>
                            <c:otherwise>
                                -₹<fmt:formatNumber value="${filteredNetAmount.abs()}" type="number" minFractionDigits="2" maxFractionDigits="2"/>
                            </c:otherwise>
                        </c:choose>
                    </h3>
                    <small class="text-muted mt-1">Income minus Expense</small>
                </div>
            </div>
        </div>

        <!-- ======================================================== -->
        <!-- 3. TRANSACTIONS TABLE                                   -->
        <!-- ======================================================== -->
        <div class="card content-card shadow-sm border-0">
            <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center">
                <div class="d-flex align-items-center gap-2">
                    <i class="bi bi-card-checklist text-primary fs-5"></i>
                    <span class="fw-bold">Filtered Transactions (${matchCount})</span>
                </div>
                <c:if test="${not empty selectedStartDate || not empty selectedEndDate || selectedCategoryId != 'all' || selectedType != 'ALL'}">
                    <span class="badge bg-primary-subtle text-primary border border-primary-subtle px-3 py-1 rounded-pill small">
                        Active Filter Applied
                    </span>
                </c:if>
            </div>

            <div class="card-body p-0">
                <c:choose>
                    <c:when test="${not empty transactions}">
                        <div class="table-responsive">
                            <table class="table table-hover table-transactions mb-0">
                                <thead class="table-light">
                                    <tr>
                                        <th class="ps-4">Date</th>
                                        <th>Category</th>
                                        <th>Description</th>
                                        <th>Payment Mode</th>
                                        <th>Type</th>
                                        <th class="text-end">Amount</th>
                                        <th class="text-center pe-4">Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="tx" items="${transactions}">
                                        <tr>
                                            <td class="ps-4 fw-medium text-muted">
                                                ${tx.transactionDate}
                                            </td>
                                            <td>
                                                <span class="fw-semibold text-dark">${tx.category.name}</span>
                                            </td>
                                            <td class="text-muted small">
                                                <c:out value="${empty tx.description ? '—' : tx.description}"/>
                                            </td>
                                            <td>
                                                <span class="badge ${tx.paymentMode.accountType == 'BANK' ? 'badge-bank' : 'badge-cash'} px-2 py-1 rounded">
                                                    <i class="bi ${tx.paymentMode.accountType == 'BANK' ? 'bi-bank' : 'bi-cash'} me-1"></i>
                                                    ${tx.paymentMode.modeName}
                                                </span>
                                            </td>
                                            <td>
                                                <span class="badge ${tx.type == 'INCOME' ? 'badge-income' : 'badge-expense'} px-2 py-1 rounded">
                                                    <i class="bi ${tx.type == 'INCOME' ? 'bi-arrow-down-left' : 'bi-arrow-up-right'} me-1"></i>
                                                    ${tx.type}
                                                </span>
                                            </td>
                                            <td class="text-end font-monospace fs-6 ${tx.type == 'INCOME' ? 'amount-income' : 'amount-expense'}">
                                                <c:choose>
                                                    <c:when test="${tx.type == 'INCOME'}">
                                                        +₹<fmt:formatNumber value="${tx.amount}" type="number" minFractionDigits="2" maxFractionDigits="2"/>
                                                    </c:when>
                                                    <c:otherwise>
                                                        -₹<fmt:formatNumber value="${tx.amount}" type="number" minFractionDigits="2" maxFractionDigits="2"/>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="text-center pe-4">
                                                <button type="button" class="btn btn-outline-danger btn-sm py-1 px-2"
                                                        data-bs-toggle="modal"
                                                        data-bs-target="#deleteModal${tx.id}"
                                                        title="Delete Transaction">
                                                    <i class="bi bi-trash"></i>
                                                </button>

                                                <!-- Delete Confirmation Modal -->
                                                <div class="modal fade" id="deleteModal${tx.id}" tabindex="-1" aria-hidden="true">
                                                    <div class="modal-dialog modal-dialog-centered">
                                                        <div class="modal-content">
                                                            <div class="modal-header">
                                                                <h5 class="modal-title fw-bold text-danger">
                                                                    <i class="bi bi-exclamation-triangle-fill me-1"></i> Confirm Delete
                                                                </h5>
                                                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                            </div>
                                                            <div class="modal-body text-start">
                                                                <p>Are you sure you want to delete this <strong>${tx.type}</strong> of <strong>₹<fmt:formatNumber value="${tx.amount}" type="number" minFractionDigits="2" maxFractionDigits="2"/></strong> (${tx.category.name})?</p>
                                                                <p class="text-muted small mb-0">
                                                                    <strong>Note:</strong> Deleting this transaction will reverse its effect on your <strong>${tx.paymentMode.accountType}</strong> balance.
                                                                </p>
                                                            </div>
                                                            <div class="modal-footer">
                                                                <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                                                                <form action="${pageContext.request.contextPath}/delete-transaction" method="post" class="d-inline">
                                                                    <input type="hidden" name="transactionId" value="${tx.id}">
                                                                    <input type="hidden" name="redirectUrl" value="transactions">
                                                                    <button type="submit" class="btn btn-danger btn-sm">
                                                                        <i class="bi bi-trash me-1"></i> Yes, Delete &amp; Reverse Balance
                                                                    </button>
                                                                </form>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div class="text-center py-5">
                            <i class="bi bi-search text-muted display-4 mb-3 d-block opacity-50"></i>
                            <h5 class="fw-bold text-dark">No Matching Transactions</h5>
                            <p class="text-muted small max-w-sm mx-auto mb-3">
                                No transactions matched your filter criteria. Try adjusting the date range, category, or type.
                            </p>
                            <a href="${pageContext.request.contextPath}/transactions" class="btn btn-outline-primary btn-sm px-3">
                                <i class="bi bi-arrow-counterclockwise me-1"></i> Clear All Filters
                            </a>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

    </div>
</main>

<jsp:include page="includes/footer.jsp"/>
