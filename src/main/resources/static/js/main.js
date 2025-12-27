document.addEventListener("DOMContentLoaded", function () {
    const options = {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    };

    const today = new Date().toLocaleDateString('id-ID', options);

    const dateElement = document.getElementById('current-date');
    if (dateElement) {
        dateElement.textContent = today;
    }
});
