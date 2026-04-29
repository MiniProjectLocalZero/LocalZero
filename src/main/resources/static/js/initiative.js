function toggleLike(element) {
    const initiativeId = element.getAttribute('data-id');
    const heartIcon = element.querySelector('.heart-icon');
    const likeCountSpan = element.querySelector('.like-count');

    const token = document.querySelector('meta[name="_csrf"]').content;
    const header = document.querySelector('meta[name="_csrf_header"]').content;

    fetch(`/initiatives/${initiativeId}/like`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [header]: token
        }
    })
        .then(response => {
            if (!response.ok) throw new Error('Network response was not ok');
            return response.json();
        })
        .then(data => {
            if (data.success) {
                likeCountSpan.innerText = data.newLikeCount;

                if (data.isLiked) {
                    heartIcon.classList.replace('far', 'fas');
                    heartIcon.classList.add('liked');
                } else {
                    heartIcon.classList.replace('fas', 'far');
                    heartIcon.classList.remove('liked');
                }
            }
        })
        .catch(error => console.error('Error:', error));
}