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

            // 1. Skapa standard-headers
            const fetchHeaders = {
                "Content-Type": "application/json"
            };

            // 2. Leta efter CSRF-taggarna försiktigt (utan att krascha om de saknas)
            const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
            const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');

            // 3. Om de finns, lägg till dem i våra headers
            if (csrfTokenMeta && csrfHeaderMeta) {
                const csrfToken = csrfTokenMeta.getAttribute('content');
                const csrfHeader = csrfHeaderMeta.getAttribute('content');
                fetchHeaders[csrfHeader] = csrfToken;
            }

            // 4. Skicka anropet
            fetch("/notifications/mark-all-read", {
                method: "POST",
                headers: fetchHeaders
            })
                .then(response => {
                    if (response.ok) {
                        console.log("Alla notiser markerade som lästa i databasen!");

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