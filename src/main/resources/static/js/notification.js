
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
})