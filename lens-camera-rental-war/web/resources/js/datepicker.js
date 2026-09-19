/* ==========================================================================
   Lens Camera Rental - Custom Date Range Picker Script (datepicker.js)
   - Dual-input controls (Start Date & End Date)
   - Interactive calendar grid (Monday - Sunday)
   - Continuous range selection and visual highlights
   - Synchronizes with hidden JSF form inputs
   ========================================================================== */

(function () {
    'use strict';

    var MONTH_NAMES = [
        'January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'
    ];

    function padZero(num) {
        return num < 10 ? '0' + num : '' + num;
    }

    function formatDate(date) {
        if (!date) return '';
        var d = padZero(date.getDate());
        var m = padZero(date.getMonth() + 1);
        var y = date.getFullYear();
        return d + '/' + m + '/' + y;
    }

    function parseDate(str) {
        if (!str || typeof str !== 'string') return null;
        var parts = str.trim().split(/[\/\-\.]/);
        if (parts.length === 3) {
            var d = parseInt(parts[0], 10);
            var m = parseInt(parts[1], 10) - 1;
            var y = parseInt(parts[2], 10);
            if (!isNaN(d) && !isNaN(m) && !isNaN(y)) {
                var dt = new Date(y, m, d);
                if (dt.getFullYear() === y && dt.getMonth() === m && dt.getDate() === d) {
                    return dt;
                }
            }
        }
        return null;
    }

    function isSameDay(d1, d2) {
        if (!d1 || !d2) return false;
        return d1.getFullYear() === d2.getFullYear() &&
               d1.getMonth() === d2.getMonth() &&
               d1.getDate() === d2.getDate();
    }

    function isBetween(target, start, end) {
        if (!target || !start || !end) return false;
        var t = new Date(target.getFullYear(), target.getMonth(), target.getDate()).getTime();
        var s = new Date(start.getFullYear(), start.getMonth(), start.getDate()).getTime();
        var e = new Date(end.getFullYear(), end.getMonth(), end.getDate()).getTime();
        return t > s && t < e;
    }

    function DatePicker(container) {
        this.container = container;
        this.startBox = container.querySelector('[data-role="start-box"]');
        this.endBox = container.querySelector('[data-role="end-box"]');
        this.startDisplay = container.querySelector('[data-role="start-display"]');
        this.endDisplay = container.querySelector('[data-role="end-display"]');
        this.startHidden = container.querySelector('[data-role="start-hidden"]');
        this.endHidden = container.querySelector('[data-role="end-hidden"]');

        this.titleEl = container.querySelector('[data-role="calendar-title"]');
        this.prevBtn = container.querySelector('[data-role="prev-month"]');
        this.nextBtn = container.querySelector('[data-role="next-month"]');
        this.daysGrid = container.querySelector('[data-role="days-grid"]');
        this.resetBtn = container.querySelector('[data-role="reset-btn"]');
        this.applyBtn = container.querySelector('[data-role="apply-btn"]');

        // Initial dates from data attributes or hidden inputs if present
        var initStartStr = container.getAttribute('data-initial-start') || (this.startHidden ? this.startHidden.value : '');
        var initEndStr = container.getAttribute('data-initial-end') || (this.endHidden ? this.endHidden.value : '');

        this.startDate = parseDate(initStartStr);
        this.endDate = parseDate(initEndStr);

        // View month/year: defaults to startDate month, or current month
        var viewBase = this.startDate || new Date();
        this.viewYear = viewBase.getFullYear();
        this.viewMonth = viewBase.getMonth();

        this.activeTarget = 'start'; // 'start' or 'end'
        this.hoverDate = null;

        this.initEvents();
        this.updateDisplayInputs();
        this.render();
    }

    DatePicker.prototype.initEvents = function () {
        var self = this;

        if (this.startBox) {
            this.startBox.addEventListener('click', function () {
                self.activeTarget = 'start';
                self.updateInputFocus();
                self.render();
            });
        }

        if (this.endBox) {
            this.endBox.addEventListener('click', function () {
                self.activeTarget = 'end';
                self.updateInputFocus();
                self.render();
            });
        }

        if (this.prevBtn) {
            this.prevBtn.addEventListener('click', function (e) {
                e.preventDefault();
                self.viewMonth--;
                if (self.viewMonth < 0) {
                    self.viewMonth = 11;
                    self.viewYear--;
                }
                self.render();
            });
        }

        if (this.nextBtn) {
            this.nextBtn.addEventListener('click', function (e) {
                e.preventDefault();
                self.viewMonth++;
                if (self.viewMonth > 11) {
                    self.viewMonth = 0;
                    self.viewYear++;
                }
                self.render();
            });
        }

        if (this.resetBtn) {
            this.resetBtn.addEventListener('click', function (e) {
                e.preventDefault();
                self.startDate = null;
                self.endDate = null;
                self.activeTarget = 'start';
                self.updateDisplayInputs();
                self.syncHiddenInputs();
                self.render();
            });
        }

        if (this.applyBtn) {
            this.applyBtn.addEventListener('click', function (e) {
                e.preventDefault();
                self.syncHiddenInputs();
                self.triggerChangeEvent();
            });
        }
    };

    DatePicker.prototype.updateInputFocus = function () {
        if (this.startBox) {
            if (this.activeTarget === 'start') {
                this.startBox.classList.add('is-active');
            } else {
                this.startBox.classList.remove('is-active');
            }
        }
        if (this.endBox) {
            if (this.activeTarget === 'end') {
                this.endBox.classList.add('is-active');
            } else {
                this.endBox.classList.remove('is-active');
            }
        }
    };

    DatePicker.prototype.updateDisplayInputs = function () {
        this.updateInputFocus();

        if (this.startDisplay) {
            if (this.startDate) {
                this.startDisplay.textContent = formatDate(this.startDate);
                this.startDisplay.classList.remove('is-empty');
            } else {
                this.startDisplay.textContent = '--/--/----';
                this.startDisplay.classList.add('is-empty');
            }
        }

        if (this.endDisplay) {
            if (this.endDate) {
                this.endDisplay.textContent = formatDate(this.endDate);
                this.endDisplay.classList.remove('is-empty');
            } else {
                this.endDisplay.textContent = '--/--/----';
                this.endDisplay.classList.add('is-empty');
            }
        }
    };

    DatePicker.prototype.syncHiddenInputs = function () {
        if (this.startHidden) {
            this.startHidden.value = formatDate(this.startDate);
        }
        if (this.endHidden) {
            this.endHidden.value = formatDate(this.endDate);
        }
    };

    DatePicker.prototype.triggerChangeEvent = function () {
        var startFormatted = formatDate(this.startDate);
        var endFormatted = formatDate(this.endDate);
        var totalDays = 0;

        if (this.startDate && this.endDate) {
            var diffTime = Math.abs(this.endDate - this.startDate);
            totalDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
        }

        var customEvt = new CustomEvent('datepicker:rangeSet', {
            bubbles: true,
            detail: {
                startDate: this.startDate,
                endDate: this.endDate,
                startFormatted: startFormatted,
                endFormatted: endFormatted,
                totalDays: totalDays
            }
        });
        this.container.dispatchEvent(customEvt);

        // Also trigger native change on hidden inputs for JSF Ajax listeners
        if (this.startHidden) {
            this.startHidden.dispatchEvent(new Event('change', { bubbles: true }));
        }
        if (this.endHidden) {
            this.endHidden.dispatchEvent(new Event('change', { bubbles: true }));
        }
    };

    DatePicker.prototype.handleDateClick = function (clickedDate) {
        if (this.activeTarget === 'start') {
            this.startDate = clickedDate;
            if (this.endDate && this.endDate < this.startDate) {
                this.endDate = null;
            }
            // Auto advance to picking end date
            this.activeTarget = 'end';
        } else {
            // Picking end date
            if (!this.startDate) {
                this.startDate = clickedDate;
                this.activeTarget = 'end';
            } else if (clickedDate < this.startDate) {
                // If clicked earlier than start date, re-set start date
                this.startDate = clickedDate;
                this.activeTarget = 'end';
            } else {
                this.endDate = clickedDate;
                this.activeTarget = 'end';
            }
        }

        this.updateDisplayInputs();
        this.syncHiddenInputs();
        this.render();
    };

    DatePicker.prototype.render = function () {
        var self = this;
        var year = this.viewYear;
        var month = this.viewMonth;

        // Update Title (e.g. October 2026)
        if (this.titleEl) {
            this.titleEl.textContent = MONTH_NAMES[month] + ' ' + year;
        }

        if (!this.daysGrid) return;
        this.daysGrid.innerHTML = '';

        var today = new Date();

        // 1st of the current month
        var firstDay = new Date(year, month, 1);
        // Days in current month
        var totalDaysInMonth = new Date(year, month + 1, 0).getDate();
        // Days in previous month
        var totalDaysInPrevMonth = new Date(year, month, 0).getDate();

        // Convert Sunday (0) to 6, Monday (1) to 0, ..., Saturday (6) to 5
        var startDayOfWeek = (firstDay.getDay() + 6) % 7;

        var fragment = document.createDocumentFragment();

        // 1. Previous month trailing days
        for (var i = startDayOfWeek - 1; i >= 0; i--) {
            var prevDayNum = totalDaysInPrevMonth - i;
            var prevDate = new Date(year, month - 1, prevDayNum);
            fragment.appendChild(this.createDayCell(prevDate, prevDayNum, true));
        }

        // 2. Current month days
        for (var d = 1; d <= totalDaysInMonth; d++) {
            var curDate = new Date(year, month, d);
            fragment.appendChild(this.createDayCell(curDate, d, false));
        }

        // 3. Next month leading days to complete the 7-column grid
        var totalRendered = startDayOfWeek + totalDaysInMonth;
        var remainingDays = (7 - (totalRendered % 7)) % 7;
        for (var n = 1; n <= remainingDays; n++) {
            var nextDate = new Date(year, month + 1, n);
            fragment.appendChild(this.createDayCell(nextDate, n, true));
        }

        this.daysGrid.appendChild(fragment);
    };

    DatePicker.prototype.createDayCell = function (date, dayNum, isOutside) {
        var self = this;
        var cell = document.createElement('div');
        cell.className = 'datepicker-day-cell';

        var numSpan = document.createElement('span');
        numSpan.className = 'datepicker-day-num';
        numSpan.textContent = dayNum;
        cell.appendChild(numSpan);

        if (isOutside) {
            cell.classList.add('is-outside');
            return cell;
        }

        var today = new Date();
        if (isSameDay(date, today)) {
            cell.classList.add('is-today');
        }

        var effectiveEnd = this.endDate || (this.activeTarget === 'end' && this.hoverDate && this.hoverDate >= this.startDate ? this.hoverDate : null);

        var isStart = isSameDay(date, this.startDate);
        var isEnd = isSameDay(date, effectiveEnd);

        if (isStart) {
            cell.classList.add('is-range-start');
        }
        if (isEnd) {
            cell.classList.add('is-range-end');
        }
        if (this.startDate && effectiveEnd && isBetween(date, this.startDate, effectiveEnd)) {
            cell.classList.add('is-in-range');
        }

        cell.addEventListener('click', function () {
            self.handleDateClick(date);
        });

        cell.addEventListener('mouseenter', function () {
            if (self.startDate && !self.endDate && self.activeTarget === 'end') {
                self.hoverDate = date;
                self.renderHoverRange();
            }
        });

        return cell;
    };

    DatePicker.prototype.renderHoverRange = function () {
        var cells = this.daysGrid.querySelectorAll('.datepicker-day-cell:not(.is-outside)');
        var self = this;
        var effectiveEnd = this.hoverDate && this.hoverDate >= this.startDate ? this.hoverDate : null;

        cells.forEach(function (cell) {
            var dayNum = parseInt(cell.querySelector('.datepicker-day-num').textContent, 10);
            var date = new Date(self.viewYear, self.viewMonth, dayNum);

            var isStart = isSameDay(date, self.startDate);
            var isEnd = isSameDay(date, effectiveEnd);

            cell.classList.remove('is-in-range', 'is-range-end');

            if (isEnd && !isStart) {
                cell.classList.add('is-range-end');
            }
            if (self.startDate && effectiveEnd && isBetween(date, self.startDate, effectiveEnd)) {
                cell.classList.add('is-in-range');
            }
        });
    };

    // Auto-initialize all datepickers on the page
    function initAllDatePickers() {
        var elements = document.querySelectorAll('.custom-datepicker-wrapper');
        elements.forEach(function (el) {
            if (!el._datepickerInstance) {
                el._datepickerInstance = new DatePicker(el);
            }
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initAllDatePickers);
    } else {
        initAllDatePickers();
    }

    // Expose global init function
    window.initDatePickers = initAllDatePickers;
    window.DatePicker = DatePicker;
})();
