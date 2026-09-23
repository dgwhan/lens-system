/* availability: validate rental dates before ajax request */
function validateRentalDates() {
    var startDateInput = document.getElementById('rentalStartDate')
        || document.querySelector('[data-role="start-hidden"]')
        || document.querySelector('input[name="startDate"]');
    var endDateInput = document.getElementById('rentalEndDate')
        || document.querySelector('[data-role="end-hidden"]')
        || document.querySelector('input[name="endDate"]');

    var startVal = startDateInput ? startDateInput.value.trim() : '';
    var endVal = endDateInput ? endDateInput.value.trim() : '';

    //không chọn cả startDate và endDate
    if (!startVal && !endVal) {
        alert('Please select both start date and end date.');
        return false;
    }

    //thiếu startDate
    if (!startVal) {
        alert('Please select a start date.');
        return false;
    }

    //thiếu endDate
    if (!endVal) {
        alert('Please select an end date.');
        return false;
    }

    var hiddenStart = document.getElementById('startDateHidden');
    var hiddenEnd = document.getElementById('endDateHidden');
    if (hiddenStart) hiddenStart.value = startVal;
    if (hiddenEnd) hiddenEnd.value = endVal;

    return true;
}
