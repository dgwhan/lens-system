/**
 * LENS Camera Rental - Rental Period & Date Picker Script
 * Day-based rental scope (24-hour rolling increment):
 * - Renting today and returning tomorrow is 1 day (24h) and is FULLY ALLOWED.
 * - Return date must be after start date (same-day / past dates disabled for return).
 * - Dynamic duration calculation: DURATION: 1 DAY, DURATION: 2 DAYS, etc.
 * - Automatic locking once dates are selected (with Change/Reset button to unlock).
 */
document.addEventListener('DOMContentLoaded', function () {
    const wrapper = document.querySelector('.rental-period-section');
    if (!wrapper) return;

    const startBox = wrapper.querySelector('[data-role="start-box"]');
    const endBox = wrapper.querySelector('[data-role="end-box"]');
    const startText = wrapper.querySelector('[data-role="start-text"]');
    const endText = wrapper.querySelector('[data-role="end-text"]');
    const durationDisplay = wrapper.querySelector('[data-role="duration-display"]');
    const unlockBtn = wrapper.querySelector('[data-role="unlock-btn"]');
    const popup = wrapper.querySelector('.rental-calendar-popup');
    const monthTitle = wrapper.querySelector('[data-role="month-title"]');
    const stepHint = wrapper.querySelector('[data-role="step-hint"]');
    const calendarDays = wrapper.querySelector('[data-role="calendar-days"]');
    const prevBtn = wrapper.querySelector('[data-role="prev-month"]');
    const nextBtn = wrapper.querySelector('[data-role="next-month"]');
    const resetBtn = wrapper.querySelector('[data-role="reset-dates"]');
    const hiddenStart = wrapper.querySelector('[data-role="start-hidden"]');
    const hiddenEnd = wrapper.querySelector('[data-role="end-hidden"]');

    let startDate = null;
    let endDate = null;
    let activeTarget = 'start'; // 'start' or 'end'
    let isLocked = false;

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    let viewYear = today.getFullYear();
    let viewMonth = today.getMonth();

    const monthNames = [
        'January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'
    ];

    function formatDisplayDate(date) {
        if (!date) return 'dd / mm / yyyy';
        const d = String(date.getDate()).padStart(2, '0');
        const m = String(date.getMonth() + 1).padStart(2, '0');
        const y = date.getFullYear();
        return `${d} / ${m} / ${y}`;
    }

    function formatIsoDate(date) {
        if (!date) return '';
        const d = String(date.getDate()).padStart(2, '0');
        const m = String(date.getMonth() + 1).padStart(2, '0');
        const y = date.getFullYear();
        return `${y}-${m}-${d}`;
    }

    function calculateDurationDays(start, end) {
        if (!start || !end) return null;
        const diffTime = end.getTime() - start.getTime();
        const diffDays = Math.round(diffTime / (1000 * 60 * 60 * 24));
        return diffDays >= 1 ? diffDays : null;
    }

    function updateDuration() {
        if (!startDate || !endDate) {
            if (durationDisplay) {
                durationDisplay.textContent = 'DURATION: --';
                durationDisplay.classList.remove('is-valid');
            }
            return;
        }
        const days = calculateDurationDays(startDate, endDate);
        if (days !== null && days >= 1) {
            if (durationDisplay) {
                const label = days === 1 ? '1 DAY' : `${days} DAYS`;
                durationDisplay.textContent = `DURATION: ${label}`;
                durationDisplay.classList.add('is-valid');
            }
        } else {
            if (durationDisplay) {
                durationDisplay.textContent = 'DURATION: --';
                durationDisplay.classList.remove('is-valid');
            }
        }
    }

    function renderCalendar() {
        if (!monthTitle || !calendarDays) return;
        monthTitle.textContent = `${monthNames[viewMonth]} ${viewYear}`;

        if (prevBtn) {
            const isCurrentMonth = (viewYear === today.getFullYear() && viewMonth === today.getMonth());
            prevBtn.disabled = isCurrentMonth;
            prevBtn.style.opacity = isCurrentMonth ? '0.35' : '1';
            prevBtn.style.cursor = isCurrentMonth ? 'not-allowed' : 'pointer';
        }

        if (stepHint) {
            if (activeTarget === 'start') {
                stepHint.textContent = 'Select start date (pickup date)';
            } else {
                stepHint.textContent = 'Select return date (>= 1 day / 24h count)';
            }
        }

        const firstDayIndex = new Date(viewYear, viewMonth, 1).getDay();
        const daysInMonth = new Date(viewYear, viewMonth + 1, 0).getDate();

        calendarDays.innerHTML = '';

        let row = document.createElement('tr');

        // Blank cells before the 1st
        for (let i = 0; i < firstDayIndex; i++) {
            const cell = document.createElement('td');
            cell.innerHTML = '<span class="calendar-day-btn is-empty"></span>';
            row.appendChild(cell);
        }

        for (let day = 1; day <= daysInMonth; day++) {
            if (row.children.length === 7) {
                calendarDays.appendChild(row);
                row = document.createElement('tr');
            }

            const currentCellDate = new Date(viewYear, viewMonth, day);
            currentCellDate.setHours(0, 0, 0, 0);

            const cell = document.createElement('td');
            const btn = document.createElement('button');
            btn.type = 'button';
            btn.className = 'calendar-day-btn';
            btn.textContent = day;

            //Past dates are never allowed
            const isPast = currentCellDate < today;

            //When choosing End Date, rental is 24h rolling count.
            let violatesMinDuration = false;
            if (activeTarget === 'end' && startDate) {
                const diffTime = currentCellDate.getTime() - startDate.getTime();
                const diffDays = Math.round(diffTime / (1000 * 60 * 60 * 24));
                if (diffDays < 1) {
                    violatesMinDuration = true;
                }
            }

            if (isPast || violatesMinDuration) {
                btn.disabled = true;
                btn.classList.add('is-disabled');
                if (violatesMinDuration && !isPast) {
                    btn.title = 'Return date must be after pickup date (minimum 1 day / 24h)';
                }
            } else {
                // Check if start or end date
                const isStart = startDate && currentCellDate.getTime() === startDate.getTime();
                const isEnd = endDate && currentCellDate.getTime() === endDate.getTime();
                const inRange = startDate && endDate && currentCellDate > startDate && currentCellDate < endDate;

                if (isStart) btn.classList.add('is-start-date');
                if (isEnd) btn.classList.add('is-end-date');
                if (inRange) btn.classList.add('is-in-range');

                btn.addEventListener('click', function (e) {
                    e.stopPropagation();
                    handleDateClick(currentCellDate);
                });
            }

            cell.appendChild(btn);
            row.appendChild(cell);
        }

        // Fill remaining row cells
        while (row.children.length < 7 && row.children.length > 0) {
            const cell = document.createElement('td');
            cell.innerHTML = '<span class="calendar-day-btn is-empty"></span>';
            row.appendChild(cell);
        }
        if (row.children.length > 0) {
            calendarDays.appendChild(row);
        }
    }

    function handleDateClick(selectedDate) {
        if (activeTarget === 'start') {
            startDate = new Date(selectedDate);
            endDate = null;
            activeTarget = 'end';
            updateInputs();
            renderCalendar();
        } else {
            // Target is end: check that return date is at least next day (24h count, diffDays >= 1)
            const diffTime = selectedDate.getTime() - startDate.getTime();
            const diffDays = Math.round(diffTime / (1000 * 60 * 60 * 24));

            if (diffDays >= 1) {
                endDate = new Date(selectedDate);
                isLocked = true;
                closePopup();
                updateInputs();
            }
        }
    }

    function updateInputs() {
        if (startText) {
            if (startDate) {
                startText.textContent = formatDisplayDate(startDate);
                startText.classList.add('has-value');
            } else {
                startText.textContent = 'dd / mm / yyyy';
                startText.classList.remove('has-value');
            }
        }
        if (endText) {
            if (endDate) {
                endText.textContent = formatDisplayDate(endDate);
                endText.classList.add('has-value');
            } else {
                endText.textContent = 'dd / mm / yyyy';
                endText.classList.remove('has-value');
            }
        }
        if (hiddenStart) hiddenStart.value = formatIsoDate(startDate);
        if (hiddenEnd) hiddenEnd.value = formatIsoDate(endDate);

        if (startBox && endBox) {
            startBox.classList.toggle('is-active', !isLocked && activeTarget === 'start' && popup.classList.contains('is-open'));
            endBox.classList.toggle('is-active', !isLocked && activeTarget === 'end' && popup.classList.contains('is-open'));
            startBox.classList.toggle('is-locked', isLocked);
            endBox.classList.toggle('is-locked', isLocked);
        }

        if (unlockBtn) {
            unlockBtn.style.display = isLocked ? 'inline-block' : 'none';
        }

        updateDuration();
    }

    function openPopup(target) {
        if (isLocked) return;
        activeTarget = target;
        popup.classList.add('is-open');
        if (target === 'start' && startDate) {
            viewYear = startDate.getFullYear();
            viewMonth = startDate.getMonth();
        } else if (target === 'end') {
            if (endDate) {
                viewYear = endDate.getFullYear();
                viewMonth = endDate.getMonth();
            } else if (startDate) {
                viewYear = startDate.getFullYear();
                viewMonth = startDate.getMonth();
            }
        }
        updateInputs();
        renderCalendar();
    }

    function closePopup() {
        popup.classList.remove('is-open');
        if (startBox) startBox.classList.remove('is-active');
        if (endBox) endBox.classList.remove('is-active');
    }

    function unlockAndReset(targetMode) {
        isLocked = false;
        startDate = null;
        endDate = null;
        activeTarget = targetMode || 'start';
        updateInputs();
        openPopup(activeTarget);
    }

    if (startBox) {
        startBox.addEventListener('click', function (e) {
            e.stopPropagation();
            if (isLocked) return;
            if (popup.classList.contains('is-open') && activeTarget === 'start') {
                closePopup();
            } else {
                openPopup('start');
            }
        });
    }

    if (endBox) {
        endBox.addEventListener('click', function (e) {
            e.stopPropagation();
            if (isLocked) return;
            if (popup.classList.contains('is-open') && activeTarget === 'end') {
                closePopup();
            } else {
                if (!startDate) {
                    openPopup('start');
                } else {
                    openPopup('end');
                }
            }
        });
    }

    if (unlockBtn) {
        unlockBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            unlockAndReset('start');
        });
    }

    if (prevBtn) {
        prevBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            viewMonth--;
            if (viewMonth < 0) {
                viewMonth = 11;
                viewYear--;
            }
            renderCalendar();
        });
    }

    if (nextBtn) {
        nextBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            viewMonth++;
            if (viewMonth > 11) {
                viewMonth = 0;
                viewYear++;
            }
            renderCalendar();
        });
    }

    if (resetBtn) {
        resetBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            unlockAndReset('start');
        });
    }

    // Close on click outside
    document.addEventListener('click', function (e) {
        if (!wrapper.contains(e.target)) {
            closePopup();
        }
    });

    renderCalendar();
    updateInputs();
});
