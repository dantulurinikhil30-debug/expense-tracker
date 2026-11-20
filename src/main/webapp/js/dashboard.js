/**
 * Dashboard Analytics Chart initialization using Chart.js with INR (₹) formatting
 */
function initExpenseChart(labels, data) {
    const canvas = document.getElementById('expensePieChart');
    if (!canvas) return;

    if (!labels || labels.length === 0 || !data || data.length === 0) {
        return;
    }

    const colorPalette = [
        '#4f46e5', // Indigo
        '#ef4444', // Red
        '#10b981', // Emerald
        '#f59e0b', // Amber
        '#06b6d4', // Cyan
        '#8b5cf6', // Violet
        '#ec4899', // Pink
        '#3b82f6', // Blue
        '#14b8a6', // Teal
        '#f97316', // Orange
        '#84cc16', // Lime
        '#64748b'  // Slate
    ];

    const ctx = canvas.getContext('2d');
    const total = data.reduce((acc, val) => acc + Number(val), 0);

    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: colorPalette.slice(0, labels.length),
                borderColor: '#ffffff',
                borderWidth: 2,
                hoverOffset: 8
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        boxWidth: 14,
                        padding: 16,
                        font: {
                            family: "-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif",
                            size: 12
                        }
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const value = Number(context.parsed) || 0;
                            const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : 0;
                            const formattedValue = value.toLocaleString('en-IN', {
                                minimumFractionDigits: 2,
                                maximumFractionDigits: 2
                            });
                            return ` ${context.label}: ₹${formattedValue} (${percentage}%)`;
                        }
                    }
                }
            },
            cutout: '62%'
        }
    });
}
