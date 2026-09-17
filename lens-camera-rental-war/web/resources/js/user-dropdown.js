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

document.addEventListener('click', function () {
    var menu = document.getElementById('userDropdownMenu');
    if (menu && menu.style.display === 'block') {
        menu.style.display = 'none';
    }
});
