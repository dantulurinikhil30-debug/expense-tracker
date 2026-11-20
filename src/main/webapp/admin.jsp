<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="en_IN"/>

<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="/includes/header.jsp" %>
    <title>Admin Panel - ExpenseTracker</title>
    <style>
        .admin-hero {
            background: linear-gradient(135deg, #2d1b69 0%, #4a1a8a 50%, #6b21a8 100%);
            padding: 2rem 0;
            margin-bottom: 2rem;
        }
        .admin-hero h2 {
            color: #fff;
            font-weight: 700;
        }
        .admin-hero p {
            color: rgba(255,255,255,0.75);
        }
        .stat-card {
            border: none;
            border-radius: 16px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.08);
            transition: transform 0.2s ease;
        }
        .stat-card:hover {
            transform: translateY(-3px);
        }
        .stat-card .stat-icon {
            width: 56px;
            height: 56px;
            border-radius: 14px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.5rem;
        }
        .stat-card .stat-value {
            font-size: 1.75rem;
            font-weight: 700;
            line-height: 1.2;
        }
        .stat-card .stat-label {
            font-size: 0.85rem;
            color: #6c757d;
            font-weight: 500;
        }
        .users-card {
            border: none;
            border-radius: 16px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.06);
        }
        .badge-admin {
            background: linear-gradient(135deg, #7c3aed, #9333ea);
            color: #fff;
            font-weight: 600;
            font-size: 0.75rem;
        }
        .badge-user {
            background: linear-gradient(135deg, #3b82f6, #2563eb);
            color: #fff;
            font-weight: 600;
            font-size: 0.75rem;
        }
        .table-admin thead th {
            background: #f8f9fa;
            border-bottom: 2px solid #e2e8f0;
            font-weight: 600;
            font-size: 0.85rem;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            color: #475569;
            white-space: nowrap;
        }
        .table-admin tbody td {
            vertical-align: middle;
            font-size: 0.9rem;
        }
        .btn-delete-user {
            border-radius: 8px;
            font-size: 0.8rem;
            padding: 0.35rem 0.75rem;
        }
    </style>
</head>
<body>

    <%@ include file="/includes/navbar.jsp" %>

    <!-- Admin Hero Section -->
    <div class="admin-hero">
        <div class="container">
            <div class="d-flex align-items-center gap-3">
                <div class="bg-white bg-opacity-10 rounded-3 p-3">
                    <i class="bi bi-shield-lock-fill text-warning fs-2"></i>
                </div>
                <div>
                    <h2 class="mb-1"><i class="bi bi-gear-wide-connected me-2"></i>Admin Panel</h2>
                    <p class="mb-0">System-wide user management and financial overview</p>
                </div>
            </div>
        </div>
    </div>

    <div class="container mb-5">

        <!-- Success / Error Alerts -->
        <c:if test="${not empty param.success}">
            <div class="alert alert-success alert-dismissible fade show rounded-3 shadow-sm" role="alert">
                <i class="bi bi-check-circle-fill me-2"></i> ${param.success}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        <c:if test="${not empty param.error}">
            <div class="alert alert-danger alert-dismissible fade show rounded-3 shadow-sm" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2"></i> ${param.error}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <!-- Overview Statistics Cards -->
        <div class="row g-4 mb-4">
            <!-- Total Users -->
            <div class="col-md-4">
                <div class="card stat-card h-100">
                    <div class="card-body d-flex align-items-center gap-3 p-4">
                        <div class="stat-icon bg-primary bg-opacity-10 text-primary">
                            <i class="bi bi-people-fill"></i>
                        </div>
                        <div>
                            <div class="stat-value text-primary">${totalUsers}</div>
                            <div class="stat-label">Total Registered Users</div>
                        </div>
                    </div>
                </div>
            </div>
            <!-- Total Transactions -->
            <div class="col-md-4">
                <div class="card stat-card h-100">
                    <div class="card-body d-flex align-items-center gap-3 p-4">
                        <div class="stat-icon bg-success bg-opacity-10 text-success">
                            <i class="bi bi-receipt-cutoff"></i>
                        </div>
                        <div>
                            <div class="stat-value text-success">
                                <fmt:formatNumber value="${totalSystemTransactions}" type="number" groupingUsed="true"/>
                            </div>
                            <div class="stat-label">Total System Transactions</div>
                        </div>
                    </div>
                </div>
            </div>
            <!-- Total Combined Balances -->
            <div class="col-md-4">
                <div class="card stat-card h-100">
                    <div class="card-body d-flex align-items-center gap-3 p-4">
                        <div class="stat-icon bg-warning bg-opacity-10 text-warning">
                            <i class="bi bi-wallet-fill"></i>
                        </div>
                        <div>
                            <div class="stat-value text-warning">
                                ₹<fmt:formatNumber value="${totalSystemBalance}" type="number" minFractionDigits="2" maxFractionDigits="2"/>
                            </div>
                            <div class="stat-label">Total Combined Balances</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Users Management Table -->
        <div class="card users-card">
            <div class="card-header bg-white py-3 px-4 d-flex align-items-center justify-content-between">
                <h5 class="mb-0 fw-bold">
                    <i class="bi bi-person-lines-fill text-primary me-2"></i> User Management
                </h5>
                <span class="badge bg-primary rounded-pill fs-6">${totalUsers} users</span>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover table-admin mb-0">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Full Name</th>
                                <th>Email</th>
                                <th class="text-center">Role</th>
                                <th class="text-end">Bank Balance</th>
                                <th class="text-end">Cash Balance</th>
                                <th class="text-end">Total Balance</th>
                                <th class="text-center">Transactions</th>
                                <th class="text-center">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty allUsers}">
                                    <c:forEach var="u" items="${allUsers}">
                                        <tr>
                                            <td class="fw-semibold">#${u.id}</td>
                                            <td>
                                                <div class="d-flex align-items-center gap-2">
                                                    <div class="bg-primary bg-opacity-10 rounded-circle d-flex align-items-center justify-content-center" 
                                                         style="width:34px;height:34px;">
                                                        <i class="bi bi-person-fill text-primary"></i>
                                                    </div>
                                                    <span class="fw-semibold">${u.fullName}</span>
                                                </div>
                                            </td>
                                            <td class="text-muted">${u.email}</td>
                                            <td class="text-center">
                                                <c:choose>
                                                    <c:when test="${u.role == 'ADMIN'}">
                                                        <span class="badge badge-admin px-2 py-1 rounded-pill">
                                                            <i class="bi bi-shield-fill me-1"></i>ADMIN
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge badge-user px-2 py-1 rounded-pill">
                                                            <i class="bi bi-person-fill me-1"></i>USER
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="text-end">
                                                ₹<fmt:formatNumber value="${u.bankBalance}" type="number" minFractionDigits="2" maxFractionDigits="2"/>
                                            </td>
                                            <td class="text-end">
                                                ₹<fmt:formatNumber value="${u.cashBalance}" type="number" minFractionDigits="2" maxFractionDigits="2"/>
                                            </td>
                                            <td class="text-end fw-bold">
                                                ₹<fmt:formatNumber value="${u.totalBalance}" type="number" minFractionDigits="2" maxFractionDigits="2"/>
                                            </td>
                                            <td class="text-center">
                                                <span class="badge bg-secondary rounded-pill">
                                                    ${userTransactionCounts[u.id]}
                                                </span>
                                            </td>
                                            <td class="text-center">
                                                <c:choose>
                                                    <c:when test="${u.id == sessionScope.currentUser.id}">
                                                        <button class="btn btn-outline-secondary btn-delete-user" disabled
                                                                title="Cannot delete your own account">
                                                            <i class="bi bi-lock-fill"></i> You
                                                        </button>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <button class="btn btn-outline-danger btn-delete-user"
                                                                data-bs-toggle="modal" 
                                                                data-bs-target="#deleteUserModal-${u.id}"
                                                                title="Delete user and all data">
                                                            <i class="bi bi-trash3-fill me-1"></i> Delete
                                                        </button>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>

                                        <!-- Delete Confirmation Modal -->
                                        <c:if test="${u.id != sessionScope.currentUser.id}">
                                            <div class="modal fade" id="deleteUserModal-${u.id}" tabindex="-1"
                                                 aria-labelledby="deleteUserModalLabel-${u.id}" aria-hidden="true">
                                                <div class="modal-dialog modal-dialog-centered">
                                                    <div class="modal-content border-0 shadow">
                                                        <div class="modal-header bg-danger text-white border-0">
                                                            <h5 class="modal-title" id="deleteUserModalLabel-${u.id}">
                                                                <i class="bi bi-exclamation-triangle-fill me-2"></i>
                                                                Confirm User Deletion
                                                            </h5>
                                                            <button type="button" class="btn-close btn-close-white" 
                                                                    data-bs-dismiss="modal" aria-label="Close"></button>
                                                        </div>
                                                        <div class="modal-body p-4">
                                                            <div class="alert alert-warning mb-3">
                                                                <i class="bi bi-exclamation-triangle-fill me-2"></i>
                                                                <strong>This action is irreversible!</strong>
                                                            </div>
                                                            <p class="mb-2">You are about to permanently delete:</p>
                                                            <ul class="mb-3">
                                                                <li><strong>User:</strong> ${u.fullName} (${u.email})</li>
                                                                <li><strong>Transactions:</strong> ${userTransactionCounts[u.id]} records</li>
                                                                <li>All associated categories and payment modes</li>
                                                            </ul>
                                                            <p class="mb-0 text-danger fw-semibold">
                                                                Total balance of 
                                                                ₹<fmt:formatNumber value="${u.totalBalance}" type="number" minFractionDigits="2" maxFractionDigits="2"/>
                                                                will be lost.
                                                            </p>
                                                        </div>
                                                        <div class="modal-footer border-0 pt-0">
                                                            <button type="button" class="btn btn-secondary rounded-pill px-4" 
                                                                    data-bs-dismiss="modal">Cancel</button>
                                                            <form action="${pageContext.request.contextPath}/admin" method="post" class="d-inline">
                                                                <input type="hidden" name="action" value="deleteUser"/>
                                                                <input type="hidden" name="userId" value="${u.id}"/>
                                                                <button type="submit" class="btn btn-danger rounded-pill px-4">
                                                                    <i class="bi bi-trash3-fill me-1"></i> Delete Permanently
                                                                </button>
                                                            </form>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
                                        </c:if>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="9" class="text-center text-muted py-5">
                                            <i class="bi bi-person-x fs-1 d-block mb-2"></i>
                                            No users found in the system.
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

    </div>

    <%@ include file="/includes/footer.jsp" %>
</body>
</html>
