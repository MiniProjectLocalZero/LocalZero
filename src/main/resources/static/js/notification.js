
window.addEventListener('load', function () {
    console.log('Script loaded')
    fetch("/notifications/unread-count")
        .then(response => response.json())
        .then(count => {
            console.log("TOTAL AMOUNT OF UNREAD NOTIFICATIONS:", count);
            const badge = document.getElementById('unread-badge');
            if (count > 0) {
                badge.textContent = count > 99 ? '99+' : count;
                badge.style.display = 'flex';
            }
        })
        .catch(error => console.error('Could not fetch notifications:', error));

    const bellIcon = document.getElementById("bell-icon");
    const dropdown = document.getElementById("notification-dropdown");

    if (bellIcon && dropdown) {
        bellIcon.addEventListener("click", (event) => {
            event.preventDefault();
            dropdown.classList.toggle("hidden");
        });

        // Close menu is clicking anywhere else on page
        document.addEventListener("click", (event) => {
            const container = bellIcon.closest(".notification-container");

            if (container && !container.contains(event.target) && !dropdown.classList.contains("hidden")) {
                dropdown.classList.add("hidden");
            }
        });
    }
})