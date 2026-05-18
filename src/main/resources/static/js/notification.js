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

        document.addEventListener("click", (event) => {
            const container = bellIcon.closest(".notification-container");

            if (container && !container.contains(event.target) && !dropdown.classList.contains("hidden")) {
                dropdown.classList.add("hidden");
            }
        });
    }

    const markReadBtn = document.getElementById("mark-all-read");
    if (markReadBtn) {
        markReadBtn.addEventListener("click", function(event) {
            event.preventDefault();

            fetch("/notifications/mark-all-read", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                }
            })
                .then(response => {
                    if (response.ok) {
                        console.log("Alla notiser markerade som lästa i databasen!");

                        // 1. Dölj den röda pricken direkt
                        const badge = document.getElementById('unread-badge');
                        if (badge) {
                            badge.style.display = 'none';
                            badge.textContent = '0';
                        }

                        const unreadItems = document.querySelectorAll('.notification-item.unread');
                        unreadItems.forEach(item => {
                            item.classList.remove('unread');
                        });

                    } else {
                        console.error("Kunde inte markera som lästa. Status:", response.status);
                    }
                })
                .catch(error => console.error("Något gick fel med markeringen:", error));
        });
    }
});