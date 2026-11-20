<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_IN"/>

<jsp:include page="includes/header.jsp">
    <jsp:param name="pageTitle" value="Dashboard - Expense Tracker"/>
</jsp:include>
<jsp:include page="includes/navbar.jsp"/>

<main class="py-4">
    <div class="container-fluid px-lg-4">

        <!-- Welcome Banner & Quick Action Header -->
        <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
            <div>
                <h3 class="fw-bold mb-1">Welcome back, ${user.fullName}!</h3>
                <p class="text-muted mb-0 small">Here is an overview of your current balances and spending distribution.</p>
            </div>
            <div class="d-flex gap-2">
                <a href="${pageContext.request.contextPath}/add-transaction" class="btn btn-primary px-3 shadow-sm">
                    <i class="bi bi-plus-circle me-1"></i> Add Transaction
                </a>
                <a href="${pageContext.request.contextPath}/manage-categories" class="btn btn-outline-secondary px-3">
                    <i class="bi bi-gear me-1"></i> Manage Categories
                </a>
            </div>
        </div>

        <!-- Success & Error Alert Messages -->
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
        <!-- 1. THREE PROMINENT SUMMARY CARDS                        -->
        <!-- ======================================================== -->
        <div class="row g-3 mb-4">
            <!-- 1. Bank Account Balance -->
            <div class="col-12 col-md-4">
                <div class="card balance-card card-bank h-100">
                    <div class="card-body">
                        <div class="balance-title">Bank Account Balance</div>
                        <h2 class="balance-amount">
                            ₹<fmt:formatNumber value="${user.bankBalance}" type="number" minFractionDigits="2" maxFractionDigits="2"/>
                        </h2>
                        <small class="opacity-75">Available in bank accounts &amp; cards</small>
                    </div>
                    <i class="bi bi-bank2 card-icon"></i>
                </div>
            </div>

            <!-- 2. Cash Balance -->
            <div class="col-12 col-md-4">
                <div class="card balance-card card-cash h-100">
                    <div class="card-body">
                        <div class="balance-title">Cash on Hand</div>
                        <h2 class="balance-amount">
                            ₹<fmt:formatNumber value="${user.cashBalance}" type="number" minFractionDigits="2" maxFractionDigits="2"/>
                        </h2>
                        <small class="opacity-75">Available in physical cash wallet</small>
                    </div>
                    <i class="bi bi-cash-coin card-icon"></i>
                </div>
            </div>

            <!-- 3. Total Balance -->
            <div class="col-12 col-md-4">
                <div class="card balance-card card-total h-100">
                    <div class="card-body">
                        <div class="balance-title">Total Net Worth</div>
                        <h2 class="balance-amount">
                            ₹<fmt:formatNumber value="${user.totalBalance}" type="number" minFractionDigits="2" maxFractionDigits="2"/>
                        </h2>
                        <small class="opacity-75">Bank Balance + Cash Balance</small>
                    </div>
                    <i class="bi bi-wallet2 card-icon"></i>
                </div>
            </div>
        </div>

        <!-- ======================================================== -->
        <!-- 2. SPENDING ANALYTICS (PIE/DOUGHNUT CHART + HIGHLIGHTS)  -->
        <!-- ======================================================== -->
        <div class="card content-card mb-4">
            <div class="card-header d-flex justify-content-between align-items-center">
                <div class="d-flex align-items-center gap-2">
                    <i class="bi bi-pie-chart-fill text-primary fs-5"></i>
                    <span class="fs-6 fw-bold">Spending Analytics by Category</span>
                </div>
                <c:if test="${totalExpenses > 0}">
                    <span class="badge bg-danger-subtle text-danger border border-danger-subtle px-3 py-2 rounded-pill">
                        Total Spent: ₹<fmt:formatNumber value="${totalExpenses}" type="number" minFractionDigits="2" maxFractionDigits="2"/>
                    </span>
                </c:if>
            </div>
            <div class="card-body p-4">
                <c:choose>
                    <c:when test="${not empty categorySummaries}">
                        <div class="row g-4 align-items-center">
                            <!-- Left: Chart.js Visualization -->
                            <div class="col-12 col-lg-5 text-center">
                                <div style="height: 280px; position: relative;">
                                    <canvas id="expensePieChart"></canvas>
                                </div>
                            </div>

                            <!-- Right: Category Highlights & Breakdown Table -->
                            <div class="col-12 col-lg-7">
                                <!-- Highest & Lowest Spend Callouts -->
                                <div class="row g-3 mb-4">
                                    <div class="col-sm-6">
                                        <div class="p-3 highlight-card highlight-highest">
                                            <div class="d-flex justify-content-between align-items-center mb-1">
                                                <span class="text-danger small fw-bold text-uppercase">
                                                    <i class="bi bi-arrow-up-circle-fill me-1"></i> Highest Spend
                                                </span>
                                                <span class="badge bg-danger">${highestSpend.formattedPercentage}</span>
                                            </div>
                                            <h5 class="fw-bold mb-0 text-dark">${highestSpend.categoryName}</h5>
                                            <div class="text-danger fw-semibold mt-1">
                                                ₹<fmt:formatNumber value="${highestSpend.totalAmount}" type="number" minFractionDigits="2" maxFractionDigits="2"/>
                                            </div>
                                        </div>
                                    </div>

                                    <div class="col-sm-6">
                                        <div class="p-3 highlight-card highlight-lowest">
                                            <div class="d-flex justify-content-between align-items-center mb-1">
                                                <span class="text-success small fw-bold text-uppercase">
                                                    <i class="bi bi-arrow-down-circle-fill me-1"></i> Lowest Spend
                                                </span>
                                                <span class="badge bg-success">${lowestSpend.formattedPercentage}</span>
                                            </div>
                                            <h5 class="fw-bold mb-0 text-dark">${lowestSpend.categoryName}</h5>
                                            <div class="text-success fw-semibold mt-1">
                                                ₹<fmt:formatNumber value="${lowestSpend.totalAmount}" type="number" minFractionDigits="2" maxFractionDigits="2"/>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <!-- Breakdown Progress List -->
                                <div class="table-responsive">
                                    <table class="table table-sm table-borderless align-middle mb-0">
                                        <thead>
                                            <tr class="text-muted small border-bottom">
                                                <th>Category</th>
                                                <th class="text-end">Amount</th>
                                                <th style="width: 35%;">Share of Total</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="item" items="${categorySummaries}">
                                                <tr>
                                                    <td class="fw-semibold text-dark">${item.categoryName}</td>
                                                    <td class="text-end fw-bold">
                                                        ₹<fmt:formatNumber value="${item.totalAmount}" type="number" minFractionDigits="2" maxFractionDigits="2"/>
                                                    </td>
                                                    <td>
                                                        <div class="d-flex align-items-center gap-2">
                                                            <div class="progress flex-grow-1" style="height: 7px;">
                                                                <div class="progress-bar bg-primary" role="progressbar"
                                                                     style="width: ${item.percentage}%;"
                                                                     aria-valuenow="${item.percentage}" aria-valuemin="0" aria-valuemax="100">
                                                                </div>
                                                            </div>
                                                            <span class="small text-muted" style="min-width: 42px;">${item.formattedPercentage}</span>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div class="text-center py-5">
                            <i class="bi bi-pie-chart text-muted display-4 mb-3 d-block opacity-50"></i>
                            <h5 class="fw-bold text-dark">No Expense Data Yet</h5>
                            <p class="text-muted small max-w-sm mx-auto mb-3">
                                You haven't recorded any expenses yet. Log your first expense to unlock interactive category breakdowns, percentages, and charts.
                            </p>
                            <a href="${pageContext.request.contextPath}/add-transaction" class="btn btn-primary btn-sm px-3">
                                <i class="bi bi-plus-circle me-1"></i> Log an Expense
                            </a>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <!-- ======================================================== -->
        <!-- 3. RECENT TRANSACTIONS TABLE                            -->
        <!-- ======================================================== -->
        <div class="card content-card">
            <div class="card-header d-flex justify-content-between align-items-center">
                <div class="d-flex align-items-center gap-2">
                    <i class="bi bi-clock-history text-primary fs-5"></i>
                    <span class="fs-6 fw-bold">Recent Transactions</span>
                </div>
                <div class="d-flex align-items-center gap-2">
                    <a href="${pageContext.request.contextPath}/transactions" class="btn btn-outline-secondary btn-sm">
                        <i class="bi bi-funnel me-1"></i> View All &amp; Filter &rarr;
                    </a>
                    <a href="${pageContext.request.contextPath}/add-transaction" class="btn btn-outline-primary btn-sm">
                        <i class="bi bi-plus-lg me-1"></i> New Transaction
                    </a>
                </div>
            </div>

            <div class="card-body p-0">
                <c:choose>
                    <c:when test="${not empty recentTransactions}">
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
                                    <c:forEach var="tx" items="${recentTransactions}">
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
                                                                    <strong>Note:</strong> Deleting this transaction will accurately reverse its effect on your <strong>${tx.paymentMode.accountType}</strong> balance.
                                                                </p>
                                                            </div>
                                                            <div class="modal-footer">
                                                                <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                                                                <form action="${pageContext.request.contextPath}/delete-transaction" method="post" class="d-inline">
                                                                    <input type="hidden" name="transactionId" value="${tx.id}">
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
                            <i class="bi bi-receipt text-muted display-4 mb-3 d-block opacity-50"></i>
                            <h5 class="fw-bold text-dark">No Transactions Found</h5>
                            <p class="text-muted small max-w-sm mx-auto mb-3">
                                You haven't recorded any transactions yet. Start logging your income or expenses to track your financial activity.
                            </p>
                            <a href="${pageContext.request.contextPath}/add-transaction" class="btn btn-primary btn-sm px-3">
                                <i class="bi bi-plus-circle me-1"></i> Add Your First Transaction
                            </a>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

    </div>
</main>

<jsp:include page="includes/footer.jsp"/>

<!-- Initialize Spending Chart if data is present -->
<c:if test="${not empty categorySummaries}">
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const chartLabels = ${chartLabelsJson};
            const chartData = ${chartDataJson};
            initExpenseChart(chartLabels, chartData);
        });
    </script>
</c:if>
