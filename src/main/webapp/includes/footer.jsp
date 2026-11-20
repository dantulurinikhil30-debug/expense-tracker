<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<footer class="bg-white border-top py-3 mt-5 text-center text-muted">
    <div class="container">
        <small>&copy; <%= java.time.Year.now().getValue() %> Personal Expense &amp; Income Tracker | Built with Java Servlets, Hibernate ORM, MySQL &amp; Chart.js</small>
    </div>
</footer>

<!-- Bootstrap 5.3.3 Bundle JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script>

<!-- Custom Dashboard JS -->
<script src="${pageContext.request.contextPath}/js/dashboard.js"></script>
</body>
</html>
