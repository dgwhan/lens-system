function toggleUserDropdown(event) {
    if (event) {
        event.stopPropagation();
    }
    var menu = document.getElementById('userDropdownMenu');
    if (menu) {
        menu.style.display = (menu.style.display === 'block') ? 'none' : 'block';
    }
}
window.toggleUserDropdown = toggleUserDropdown;

document.addEventListener('click', function (event) {
    var menu = document.getElementById('userDropdownMenu');
    if (menu && menu.style.display === 'block') {
        var dropdown = document.querySelector('.user-dropdown');
        if (!dropdown || !dropdown.contains(event.target)) {
            menu.style.display = 'none';
        }
    }
});
