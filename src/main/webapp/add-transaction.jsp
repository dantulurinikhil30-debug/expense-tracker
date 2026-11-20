<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_IN"/>

<jsp:include page="includes/header.jsp">
    <jsp:param name="pageTitle" value="Add Transaction - Expense Tracker"/>
</jsp:include>
<jsp:include page="includes/navbar.jsp"/>

<main class="py-4">
    <div class="container" style="max-width: 680px;">

        <!-- Header / Back Breadcrumb -->
        <div class="d-flex align-items-center justify-content-between mb-4">
            <div>
                <a href="${pageContext.request.contextPath}/dashboard" class="text-decoration-none text-muted small">
                    <i class="bi bi-arrow-left me-1"></i> Back to Dashboard
                </a>
                <h3 class="fw-bold mb-0 mt-1">Record Transaction</h3>
            </div>
            <a href="${pageContext.request.contextPath}/manage-categories" class="btn btn-outline-secondary btn-sm">
                <i class="bi bi-gear me-1"></i> Manage Categories
            </a>
        </div>

        <!-- Available Balance Pills -->
        <c:if test="${not empty user}">
            <div class="d-flex flex-wrap gap-2 mb-3">
                <div class="px-3 py-2 rounded-3 bg-white border shadow-sm d-flex align-items-center gap-2">
                    <i class="bi bi-bank text-primary"></i>
                    <small class="text-muted">Available Bank:</small>
                    <span class="fw-bold text-primary">
                        ₹<fmt:formatNumber value="${user.bankBalance}" type="number" minFractionDigits="2" maxFractionDigits="2"/>
                    </span>
                </div>
                <div class="px-3 py-2 rounded-3 bg-white border shadow-sm d-flex align-items-center gap-2">
                    <i class="bi bi-cash-coin text-success"></i>
                    <small class="text-muted">Available Cash:</small>
                    <span class="fw-bold text-success">
                        ₹<fmt:formatNumber value="${user.cashBalance}" type="number" minFractionDigits="2" maxFractionDigits="2"/>
                    </span>
                </div>
            </div>
        </c:if>

        <!-- Error Alert -->
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center shadow-sm" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2 fs-5"></i>
                <div>${errorMessage}</div>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <div class="card content-card shadow-sm border-0">
            <div class="card-body p-4 p-md-5">
                <form action="${pageContext.request.contextPath}/add-transaction" method="post" id="transactionForm">

                    <!-- 1. Transaction Type Toggle -->
                    <div class="mb-4">
                        <label class="form-label fw-semibold text-muted small text-uppercase">Transaction Type</label>
                        <div class="row g-2">
                            <div class="col-6">
                                <input type="radio" class="btn-check" name="type" id="typeExpense" value="EXPENSE"
                                       ${empty selectedType || selectedType == 'EXPENSE' ? 'checked' : ''} onchange="filterCategories()">
                                <label class="btn btn-outline-danger w-100 py-2 fw-semibold d-flex align-items-center justify-content-center gap-2" for="typeExpense">
                                    <i class="bi bi-arrow-up-right-circle"></i> Expense
                                </label>
                            </div>
                            <div class="col-6">
                                <input type="radio" class="btn-check" name="type" id="typeIncome" value="INCOME"
                                       ${selectedType == 'INCOME' ? 'checked' : ''} onchange="filterCategories()">
                                <label class="btn btn-outline-success w-100 py-2 fw-semibold d-flex align-items-center justify-content-center gap-2" for="typeIncome">
                                    <i class="bi bi-arrow-down-left-circle"></i> Income
                                </label>
                            </div>
                        </div>
                    </div>

                    <!-- 2. Amount and Date -->
                    <div class="row g-3 mb-3">
                        <div class="col-md-6">
                            <label for="amount" class="form-label fw-semibold">Amount <span class="text-danger">*</span></label>
                            <div class="input-group">
                                <span class="input-group-text bg-light fw-bold text-muted">₹</span>
                                <input type="number" step="0.01" min="0.01" class="form-control" id="amount" name="amount"
                                       placeholder="0.00" value="${enteredAmount}" required autofocus>
                            </div>
                        </div>

                        <div class="col-md-6">
                            <label for="transactionDate" class="form-label fw-semibold">Date <span class="text-danger">*</span></label>
                            <div class="input-group">
                                <span class="input-group-text bg-light text-muted"><i class="bi bi-calendar3"></i></span>
                                <input type="date" class="form-control" id="transactionDate" name="transactionDate"
                                       value="${empty selectedDate ? defaultDate : selectedDate}" required>
                            </div>
                        </div>
                    </div>

                    <!-- 3. Category Dropdown -->
                    <div class="mb-3">
                        <label for="categoryId" class="form-label fw-semibold">Category <span class="text-danger">*</span></label>
                        <select class="form-select" id="categoryId" name="categoryId" required>
                            <option value="">-- Select Category --</option>
                            <c:forEach var="cat" items="${categories}">
                                <option value="${cat.id}" data-type="${cat.type}"
                                        ${selectedCategoryId == cat.id.toString() ? 'selected' : ''}>
                                    ${cat.name} (${cat.type})
                                </option>
                            </c:forEach>
                        </select>
                        <div class="form-text">
                            Need another category? <a href="${pageContext.request.contextPath}/manage-categories">Add it here</a>.
                        </div>
                    </div>

                    <!-- 4. Payment Mode Dropdown -->
                    <div class="mb-3">
                        <label for="paymentModeId" class="form-label fw-semibold">Payment Mode <span class="text-danger">*</span></label>
                        <select class="form-select" id="paymentModeId" name="paymentModeId" required>
                            <option value="">-- Select Payment Mode --</option>
                            <c:forEach var="mode" items="${paymentModes}">
                                <option value="${mode.id}"
                                        ${selectedPaymentModeId == mode.id.toString() ? 'selected' : ''}>
                                    ${mode.modeName} [Affects ${mode.accountType} Balance]
                                </option>
                            </c:forEach>
                        </select>
                        <div class="form-text">
                            Expenses will verify that your selected account pool has sufficient funds before saving.
                        </div>
                    </div>

                    <!-- 5. Description / Note -->
                    <div class="mb-4">
                        <label for="description" class="form-label fw-semibold">Description / Note <small class="text-muted fw-normal">(Optional)</small></label>
                        <textarea class="form-control" id="description" name="description" rows="2"
                                  placeholder="Add details, merchant name, or notes...">${enteredDescription}</textarea>
                    </div>

                    <!-- Action Buttons -->
                    <div class="d-flex gap-2">
                        <button type="submit" class="btn btn-primary flex-grow-1 py-2 fw-semibold">
                            <i class="bi bi-check2-circle me-1"></i> Save Transaction
                        </button>
                        <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-outline-secondary px-4 py-2">
                            Cancel
                        </a>
                    </div>
                </form>
            </div>
        </div>

    </div>
</main>

<jsp:include page="includes/footer.jsp"/>

<script>
    // Filter categories dynamically based on selected transaction type
    function filterCategories() {
        const isExpense = document.getElementById('typeExpense').checked;
        const currentType = isExpense ? 'EXPENSE' : 'INCOME';
        const select = document.getElementById('categoryId');
        const options = select.querySelectorAll('option');

        let hasSelectedOption = false;

        options.forEach(opt => {
            if (!opt.value) return; // Keep placeholder option
            const optType = opt.getAttribute('data-type');
            if (optType === currentType) {
                opt.style.display = '';
                opt.disabled = false;
                if (opt.selected) {
                    hasSelectedOption = true;
                }
            } else {
                opt.style.display = 'none';
                opt.disabled = true;
                if (opt.selected) {
                    opt.selected = false;
                }
            }
        });

        // If no option is selected, re-select placeholder
        if (!hasSelectedOption) {
            select.value = "";
        }
    }

    document.addEventListener('DOMContentLoaded', function() {
        filterCategories();
    });
</script>
