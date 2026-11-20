<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_IN"/>

<jsp:include page="includes/header.jsp">
    <jsp:param name="pageTitle" value="Categories & Payment Modes - Expense Tracker"/>
</jsp:include>
<jsp:include page="includes/navbar.jsp"/>

<main class="py-4">
    <div class="container-fluid px-lg-4">

        <!-- Header -->
        <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
            <div>
                <a href="${pageContext.request.contextPath}/dashboard" class="text-decoration-none text-muted small">
                    <i class="bi bi-arrow-left me-1"></i> Back to Dashboard
                </a>
                <h3 class="fw-bold mb-0 mt-1">Manage Categories &amp; Payment Modes</h3>
                <p class="text-muted small mb-0">Configure your classification tags and payment methods.</p>
            </div>
            <a href="${pageContext.request.contextPath}/add-transaction" class="btn btn-primary px-3 shadow-sm">
                <i class="bi bi-plus-circle me-1"></i> Add Transaction
            </a>
        </div>

        <!-- Alerts -->
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

        <!-- Two Side-by-Side Cards -->
        <div class="row g-4">

            <!-- ======================================================== -->
            <!-- CARD 1: CATEGORIES MANAGEMENT                            -->
            <!-- ======================================================== -->
            <div class="col-12 col-lg-6">
                <div class="card content-card h-100 shadow-sm border-0">
                    <div class="card-header d-flex align-items-center justify-content-between bg-white py-3">
                        <div class="d-flex align-items-center gap-2">
                            <i class="bi bi-tags-fill text-primary fs-5"></i>
                            <span class="fs-6 fw-bold">Categories</span>
                        </div>
                        <span class="badge bg-light text-dark border">${categories.size()} total</span>
                    </div>

                    <div class="card-body p-4">
                        <!-- Add Category Form -->
                        <div class="p-3 bg-light rounded-3 mb-4 border">
                            <h6 class="fw-bold mb-3 text-dark">
                                <i class="bi bi-plus-circle me-1 text-primary"></i> Add New Category
                            </h6>
                            <form action="${pageContext.request.contextPath}/manage-categories" method="post">
                                <input type="hidden" name="action" value="addCategory">
                                <div class="row g-2">
                                    <div class="col-sm-7">
                                        <input type="text" class="form-control" name="categoryName"
                                               placeholder="Category Name (e.g. Gym, Books)" required>
                                    </div>
                                    <div class="col-sm-5">
                                        <select class="form-select" name="categoryType" required>
                                            <option value="EXPENSE">Expense</option>
                                            <option value="INCOME">Income</option>
                                        </select>
                                    </div>
                                    <div class="col-12 mt-2">
                                        <button type="submit" class="btn btn-primary w-100 btn-sm py-2 fw-semibold">
                                            <i class="bi bi-plus-lg me-1"></i> Add Category
                                        </button>
                                    </div>
                                </div>
                            </form>
                        </div>

                        <!-- Existing Categories List -->
                        <h6 class="fw-bold mb-3 text-muted small text-uppercase">Existing Categories</h6>
                        <div class="table-responsive" style="max-height: 400px; overflow-y: auto;">
                            <table class="table table-hover align-middle table-sm">
                                <thead class="table-light sticky-top">
                                    <tr>
                                        <th>Name</th>
                                        <th>Type</th>
                                        <th class="text-end">Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="cat" items="${categories}">
                                        <tr>
                                            <td class="fw-medium">${cat.name}</td>
                                            <td>
                                                <span class="badge ${cat.type == 'EXPENSE' ? 'badge-expense' : 'badge-income'} px-2 py-1">
                                                    ${cat.type}
                                                </span>
                                            </td>
                                            <td class="text-end">
                                                <button type="button" class="btn btn-outline-danger btn-sm py-0 px-2"
                                                        data-bs-toggle="modal"
                                                        data-bs-target="#deleteCatModal${cat.id}"
                                                        title="Delete Category">
                                                    <i class="bi bi-trash"></i>
                                                </button>

                                                <!-- Delete Category Modal -->
                                                <div class="modal fade" id="deleteCatModal${cat.id}" tabindex="-1" aria-hidden="true">
                                                    <div class="modal-dialog modal-dialog-centered">
                                                        <div class="modal-content">
                                                            <div class="modal-header">
                                                                <h5 class="modal-title fw-bold text-danger">Delete Category</h5>
                                                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                            </div>
                                                            <div class="modal-body text-start">
                                                                <p>Are you sure you want to delete category <strong>${cat.name}</strong> (${cat.type})?</p>
                                                                <small class="text-muted">Note: A category can only be deleted if no transactions are linked to it.</small>
                                                            </div>
                                                            <div class="modal-footer">
                                                                <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                                                                <form action="${pageContext.request.contextPath}/manage-categories" method="post" class="d-inline">
                                                                    <input type="hidden" name="action" value="deleteCategory">
                                                                    <input type="hidden" name="categoryId" value="${cat.id}">
                                                                    <button type="submit" class="btn btn-danger btn-sm">Delete</button>
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
                    </div>
                </div>
            </div>

            <!-- ======================================================== -->
            <!-- CARD 2: PAYMENT MODES MANAGEMENT                         -->
            <!-- ======================================================== -->
            <div class="col-12 col-lg-6">
                <div class="card content-card h-100 shadow-sm border-0">
                    <div class="card-header d-flex align-items-center justify-content-between bg-white py-3">
                        <div class="d-flex align-items-center gap-2">
                            <i class="bi bi-credit-card-2-front-fill text-success fs-5"></i>
                            <span class="fs-6 fw-bold">Payment Modes</span>
                        </div>
                        <span class="badge bg-light text-dark border">${paymentModes.size()} total</span>
                    </div>

                    <div class="card-body p-4">
                        <!-- Add Payment Mode Form -->
                        <div class="p-3 bg-light rounded-3 mb-4 border">
                            <h6 class="fw-bold mb-3 text-dark">
                                <i class="bi bi-plus-circle me-1 text-success"></i> Add New Payment Mode
                            </h6>
                            <form action="${pageContext.request.contextPath}/manage-categories" method="post">
                                <input type="hidden" name="action" value="addPaymentMode">
                                <div class="row g-2">
                                    <div class="col-sm-7">
                                        <input type="text" class="form-control" name="modeName"
                                               placeholder="Mode Name (e.g. Apple Pay, Amex)" required>
                                    </div>
                                    <div class="col-sm-5">
                                        <select class="form-select" name="accountType" required>
                                            <option value="BANK">Bank Account</option>
                                            <option value="CASH">Cash Pool</option>
                                        </select>
                                    </div>
                                    <div class="col-12 mt-2">
                                        <button type="submit" class="btn btn-success w-100 btn-sm py-2 fw-semibold">
                                            <i class="bi bi-plus-lg me-1"></i> Add Payment Mode
                                        </button>
                                    </div>
                                </div>
                            </form>
                        </div>

                        <!-- Existing Payment Modes List -->
                        <h6 class="fw-bold mb-3 text-muted small text-uppercase">Existing Payment Modes</h6>
                        <div class="table-responsive" style="max-height: 400px; overflow-y: auto;">
                            <table class="table table-hover align-middle table-sm">
                                <thead class="table-light sticky-top">
                                    <tr>
                                        <th>Mode Name</th>
                                        <th>Linked Account Pool</th>
                                        <th class="text-end">Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="mode" items="${paymentModes}">
                                        <tr>
                                            <td class="fw-medium">${mode.modeName}</td>
                                            <td>
                                                <span class="badge ${mode.accountType == 'BANK' ? 'badge-bank' : 'badge-cash'} px-2 py-1">
                                                    <i class="bi ${mode.accountType == 'BANK' ? 'bi-bank' : 'bi-cash'} me-1"></i>
                                                    Affects ${mode.accountType}
                                                </span>
                                            </td>
                                            <td class="text-end">
                                                <button type="button" class="btn btn-outline-danger btn-sm py-0 px-2"
                                                        data-bs-toggle="modal"
                                                        data-bs-target="#deleteModeModal${mode.id}"
                                                        title="Delete Payment Mode">
                                                    <i class="bi bi-trash"></i>
                                                </button>

                                                <!-- Delete Payment Mode Modal -->
                                                <div class="modal fade" id="deleteModeModal${mode.id}" tabindex="-1" aria-hidden="true">
                                                    <div class="modal-dialog modal-dialog-centered">
                                                        <div class="modal-content">
                                                            <div class="modal-header">
                                                                <h5 class="modal-title fw-bold text-danger">Delete Payment Mode</h5>
                                                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                            </div>
                                                            <div class="modal-body text-start">
                                                                <p>Are you sure you want to delete payment mode <strong>${mode.modeName}</strong>?</p>
                                                                <small class="text-muted">Note: A payment mode can only be deleted if no transactions are linked to it.</small>
                                                            </div>
                                                            <div class="modal-footer">
                                                                <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Cancel</button>
                                                                <form action="${pageContext.request.contextPath}/manage-categories" method="post" class="d-inline">
                                                                    <input type="hidden" name="action" value="deletePaymentMode">
                                                                    <input type="hidden" name="paymentModeId" value="${mode.id}">
                                                                    <button type="submit" class="btn btn-danger btn-sm">Delete</button>
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
                    </div>
                </div>
            </div>

        </div>

    </div>
</main>

<jsp:include page="includes/footer.jsp"/>
